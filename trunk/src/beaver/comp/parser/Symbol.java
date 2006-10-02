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
 * Represents symbols of a grammar.
 * 
 * @author Alexander Demenchuk
 */
public class Symbol
{
	/**
	 * Symbol's ID
	 */
	char id;
	
	/**
	 * String that is used to reference this symbol in the specification.
	 */
	String name;
	
	protected Symbol(char id, String name)
	{
		this.name = name;
	}
}
