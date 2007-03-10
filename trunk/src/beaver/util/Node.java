/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.util;

import beaver.Location;

/**
 * Base class for all tree elements.
 * 
 * @author Alexander Demenchuk
 */
public class Node extends IList.Element implements Location
{
	/**
	 * A line where the first character of the symbol is found.
	 */
	protected int line;
	
	/**
	 * A columns where the first character of the symbol is found. 
	 */
	protected int column;
	
	/**
	 * A line where the last character of the symbol is found.
	 */
	protected int endLine;
	
	/**
	 * A position of the next to last character of the symbol. 
	 */
	protected int endColumn;
	
	/**
	 * @see beaver.Location#setLocation(int, int, int, int)
	 */
	public void setLocation(int line, int column, int endLine, int endColumn)
	{
		this.line = line;
		this.column = column;
		this.endLine = endLine;
		this.endColumn = endColumn;
	}
	
	/**
	 * Makes a Node "occupy" the same space in the source as the other Node
	 */
	public void copyLocation(Location dest)
	{
		dest.setLocation(line, column, endLine, endColumn);
	}
}
