package beaver.parser;

abstract class ParserAction
{
	Symbol       lookahead;
	ParserAction next;

	ParserAction(Symbol lookahead)
	{
		this.lookahead = lookahead;
	}

	abstract int getId();
	abstract String descr();
	
	public String toString()
	{
		return lookahead + " -> ?";
	}

	static class Shift extends ParserAction
	{
		ParserState dest;

		Shift(Symbol lookahead, ParserState shiftTo)
		{
			super(lookahead);
			this.dest = shiftTo;
		}

		/**
		 * "shift" action ID is a state id to shift to, i.e. it's a positive number in the range
		 * [1..NumberOfStates]
		 */
		int getId()
		{
			return dest.id;
		}
		
		String descr()
		{
			return "shift";
		}
		
		public String toString()
		{
			return lookahead + " -> " + dest.id;
		}
	}

	static class Reduce extends ParserAction
	{
		Production production;

		Reduce(Symbol lookahead, Production prod)
		{
			super(lookahead);
			this.production = prod;
		}

		/**
		 * "reduce" action ID is an inverse of the rule id that will be used to reduce symbols on a
		 * stack to a new non-terminal.
		 */
		int getId()
		{
			return ~production.id;
		}
		
		String descr()
		{
			return "reduce " + production;
		}
		
		public String toString()
		{
			return lookahead + " -> " + production;
		}
	}
	
	static class Accept extends ParserAction
	{
		Grammar grammar;
		
		Accept(Grammar grammar)
		{
			super(grammar.getGoal());
			this.grammar = grammar;
		}
		
		int getId()
		{
			return grammar.getAcceptActionId();
		}

		String descr()
		{
			return "accept " + grammar.getGoal();
		}
		
		public String toString()
		{
			return lookahead + " -> ACCEPT";
		}
	}

	static class Conflict
	{
		Conflict     next;

		ParserState  state;
		ParserAction action1;
		ParserAction action2;

		Conflict(ParserState state, ParserAction action1, ParserAction action2, Conflict last)
		{
			this.state = state;
			this.action1 = action1;
			this.action2 = action2;
			this.next = last;
		}
		
		public String toString()
		{
			StringBuffer text = new StringBuffer(100);
	    	text.append("on ").append(action1.lookahead).append(": ").append(action1.descr()).append(" or ").append(action2.descr());
	    	return text.toString();
		}
	}
}
