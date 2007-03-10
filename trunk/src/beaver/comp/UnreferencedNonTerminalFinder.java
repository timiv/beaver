/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp;

import java.util.HashSet;
import java.util.Set;

import beaver.comp.spec.ItemSymbol;
import beaver.comp.spec.Rule;
import beaver.comp.spec.TreeWalker;

/**
 * @author Alexander Demenchuk
 *
 */
public class UnreferencedNonTerminalFinder extends TreeWalker
{
	private Set nonterminals;
	private String currentRuleName;
	
	public UnreferencedNonTerminalFinder(Set nonterms)
	{
		nonterminals = new HashSet(nonterms);
	}

	public void visit(Rule rule)
    {
		if ( currentRuleName == null ) // then we are looking at the first, a.k.a. the goal, rule
		{
			nonterminals.remove(rule.name.text);
		}
		currentRuleName = rule.name.text;
	    super.visit(rule);
    }

	public void visit(ItemSymbol item)
    {
		String symbolName = item.name.text;
		if ( !symbolName.equals(currentRuleName) )
		{
			nonterminals.remove(item.name.text);
		}
    }
	
	public Set getSymbolNames()
	{
		return nonterminals;
	}
}
