/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
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
	 * Rules to derive nonterminal symbols.
	 */
	Production[] productions;
	
	/**
	 * "goal" symbol
	 */
	NonTerminal goal;
	
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
		 * "error" nonterminal is "defined" implicitly, i.e.
		 * there are no explicit rules
		 */
		error.derivationRules = new Production[0];
		
		this.goal = rules[0].lhs; 
		/*
		 * Augment grammar if needed
		 */
		if ( goal.derivationRules.length > 1 )
		{
			char goalId = error.id++;
			
			NonTerminal newGoal = new NonTerminal(goalId, "GoalAs" + goal.name);
			
			char goalRuleId = (char) rules.length;
			Production goalRule = new Production(goalRuleId, newGoal.name, newGoal, new Production.RHSItem[] { new Production.RHSItem(goal) });
			
			newGoal.derivationRules = new Production[] { goalRule };
			
			/*
			 * Add new symbol and new rule to the mix
			 */
			nonterminals = new NonTerminal[nonterms.length + 1];
			System.arraycopy(nonterms, 0, nonterminals, 0, nonterms.length);
			nonterminals[nonterms.length] = newGoal;
			
			nonterms = nonterminals;
			
			productions = new Production[rules.length + 1];
			System.arraycopy(rules, 0, productions, 0, rules.length);
			productions[rules.length] = goalRule;
			
			rules = productions;

			goal = newGoal;
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
		 * Phase 0: setup
		 */
		for (int i = 0; i < nonterms.length; i++)
        {
			nonterms[i].firstSet = new BitSet(terms.length);
        }
		err.firstSet = new BitSet(terms.length);
		
		/*
		 * First Sets Phase 1:
		 * Create first generation of first set terminals
		 */
		for (int i = 0; i < rules.length; i++)
        {
			NonTerminal          lhs = rules[i].lhs;
			Production.RHSItem[] rhs = rules[i].rhs;
	        
	        for (int j = 0; j < rhs.length; j++)
            {
	            Symbol e = rhs[j].symbol;

	            if ( e instanceof Terminal )
	            {
	            	lhs.firstSet.add(e.id);
	            }
	            else
	            {
		            NonTerminal nt = (NonTerminal) e;
		            if ( nt != lhs )
		            {
		            	lhs.firstSet.add(nt.firstSet);
		            }
	            }
	            
	            if ( !e.matchesEmptyString() )
	            {
	            	break;
	            }
            }
        }
		/*
		 * First Sets Phase 2:
		 * Keep adding terminals from leading nonterminals
		 */
		for (boolean adding = true; adding; )
		{
			adding = false;
			
			for (int i = 0; i < rules.length; i++)
	        {
				NonTerminal          lhs = rules[i].lhs;
				Production.RHSItem[] rhs = rules[i].rhs;
		        
		        for (int j = 0; j < rhs.length; j++)
	            {
		            Symbol e = rhs[j].symbol;

		            if ( e instanceof NonTerminal )
		            {
			            NonTerminal nt = (NonTerminal) e;
			            if ( nt != lhs )
			            {
			            	if ( lhs.firstSet.add(nt.firstSet) )
			            	{
			            		adding = true;
			            	}
			            }
		            }
		            
		            if ( !e.matchesEmptyString() )
		            {
		            	break;
		            }
	            }
	        }
		}
	}

	public BitSet findUnreducibleProductions(State firstState)
	{
		BitSet set = new BitSet(productions.length);
		set.add(0, productions.length);
		for (State st = firstState; st != null; st = st.next)
		{
			if (st.reduceActions != null)
			{
				for ( Action.Reduce act = (Action.Reduce) st.reduceActions.first(); act != null; act = (Action.Reduce) act.next() )
				{
					set.erase(act.prod.id);
				}
			}
		}
		return set;
	}
}
