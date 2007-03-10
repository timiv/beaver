/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */

package beaver.comp.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import beaver.util.BitSet;
import beaver.util.DList;
import beaver.util.IList;

/**
 * @author Alexander Demenchuk
 *
 */
public class State
{
	State       next;
	int         id;
	ItemSet     config;
	IList   	shiftActions;
	IList     	reduceActions;
	Production  defaultReduceRule;
	BitSet      defaultReduceRuleLookaheads;
	int         defaultReduceRuleLookaheadsSetIndex;
	
	State(int id, ItemSet core)
	{
		this.id = id;
		config = core;
	}
	
	void add(Action.Shift act)
	{
		if ( shiftActions == null )
			shiftActions = new DList();
		shiftActions.add(act);
	}
	
	void add(Action.Reduce act)
	{
		if ( reduceActions == null )
			reduceActions = new DList();
		reduceActions.add(act);
	}

	public String toString()
	{
		StringBuffer text = new StringBuffer();
		text.append(id).append(':').append('\n').append(config);
		return text.toString();
	}
	
	public static Collection collateDefaultReduceRuleLookaheadsSets(State firstState)
	{
    	Collection sets = new ArrayList();
    	Map definingStates = new HashMap();
		for (State s = firstState; s != null; s = s.next)
		{
			if ( s.defaultReduceRuleLookaheads != null )
			{
    			State def = (State) definingStates.get(s.defaultReduceRuleLookaheads);
    			if (def != null)
    			{
    				s.defaultReduceRuleLookaheadsSetIndex = def.defaultReduceRuleLookaheadsSetIndex;
    			}
    			else
    			{
    				definingStates.put(s.defaultReduceRuleLookaheads, s);
    				sets.add(s.defaultReduceRuleLookaheads);
    				s.defaultReduceRuleLookaheadsSetIndex = sets.size();
    			}
			}
		}
    	return sets;
	}
	
	public static class Builder
	{
		private Map             states     = new HashMap();
		private ItemSet.Builder setBuilder = new ItemSet.Builder();
		private State           last;
		
		public State createStates(Grammar grammar)
		{
			NonTerminal goal      = grammar.goal;
			Terminal[]  terminals = grammar.terminals;
			
			for (int i = 0; i < goal.derivationRules.length; i++)
            {
				setBuilder.getItem(goal.derivationRules[i], 0).addLookahead(Terminal.EOF);
            }
			State firstState = getState(new ItemSet(setBuilder.getCore()));
			
			for (State s = firstState; s != null; s = s.next)
			{
				s.config.reverseEmitters();
				s.config.resetContributions();
			}
			findLookaheads(firstState);
			buildReduceActions(firstState, terminals);
			
			return firstState;
		}
		
		private State getState(ItemSet newCore)
		{
			State st = (State) states.get(newCore);
			if (st != null)
			{
				st.config.copyEmitters(newCore);
			}
			else
			{
				newCore.buildClosure(setBuilder);
				states.put(newCore, st = new State(states.size() + 1, newCore));
				if (last == null)
					last = st;
				else
					last = last.next = st;
				buildShiftsFrom(st);
			}
			return st;
		}
		
		private void buildShiftsFrom(State st)
		{
			st.config.resetContributions();
			
			for ( Item item = st.config.getFirstItem(); item != null; item = item.next)
			{
				if ( item.hasContributed || item.isDotAfterLastSymbol() )
					continue;
				
				setBuilder.reset();
			
				Symbol lookahead = item.getSymbolAfterDot();
				/*
				 * For every item in the "from" state which also has the same symbol
				 * after the dot add the same item to the core under construction
				 * but with the dot shifted one symbol to the right.
				 */
				for ( Item x = item; x != null; x = x.next )
				{
					if ( x.hasContributed || x.isDotAfterLastSymbol() || x.getSymbolAfterDot() != lookahead )
						continue;
					
					setBuilder.getItem(x.makeItemForShiftedDot()).addEmitter(x);
					x.hasContributed = true;
				}
				State shiftTo = getState(new ItemSet(setBuilder.getCore()));
				
				// The state "shiftTo" is reached from the state "st" by a shift action on the symbol "sym"
				st.add(new Action.Shift(lookahead, shiftTo));
			}
		}
		
		private static void findLookaheads(State firstState)
		{
			boolean found = true;
			while (found)
			{
				found = false;
				
				for (State s = firstState; s != null; s = s.next)
				{
					if (s.config.findLookaheads())
					{
						found = true;
					}
				}
			}
		}
		
		private static void buildReduceActions(State firstState, Terminal[] terminals)
		{
			Action.Reduce.Builder reduceActionBuilder = new Action.Reduce.Builder(terminals); 
			
			for ( State state = firstState; state != null; state = state.next )
			{
				reduceActionBuilder.set(state);
				
				for ( Item item = state.config.getFirstItem(); item != null; item = item.next )
				{
					if ( item.isDotAfterLastSymbol() )
					{
						item.accept(reduceActionBuilder);
					}
				}
			}
		}
	}
}
