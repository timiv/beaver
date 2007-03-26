/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp;

import beaver.comp.ast.Alt;
import beaver.comp.ast.AltList;
import beaver.comp.ast.Item;
import beaver.comp.ast.ItemInline;
import beaver.comp.ast.ItemSymbol;
import beaver.comp.ast.ParserSpec;
import beaver.comp.ast.Rule;
import beaver.comp.ast.RuleList;
import beaver.comp.ast.Term;
import beaver.comp.ast.TreeWalker;


/**
 * @author Alexander Demenchuk
 *
 */
public class InlineRulesExtractor extends TreeWalker
{
	private RuleList rules;
	
	public void visit(ParserSpec node)
	{
		rules = node.ruleList;
		super.visit(node);
	}
	
	public void visit(ItemInline node)
	{
		super.visit(node);
		
		String nt;
		Alt ntDef = new Alt(node.itemList);
		Rule rule = rules.find(ntDef);
		if ( rule == null )
		{
			String ruleName = nt = Character.toUpperCase(node.name.text.charAt(0)) + node.name.text.substring(1);
			for ( int i = 1; (rule = rules.find(ruleName)) != null; i++ )
			{
				ruleName = nt + i;
			}
			nt = ruleName;
			
			Term name = new Term(nt);
			node.itemList.copyLocation(ntDef);
			
			rules.add(rule = new Rule(name, new AltList().add(ntDef)));
			node.copyLocation(rule);
		}
		else
		{
			nt = rule.name.text;
		}
		Item rhsSym = new ItemSymbol(node.name, new Term(nt), node.oper);
		node.copyLocation(rhsSym);
		
		node.replaceWith(rhsSym);
	}
}
