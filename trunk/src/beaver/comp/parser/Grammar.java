/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp.parser;


/**
 * @author Alexander Demenchuk
 */
public class Grammar
{
	/**
	 * Symbols are created by a scanner and represent input tokens for the partser. 
	 */
	Terminal[] terminals;
	
	/**
	 * Symbols that are created by parser, i.e. when a RHS of a production is reduced to a LHS.
	 */
	NonTerminal[] nonterminals;
	
	/**
	 * Start of the linked list of grammar rules
	 */
	Production firstProd;
	
	public Grammar(Production firstProd, NonTerminal[] nonterms, Terminal[] terms)
	{
		this.firstProd = firstProd;
		this.nonterminals = nonterms;
		this.terminals = terms;
	}
}
