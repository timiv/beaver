/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import beaver.comp.spec.ItemString;
import beaver.comp.spec.ItemSymbol;
import beaver.comp.spec.TreeWalker;

/**
 * @author Alexander Demenchuk
 *
 */
public class TerminalCollector extends TreeWalker
{
	private Map constTokens = new HashMap();
	private Set namedTokens = new HashSet();
	private Set nonterminalNames;
	
	public TerminalCollector(Set nonterminalNames)
	{
		this.nonterminalNames = nonterminalNames;
	}

	public void visit(ItemString item)
    {
		String tokenText = item.text.text;
		if ( !constTokens.containsKey(tokenText) )
		{
			int n = constTokens.size() + 1;
			constTokens.put(tokenText, "TEXT" + n);
		}
    }

	public void visit(ItemSymbol item)
    {
		String name = item.symName.text;
		if ( !nonterminalNames.contains(name) )
		{
			namedTokens.add(name);
		}
    }

	public Map getConstTokens()
	{
		return constTokens;
	}
	
	public Set getNamedTokens()
	{
		return namedTokens;
	}
}
