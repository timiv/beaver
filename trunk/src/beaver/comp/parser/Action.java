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
 *
 */
public abstract class Action extends DLList.Element
{
	Symbol lookahead;
	
	Action(Symbol lookahead)
	{
		this.lookahead = lookahead;
	}
	
	abstract int getCode();
	
	static class List extends DLList
	{
		Action getFirstAction()
		{
			return (Action) super.getFirstElement();
		}
		
		Action next(Action current)
		{
			return (Action) super.getNextElement(current);
		}
	}
	
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
		int getCode()
		{
			return dest.id;
		}
	}
	
	static class Reduce extends Action
	{
		Production prod;
		
		Reduce(Symbol lookahead, Production prod)
		{
			super(lookahead);
			this.prod = prod; 
		}
		
		/**
		 * "reduce" action code is an inverse of the rule id that will be used to
		 * reduce symbols on a stack to a new non-terminal.
		 */
		int getCode()
		{
			return ~prod.id;
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
		
		void resolveConflicts(Action.List shiftActions, Action.List reduceActions)
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
			
			for ( Action reduce1 = reduceActions.getFirstAction(); reduce1 != null; reduce1 = reduceActions.next(reduce1) )
			{
				for ( Action reduce2 = reduceActions.next(reduce1); reduce2 != null; reduce2 = reduceActions.next(reduce2) )
				{
					if ( reduce1.lookahead == reduce2.lookahead )
					{
						Action remove = resolveConflict((Action.Reduce) reduce1, (Action.Reduce) reduce2);
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
}
