/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp;

import beaver.comp.spec.Alt;
import beaver.comp.spec.AltList;
import beaver.comp.spec.Item;
import beaver.comp.spec.ItemInline;
import beaver.comp.spec.ItemSymbol;
import beaver.comp.spec.Rule;
import beaver.comp.spec.RuleList;
import beaver.comp.spec.Spec;
import beaver.comp.spec.Term;
import beaver.comp.spec.TreeWalker;


/**
 * @author Alexander Demenchuk
 *
 */
public class InlineRulesExtractor extends TreeWalker
{
	private RuleList rules;
	private int lastRuleId;
	
	public void visit(Spec node)
	{
		rules = node.rules;
		super.visit(node);
	}
	
	public void visit(ItemInline node)
	{
		super.visit(node);
		/*
		 * the boostrap grammar does not support inline rule names
		 * we work around this by generating some synthetic names
		 */
		String nt = (node.refName != null ? node.refName.text : "SyntheticRule") + ++lastRuleId;
		Term name = new Term(nt);
		Alt ntDef = new Alt(node.def);
		node.def.copyLocation(ntDef);
		
		rules.add(new Rule(name, new AltList(ntDef)));

		Item rhsSym = new ItemSymbol(node.refName, new Term(nt), node.operator);
		node.copyLocation(rhsSym);
		
		node.replaceWith(rhsSym);
	}
}
