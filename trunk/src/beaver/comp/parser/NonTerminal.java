/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp.parser;

import beaver.util.BitSet;

/**
 * Non-terminals
 * 
 * @author Alexander Demenchuk
 */
public class NonTerminal extends Symbol
{
	/**
	 * First rule/production in the list of rules where this non-terminal is a LHS. 
	 */
	Production def;
	
	/**
	 * Non-terminal is nullable if any of its productions can derive an empty string.
	 */
	boolean isNullable;
	
	/**
	 * The set of terminals that begin strings derived from production rules
	 * where this non-terminal is a LHS
	 */
	BitSet firstSet;
	
	public NonTerminal(char id, String name)
	{
		super(id, name);
	}

}
