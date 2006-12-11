/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */

package beaver.comp.parser;

import java.util.Arrays;

import beaver.util.BitSet;
import beaver.util.IList;

/**
 * @author Alexander Demenchuk
 *
 */
public abstract class Action extends IList.Element
{
	Symbol lookahead;
	
	Action(Symbol lookahead)
	{
		this.lookahead = lookahead;
	}
	
	abstract short getCode();
	
	static class Shift extends Action
	{
		State dest;
		
		Shift(Symbol lookahead, State shiftTo)
		{
			super(lookahead);
			this.dest = shiftTo;
		}
		/**
		 * "shift" action code is a state id to shift to, i.e. it's a positive
		 * number in the range [1..NumberOfStates]
		 */
		short getCode()
		{
			return (short) dest.id;
		}
	}
	
	static class Reduce extends Action
	{
		Production prod;
		boolean defaultAction;
		
		Reduce(Symbol lookahead, Production prod)
		{
			super(lookahead);
			this.prod = prod; 
		}
		
		/**
		 * "reduce" action code is an inverse of the rule id that will be used to
		 * reduce symbols on a stack to a new non-terminal.
		 */
		short getCode()
		{
			return (short) ~prod.id;
		}
		
		static class Builder implements BitSet.BitVisitor
		{
			private Terminal[] terminals; 
			private State state;
			private Production prod;

			Builder(Terminal[] terminals)
			{
				this.terminals = terminals;
			}
			
			void set(State s)
			{
				state = s;
			}
			
			void set(Production p)
			{
				prod = p;
			}
			
			public void visit(int i)
            {
	            state.add(new Reduce(terminals[i], prod));
            }
		}
	}
	
	public static class ConflictResolver
	{
		int srCount;
		int rrCount;
		
		public boolean resolveConflicts(State firstState)
		{
			for ( State st = firstState; st != null; st = st.next )
			{
				resolveConflicts(st.shiftActions, st.reduceActions);
			}
			return srCount == 0 && rrCount == 0;
		}
		
		void resolveConflicts(IList shiftActions, IList reduceActions)
		{
			if (reduceActions == null)
				return;
			
			if (shiftActions != null)
			{
				for ( Action.Shift shift = (Action.Shift) shiftActions.first(); shift != null; shift = (Action.Shift) shift.next() )
				{
					for ( Action.Reduce reduce = (Action.Reduce) reduceActions.first(); reduce != null; reduce = (Action.Reduce) reduce.next() )
					{
						if ( shift.lookahead == reduce.lookahead )
						{
							Action remove = resolveConflict(shift, reduce);
							if (remove == reduce)
							{
								reduceActions.remove(reduce);
								continue;
							}
							if (remove == shift)
							{
								shiftActions.remove(shift);
								break;
							}
							srCount++;
							System.err.println("SR conflict");
						}
					}
				}
			}
			
			for ( Action.Reduce reduce1 = (Action.Reduce) reduceActions.first(); reduce1 != null; reduce1 = (Action.Reduce) reduce1.next() )
			{
				for ( Action.Reduce reduce2 = (Action.Reduce) reduce1.next(); reduce2 != null; reduce2 = (Action.Reduce) reduce2.next() )
				{
					if ( reduce1.lookahead == reduce2.lookahead )
					{
						Action remove = resolveConflict(reduce1, reduce2);
						if (remove == reduce2)
						{
							reduceActions.remove(reduce2);
							continue;
						}
						if (remove == reduce1)
						{
							reduceActions.remove(reduce1);
							break;
						}
						rrCount++;
						System.err.println("RR conflict");
					}
				}
			}
			
		}

		private Action resolveConflict(Action.Shift shift, Action.Reduce reduce)
		{
			if ( shift.lookahead instanceof Terminal )
			{
				Terminal shiftLookahead = (Terminal) shift.lookahead; 
				
				if ( shiftLookahead.precedence > reduce.prod.precedence )
					return reduce;

				if ( reduce.prod.precedence > shiftLookahead.precedence )
					return shift;
				
				switch ( shiftLookahead.associativity )
				{
					case 'L':
						return shift;
						
					case 'R':
						return reduce;
				}
			}
			return null;
		}

		private Action resolveConflict(Action.Reduce reduce1, Action.Reduce reduce2)
		{
			if ( reduce1.prod.precedence > reduce2.prod.precedence )
				return reduce2;
			
			if ( reduce2.prod.precedence > reduce1.prod.precedence )
				return reduce1;
			
			return null;
		}
	}

	public static class Compressor
	{
		private int[] counters;
		private int numSymbols;
		
		public Compressor(Grammar grammar)
		{
			counters = new int[grammar.productions.length];
			numSymbols = grammar.terminals.length + grammar.nonterminals.length + 1;
		}
		
		public void compress(State firstState)
		{
			for ( State st = firstState; st != null; st = st.next )
			{
				if ( st.reduceActions != null && st.reduceActions.length() > 1 )
				{
					Arrays.fill(counters, 0);
					int maxCount = 0;
					Production defaultRule = null;
					for ( Action.Reduce act = (Action.Reduce) st.reduceActions.first(); act != null; act = (Action.Reduce) act.next() )
					{
						int c = ++counters[act.prod.id];
						if ( maxCount < c )
						{
							maxCount = c;
							defaultRule = act.prod;
						}
					}
					if ( maxCount > 1 )
					{
						st.defaultReduceRule = defaultRule;
						st.defaultReduceRuleLookaheads = new BitSet(numSymbols);
						for ( Action.Reduce act = (Action.Reduce) st.reduceActions.first(); act != null; act = (Action.Reduce) act.next() )
						{
							if ( act.prod == defaultRule )
							{
								act.defaultAction = true;
								st.defaultReduceRuleLookaheads.add(act.lookahead.id);
							}
						}
					}
				}
			}
		}
	}
}
