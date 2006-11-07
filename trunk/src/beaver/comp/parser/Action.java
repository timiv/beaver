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
}
