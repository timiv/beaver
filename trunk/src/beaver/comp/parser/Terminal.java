/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp.parser;

/**
 * Terminals 
 * 
 * @author Alexander Demenchuk
 */
public class Terminal extends Symbol
{
	/**
	 * Precedence: 0 - undefined (lowest), 0xffff - highest
	 */
	char precedence;
	
	/**
	 * 'L' - left, 'R' - right, 'N' - none
	 */
	char accociativity;

	public Terminal(char id, String name)
	{
		super(id, name);
	}

}
