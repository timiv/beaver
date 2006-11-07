/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */

package beaver.comp.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Demenchuk
 *
 */
public class State
{
	State       next;
	int         id;
	ItemSet     config;
	Action.List shiftActions;
	Action.List reduceActions;
	
	State(int id, ItemSet core)
	{
		this.id = id;
		config = core;
	}
	
	void add(Action.Shift act)
	{
		if ( shiftActions == null )
			shiftActions = new Action.List();
		shiftActions.add(act);
	}
	
	void add(Action.Reduce act)
	{
		if ( reduceActions == null )
			reduceActions = new Action.List();
		reduceActions.add(act);
	}

	void resolveActionConflicts()
	{
		if (reduceActions == null)
			return;
		
		if (shiftActions != null)
		{
			for ( Action shift = shiftActions.getFirstAction(); shift != null; shift = shiftActions.next(shift) )
			{
				for ( Action reduce = reduceActions.getFirstAction(); reduce != null; reduce = reduceActions.next(reduce) )
				{
					if ( shift.lookahead == reduce.lookahead )
					{
						Action remove = resolveConflict((Action.Shift) shift, (Action.Reduce) reduce);
						if (remove == reduce)
							continue;
						else if (remove == shift)
							break;
						else
						{
							System.out.println("SR conflict");
						}
					}
				}
			}
		}
		
		for ( Action reduce1 = reduceActions.getFirstAction(); reduce1 != null; reduce1 = reduceActions.next(reduce1) )
		{
			for ( Action reduce2 = reduceActions.next(reduce1); reduce2 != null; reduce2 = reduceActions.next(reduce2) )
			{
				if ( reduce1.lookahead == reduce2.lookahead )
				{
					Action remove = resolveConflict((Action.Reduce) reduce1, (Action.Reduce) reduce2);
					if (remove == reduce2)
						continue;
					else if (remove == reduce1)
						break;
					else
					{
						System.out.println("RR conflict");
					}
				}
			}
		}
		
	}

	private Action resolveConflict(Action.Shift shift, Action.Reduce reduce)
	{
		
		return null;
	}

	private Action resolveConflict(Action.Reduce reduce1, Action.Reduce reduce2)
	{
		
		return null;
	}
	
	public String toString()
	{
		StringBuffer text = new StringBuffer();
		text.append(id).append(':').append('\n').append(config);
		return text.toString();
	}
	
	public static class Builder
	{
		private Map             states     = new HashMap();
		private ItemSet.Builder setBuilder = new ItemSet.Builder();
		private State           last;
		
		public State createStates(Grammar grammar)
		{
			NonTerminal goal      = grammar.productions[0].lhs;
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
			resolveConflicts(firstState);
			
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
		
		private static void resolveConflicts(State firstState)
		{
			for ( State state = firstState; state != null; state = state.next )
			{
				state.resolveActionConflicts();
			}
		}
	}
}
