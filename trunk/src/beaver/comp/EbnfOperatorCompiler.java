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

import beaver.comp.cst.Alt;
import beaver.comp.cst.AltList;
import beaver.comp.cst.ItemList;
import beaver.comp.cst.ItemSymbol;
import beaver.comp.cst.ParserSpec;
import beaver.comp.cst.Rule;
import beaver.comp.cst.RuleList;
import beaver.comp.cst.Term;
import beaver.comp.cst.TreeWalker;


/**
 * @author Alexander Demenchuk
 *
 */
public class EbnfOperatorCompiler extends TreeWalker
{
	private RuleList rules;
	private Set addedRules = new HashSet();
	
	public void visit(ParserSpec node)
	{
		rules = node.ruleList;
		super.visit(node);
	}
	
	public void visit(ItemSymbol node)
	{
		if (node.oper == null)
			return;
		
		switch (node.oper.text.charAt(0))
		{
			case '?':
			{
				node.name.text = getOptionalSymbolName(node.name.text);
				break;
			}
			case '+':
			{
				node.name.text = getListSymbolName(node.name.text);
				break;
			}
			case '*':
			{
				node.name.text = getOptionalSymbolName(getListSymbolName(node.name.text));
				break;
			}
			default:
				throw new IllegalStateException("Unrecognized EBNF operator - " + node.oper.text + " on " + node.name.text);
		}
		node.oper = null;
	}
	
	/**
	 * Creates an OptSym rule for a symbol Sym
	 * 
	 * @param symName name of the symbol
	 * @return name of the optional symbol
	 */
	private String getOptionalSymbolName(String symName)
	{
		String optSymName = "Opt" + symName;
		if (!addedRules.contains(optSymName))
		{
			rules.add(
				new Rule(
					new Term(optSymName),
					new AltList().add(
						new Alt(
							null // i.e. empty
						)
					).add(
						new Alt(
							new ItemList().add(
								new ItemSymbol(new Term("item"), new Term(symName), null)
							)
						)
					)
				)
			);
			addedRules.add(optSymName);
		}
		return optSymName;
	}
	
	/**
	 * Create a SymList rule for a symbol Sym
	 * 
	 * @param symName name of the symbol that is an element of the list
	 * @return name of the symbol that represent a list
	 */
	private String getListSymbolName(String symName)
	{
		String lstSymName = symName + "List";
		if (!addedRules.contains(lstSymName))
		{
			rules.add(
				new Rule(
					new Term(lstSymName),
					new AltList().add(
						new Alt(
							new ItemList().add(
								new ItemSymbol(new Term("item"), new Term(symName), null)
							)
						)
					).add(
						new Alt(
							new ItemList().add(
								new ItemSymbol(new Term("list"), new Term(lstSymName), null)
							).add(
								new ItemSymbol(new Term("item"), new Term(symName), null)
							)
						)
					)
				)
			);
			addedRules.add(lstSymName);
		}
		return lstSymName;
	}
}
