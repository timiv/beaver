/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp.parser;

import beaver.util.BitSet;



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
	 * Rules to derives nonterminal symbols.
	 */
	Production[] productions;
	
	/**
	 * "error" symbol, if used by the grammar
	 */
	NonTerminal error;
	
	public Grammar(Terminal[] terms, NonTerminal[] nonterms, Production[] rules, NonTerminal err)
	{
		this.productions  = rules;
		this.nonterminals = nonterms;
		this.terminals    = terms;
		this.error        = err;
		/*
		 * Link nonterminals to their derivation rules
		 */
		for (int i = 0; i < nonterms.length; i++)
        {
	        nonterms[i].findAndSetDerivationRules(rules);
        }
		/*
		 * Mark nullable nonterminals
		 */
		for ( boolean marking = true; marking; )
		{
			marking = false;
			
			for (int i = 0; i < nonterms.length; i++)
            {
	            NonTerminal nt = nonterms[i];
	            
	            if ( !nt.matchesEmptyString() && nt.derivationRuleMatchesEmptyString() )
	            {
            		nt.setMatchesEmptyString();
            		/*
            		 * Need to repeat this marking loop at least once.
            		 * We found that this nontermial can match an empty string. This finding
            		 * can affect productions where it is used, so now some of them may also
            		 * match empty strings.
            		 */
            		marking = true;
	            }
            }
		}
		/*
		 * Build first sets.
		 */
		for (int i = 0; i < nonterms.length; i++)
        {
			nonterms[i].firstSet = new BitSet(terms.length);
        }
		/*
		 * Create first generation of first set terminals
		 */
		for (int i = 0; i < rules.length; i++)
        {
			NonTerminal             lhs = rules[i].lhs;
			Production.RHSElement[] rhs = rules[i].rhs;
	        
	        for (int j = 0; j < rhs.length; j++)
            {
	            Production.RHSElement e = rhs[j];

	            if ( e.symbol instanceof Terminal )
	            {
	            	lhs.firstSet.add(e.symbol.id);
	            }
	            else
	            {
		            NonTerminal nt = (NonTerminal) e.symbol;
		            if ( nt != lhs )
		            {
		            	lhs.firstSet.add(nt.firstSet);
		            }
	            }
	            
	            if ( !e.symbol.matchesEmptyString() )
	            {
	            	break;
	            }
            }
        }
		/*
		 * Keep adding terminals from leading nonterminals
		 */
		for (boolean adding = true; adding; )
		{
			adding = false;
			
			for (int i = 0; i < rules.length; i++)
	        {
				NonTerminal             lhs = rules[i].lhs;
				Production.RHSElement[] rhs = rules[i].rhs;
		        
		        for (int j = 0; j < rhs.length; j++)
	            {
		            Production.RHSElement e = rhs[j];

		            if ( e.symbol instanceof NonTerminal )
		            {
			            NonTerminal nt = (NonTerminal) e.symbol;
			            if ( nt != lhs )
			            {
			            	if ( lhs.firstSet.add(nt.firstSet) )
			            	{
			            		adding = true;
			            	}
			            }
		            }
		            
		            if ( !e.symbol.matchesEmptyString() )
		            {
		            	break;
		            }
	            }
	        }
		}
	}

}
