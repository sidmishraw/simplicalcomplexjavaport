/**
 * Project: SimplicalComplexAdvanced
 * Package: org.sjsu.sidmishaw.simplicalcomplex.core
 * File: Simplex.java
 * 
 * @author sidmishraw
 *         Last modified: Apr 17, 2017 4:18:20 PM
 */
package org.sjsu.sidmishaw.simplicalcomplex.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Simplicial complex is a collection of open simplicies, also known as 'K',
 * such that every
 * phases of the open simplex is in K. A collection of all association rules
 * (database object)
 * form a simplicial complex (geometry object).
 *
 * Aprior principle in data mining is called closed condition in simplicial
 * complex.
 * 
 * @author sidmishraw
 *
 *         Qualified Name: org.sjsu.sidmishaw.simplicalcomplex.core.Simplex
 *
 */
public class Simplex {
	
	// group name: "0", "0 1", "0 1 2" etc
	public String								gname;
	
	// # of rules to be mined, basically the #th simplex to be made
	public Integer								nrules;
	
	// [row#] -- inverted list of 1's
	public List<Integer>						ones	= new ArrayList<>();
	
	// [simplex, links] -- connected vertices on graph with AND'ed ones
	public List<Pair<Integer, List<Integer>>>	links	= new ArrayList<>();
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		return "Simplex [groupName=" + this.gname + ", nbrRules=" + this.nrules + ", ones=" + this.ones + ", links="
				+ this.links + "]";
	}
}
