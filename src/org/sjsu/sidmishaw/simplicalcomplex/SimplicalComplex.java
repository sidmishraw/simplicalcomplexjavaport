/**
 * Project: SimplicalComplexAdvanced
 * Package: org.sjsu.sidmishaw.simplicalcomplex
 * File: SimplicalComplex.java
 * 
 * @author sidmishraw
 *         Last modified: Apr 17, 2017 3:43:04 PM
 */
package org.sjsu.sidmishaw.simplicalcomplex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sjsu.sidmishaw.simplicalcomplex.core.Pair;
import org.sjsu.sidmishaw.simplicalcomplex.core.Simplex;

/**
 * @author sidmishraw
 *
 *         Qualified Name: org.sjsu.sidmishaw.simplicalcomplex.SimplicalComplex
 *
 */
public class SimplicalComplex {
	
	// Define a static logger variable so that it references the
	// Logger instance named "SimplicalComplex".
	private static final Logger					logger			= LogManager.getLogger(SimplicalComplex.class);
	
	// The bitmap -- input data matrix :data -- the bitmap
	private static List<List<Boolean>>			data			= new ArrayList<>();
	
	// inverted list of 1's :lookup
	private static List<List<Integer>>			lookup			= new ArrayList<>();
	
	// [simplex, count] -- sorted list of qualified counts
	private static List<Pair<Integer, Integer>>	qcols			= new ArrayList<>();
	
	// {simplex: frequency}
	private static Map<Integer, Integer>		results			= new HashMap<>();
	
	private static int							numRules;
	private static int							thresholdLimit;
	private static float						threshold;
	
	// filename -- default
	private static String						outFilename		= "results.txt";
	private static BufferedWriter				bufferedWriter	= null;
	
	/**
	 * // The bitmap -- input data matrix :data
	 * private static List<List<Boolean>> inputDataMatrix = new ArrayList<>();
	 * 
	 * // inverted list of 1's :lookup
	 * private static List<List<Integer>> invertedLookup = new ArrayList<>();
	 * 
	 * @param inputFileName
	 * @param nbrCols
	 * @param nbrRows
	 */
	private static void readFile(String inputFileName, int nbrCols, int nbrRows) {
		
		logger.error("HARMLESS: Started reading from file named: " + inputFileName);
		
		for (int col = 0; col < nbrCols; col++) {
			
			// initialize the data and lookups with -1 and false respectively
			lookup.add(col,
					Stream.generate(() -> -1).limit(nbrRows).parallel().collect(java.util.stream.Collectors.toList()));
			
			data.add(col, Stream.generate(() -> false).limit(nbrRows).parallel()
					.collect(java.util.stream.Collectors.toList()));
		}
		
		try (BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(new File(inputFileName))))) {
			
			String line = null;
			int row = 0;
			int maxColumnLength = -1;
			
			while (null != (line = bufferedReader.readLine())) {
				
				String[] elements = line.split(" ");
				int length = Integer.parseInt(elements[0]);
				
				// it seems the guy makes a transpose of the matrix while
				// reading in
				// hence building it as a transpose
				// the first element of the line is the length of the column
				for (int i = 1; i <= length; i++) {
					
					int col = Integer.parseInt(elements[i]);
					
					data.get(col).set(row, true);
					
					lookup.get(col).set(row, row);
					
					if (col > maxColumnLength) {
						
						maxColumnLength = col;
					}
				}
				
				row++;
			}
			
			logger.error("HARMLESS: Max column length: " + maxColumnLength);
		} catch (IOException e) {
			
			logger.error(e);
		}
		
		logger.error("HARMLESS: Finished reading from file named: " + inputFileName);
	}
	
	/**
	 * This function run once on the data matrix to reduce the number of columns
	 * or 0-simplices
	 * so that the program can operate on reduced topological space. It also
	 * sorts the columns
	 * depending on the number of 1's appear in each column. This allows the
	 * program to run
	 * a lot faster because any initial 0-simplex below the threshold will be
	 * excluded.
	 */
	private static void reduceSpace() {
		
		for (int col = 0; col < (int) lookup.size(); col++) {
			
			int count = (int) lookup.get(col).size();
			
			if (count >= thresholdLimit) {
				
				qcols.add(new Pair<Integer, Integer>(col, count));
			}
		}
		
		// o1 and o2 are pairs
		Collections.sort(qcols, (pair1, pair2) -> pair1.second.compareTo(pair2.second));
	}
	
	/**
	 * The goal of this function is to build the simplex structure that is
	 * required for the algorithm to visit each
	 * simplex once, moving from lowest # of 1's to highest. Below is the format
	 * of the structure and what is being
	 * stored inside it.
	 * Simplex Structure:
	 * 1. name of simplex: "0", "0 1", "0 1 2" and etc.
	 * 2. # of rules or simplex dimension: 1 is for 0-simplex, 2 is for
	 * 1-simplex and so on.
	 * 3. indices to all AND'ed ones for this simplex. ex: inverted
	 * list={0,1000,65000}.
	 * 4. all qualified columns associated with this simplex with all AND'ed
	 * indices.
	 * ex: [col=0, inverted list={0,1122,32000,65123}]
	 * [col=4, inverted list={1,1122,32111,65000}]
	 * [col=1256, inverted list={0,32000,65000}]
	 * note: all {col,inverted_list} pairs must qualify the given threshold
	 * 
	 * @param simplex
	 * @param gname
	 * @param nrules
	 * @param ones
	 * @param cols
	 * @param start
	 */
	private static void buildSimplex(Simplex simplex, String gname, int nrules, List<Integer> ones,
			List<Pair<Integer, Integer>> cols, int start) {
		
		simplex.gname = gname;
		simplex.nrules = nrules;
		simplex.ones = ones;
		
		int nlinks = cols.size() - start;
		
		if (nlinks > 0) {
			
			for (int index = start; index < cols.size(); index++) {
				
				int col = cols.get(index).first;
				Pair<Integer, List<Integer>> link = new Pair<>(-1, new ArrayList<>());
				
				for (int row = 0; row < ones.size(); row++) {
					
					if (ones.get(row) != -1 && data.get(col).get(ones.get(row))) {
						
						link.second.add(ones.get(row));
					}
				}
				
				if (link.second.size() >= thresholdLimit) {
					
					link.first = col;
					simplex.links.add(link);
				}
			}
		}
	}
	
	/**
	 * Once the simplex is successfully created by buildSimplex, this function
	 * then uses that information
	 * to connect all possible links on topological space to it. However, it
	 * must do it in a way that
	 * is efficient and avoid reconnecting simplex links by navigating from
	 * left-to-right of all the
	 * qualified columns. As it tries to connect each of the simplex dimension,
	 * it prints out the simplex
	 * name and the large itemsets computed. Once all information are reported
	 * for this simplex, it
	 * affectively combines the next link to build a higher-dimension simplex
	 * and repeat the process.
	 *
	 * This is also known as finding all the star-neighbors for a given
	 * 0-simplex, called 'V' (vertex).
	 * Build all the open simplices that have 'V' as a phase. Note that once
	 * completed, all these
	 * open simplices must be removed from 'K'
	 * 
	 * @param simplex
	 * @throws IOException
	 */
	private static void connectSimplex(Simplex simplex) throws IOException {
		
		if (null == results.get(simplex.nrules)) {
			
			results.put(simplex.nrules, 1);
		} else {
			
			int newRules = results.get(simplex.nrules) + 1;
			results.put(simplex.nrules, newRules);
		}
		
		// print simplex
		printSimplex(simplex);
		
		if (simplex.nrules < numRules) {
			
			for (int i = 0; i < simplex.links.size(); i++) {
				
				Pair<Integer, List<Integer>> link = simplex.links.get(i);
				String gname = simplex.gname + " " + String.valueOf(link.first);
				List<Pair<Integer, Integer>> cols = new ArrayList<>();
				
				for (int j = i + 1; j < simplex.links.size(); j++) {
					
					Pair<Integer, List<Integer>> sublink = simplex.links.get(j);
					cols.add(new Pair<Integer, Integer>(sublink.first, (int) sublink.second.size()));
				}
				
				Simplex subsimplex = new Simplex();
				buildSimplex(subsimplex, gname, simplex.nrules + 1, link.second, cols, 0);
				connectSimplex(subsimplex);
			}
		}
	}
	
	/**
	 * @param simplex
	 * @throws IOException
	 */
	private static void printSimplex(Simplex simplex) throws IOException {
		
		logger.error("HARMLESS: Printing Simplex: " + simplex);
		
		bufferedWriter.write(String.format("[%s] %s\n", simplex.gname,
				simplex.ones.parallelStream().filter(e -> e != -1).collect(Collectors.toList()).size()));
		
		logger.error("HARMLESS: Printed Simplex: " + simplex);
	}
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
		logger.error("Starting simplical complex Java port");
		logger.error("by sidmishraw");
		
		if (args.length < 5) {
			
			logger.error("Usage: SimplicialComplex <#rules> <threshold> <#columns> <#records> <data_file>");
			
			System.exit(0);
		}
		
		try {
			
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outFilename))));
			
			numRules = Integer.parseInt(args[0]);
			threshold = Float.parseFloat(args[1]);
			
			int numColumns = Integer.parseInt(args[2]);
			int numRecords = Integer.parseInt(args[3]);
			String inputFile = args[4];
			
			thresholdLimit = (int) (threshold * numRecords);
			thresholdLimit = (thresholdLimit == 0 ? 1 : thresholdLimit);
			
			// read from .dat file
			readFile(inputFile, numColumns, numRecords);
			
			reduceSpace();
			
			// visit each simplex once starting with lowest # of 1's
			numRules = (numRules < numColumns) ? numRules : numColumns;
			
			logger.error("HARMLESS: Running Simplicial Complex rules: " + numRules + " threshold: " + threshold
					+ " qualified column count: " + qcols.size());
			
			for (int index = (int) qcols.size() - 1; index >= 0; index--) {
				
				int col = qcols.get(index).first;
				String gname = String.valueOf(col);
				Simplex simplex = new Simplex();
				
				// buildSimplex(simplex, gname, 1, lookup[col], qcols, index +
				// 1);
				buildSimplex(simplex, gname, 1, lookup.get(col), qcols, index + 1);
				connectSimplex(simplex);
				
				// track time for all shapes for every 10 vertices processed
				if (index == 0 || index == (qcols.size() - 1) || (index % 10) == 0) {
					
					System.out.print(String.format(" col %s, shape", index));
					
					for (Map.Entry<Integer, Integer> shape : results.entrySet()) {
						
						logger.error(String.format(" %s:%s", shape.getKey(), shape.getValue()));
					}
				}
			}
			
			logger.error("writing simplical complex results...");
			
			for (Map.Entry<Integer, Integer> shape : results.entrySet()) {
				
				logger.error(String.format(" %s-way support items:%s ", shape.getKey(), shape.getValue()));
			}
		} catch (IOException e) {
			
			logger.error(e);
		} finally {
			
			if (null != bufferedWriter) {
				
				bufferedWriter.close();
			}
		}
	}
}
