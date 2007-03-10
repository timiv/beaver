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

import beaver.comp.spec.ParserSpec;
import beaver.comp.spec.Rule;
import beaver.comp.spec.RuleList;
import beaver.comp.spec.TreeWalker;

/**
 * @author Alexander Demenchuk
 *
 */
public class NonTerminalCollector extends TreeWalker
{
	private Set names = new HashSet();
	private RuleList rules;
	private Log log;

	public NonTerminalCollector(Log log)
	{
		this.log = log;
	}
	
	public void visit(ParserSpec node)
    {
		rules = node.ruleList;
	    super.visit(node);
    }

	public void visit(Rule rule)
    {
		if ( rule.name.text.equals("error") )
		{
			log.error(rule.name, "Reserved rule is declared explicitly. Removed to continue.");
			rules.remove(rule);
		}
		else
		{
			names.add(rule.name.text);
		}
    }
	
	public Set getNames()
	{
		return names;
	}
	
}
