/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver;

/**
 * Protocol to transfer symbol location information.
 * 
 * @author Alexander Demenchuk
 */
public interface Location
{
	/**
	 * Sets provided line and column numbers. 
	 * 
	 * @param line start line
	 * @param column start column
	 * @param endLine end line
	 * @param endColumn emd column
	 */
	void setLocation(int line, int column, int endLine, int endColumn);
	
	/**
	 * Copies line and column numbers into another Location.
	 * 
	 * Typically the implementaion would just call:
	 * <code>
	 * dest.setLocation(...);
	 * </code>
	 * 
	 * @param dest acceptor of location information
	 */
	void copyLocation(Location dest);
}