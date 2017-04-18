/**
 * Project: SimplicalComplexAdvanced
 * Package: org.sjsu.sidmishaw.simplicalcomplex.core
 * File: Pair.java
 * 
 * @author sidmishraw
 *         Last modified: Apr 17, 2017 4:19:55 PM
 */
package org.sjsu.sidmishaw.simplicalcomplex.core;

/**
 * @author sidmishraw
 *
 *         Qualified Name: org.sjsu.sidmishaw.simplicalcomplex.core.Pair
 *
 *         Container to hold values -- emulating C++'s Pair or tuple
 */
public class Pair<X, Y> {
	
	public X	first;
	public Y	second;
	
	/**
	 * 
	 */
	public Pair() {}
	
	/**
	 * @param first
	 * @param second
	 */
	public Pair(X first, Y second) {
		
		this.first = first;
		this.second = second;
	}
	
}
