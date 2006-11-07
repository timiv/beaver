/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp.parser;

import java.util.ArrayList;
import java.util.Collection;

import beaver.util.BitSet;

/**
 * Non-terminals
 * 
 * @author Alexander Demenchuk
 */
public class NonTerminal extends Symbol
{
	/**
	 * Productions that define rules to derive this nonterminal (where this non-terminal is a LHS). 
	 */
	Production[] derivationRules;

	/**
	 * Non-terminal is nullable if any of its productions can derive an empty string.
	 */
	boolean isNullable;
	
	/**
	 * The set of all terminal symbols that could appear at the beginning of a string
	 * derived from this nonterminal.
	 */
	BitSet firstSet;
	
	public NonTerminal(char id, String name)
	{
		super(id, name);
	}

	void findAndSetDerivationRules(Production[] allRules)
	{
		Collection defs = new ArrayList();
        for (int i = 0; i < allRules.length; i++)
        {
            if (allRules[i].lhs == this)
            {
            	defs.add(allRules[i]);
            }
        }
        if (defs.size() == 0)
        	throw new IllegalStateException("Cannot find rules to derive symbol '" + name + "'");
		derivationRules = (Production[]) defs.toArray(new Production[defs.size()]);
	}
	
	/**
	 * Nonterminal can match an empty string only if one of its derivation rules
	 * matches an empty string. This method returns what has been found (so far)
	 * by calculating nullability of this nonterminal.
	 * 
	 * @return true if the symbol is nullable
	 */
	public boolean matchesEmptyString()
	{
		return isNullable;
	}

	/**
	 * This method is where we find out whether the nonterminal is nullable.
	 * 
	 * @return true if one of derivation rules matches an ampty string
	 */
	boolean derivationRuleMatchesEmptyString()
	{
		for (int i = 0; i < derivationRules.length; i++)
        {
	        if ( derivationRules[i].matchesEmptyString() )
	        	return true;
        }
		return false;
	}
	
	/**
	 * Sets the flag that this nonterminal can match empty strings. 
	 */
	void setMatchesEmptyString()
	{
		isNullable = true;
	}

}
