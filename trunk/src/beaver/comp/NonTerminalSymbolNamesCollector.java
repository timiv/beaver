/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp;

import java.util.HashSet;
import java.util.Set;

import beaver.comp.spec.Rule;
import beaver.comp.spec.TreeWalker;

/**
 * @author Alexander Demenchuk
 *
 */
public class NonTerminalSymbolNamesCollector extends TreeWalker
{
	private Set names = new HashSet();

	public void visit(Rule rule)
    {
		names.add(rule.name.text);
    }

	Set getNames()
	{
		return names;
	}
}
