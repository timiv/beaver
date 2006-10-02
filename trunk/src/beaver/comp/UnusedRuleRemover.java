/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp;

import java.util.Set;

import beaver.comp.spec.Rule;
import beaver.comp.spec.RuleList;
import beaver.comp.spec.Spec;
import beaver.comp.spec.TreeWalker;

/**
 * @author Alexander Demenchuk
 *
 */
public class UnusedRuleRemover extends TreeWalker
{
	private Log log;
	private Set names;
	private RuleList rules;
	
	public UnusedRuleRemover(Set names, Log log)
	{
		this.names = names;
		this.log = log;
	}

	public void visit(Spec spec)
    {
		rules = spec.rules;
	    super.visit(spec);
    }

	public void visit(Rule rule)
    {
		if ( names.contains(rule.name.text) )
		{
			rules.remove(rule);
			log.warning(rule.name, "Symbol '" + rule.name.text + "' is not used. Defining rule removed.");
		}
    }	
}
