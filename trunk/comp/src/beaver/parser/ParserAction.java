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
	}
}
