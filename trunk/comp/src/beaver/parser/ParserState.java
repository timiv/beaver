package beaver.parser;

import java.util.Comparator;

class ParserState
{
	int          id;
	ItemSet      config;
	ParserAction shift;
	ParserAction reduce;
	ParserAction accept;
	ParserAction defaultReduce;
	/**
	 * Total number of parser actions at this state
	 */
	int          numActions;

	ParserState  next;

	ParserState(ItemSet kernel)
	{
		config = kernel;
	}

	public String toString()
	{
		StringBuffer buf = new StringBuffer(100);
		buf.append(id).append(":\n");
		if (shift != null)
		{
			buf.append("   shift:\n");
			for (ParserAction action = shift; action != null; action = action.next)
			{
				String symstr = action.lookahead.toString();
				int strlen = symstr.length();
				for (int i = strlen; i < 10; i++)
				{
					buf.append(' ');
				}
				buf.append(action).append('\n');
			}
		}
		if (reduce != null)
		{
			buf.append("   reduce:\n");
			for (ParserAction action = reduce; action != null; action = action.next)
			{
				String symstr = action.lookahead.toString();
				int strlen = symstr.length();
				for (int i = strlen; i < 10; i++)
				{
					buf.append(' ');
				}
				buf.append(action).append('\n');
			}
		}
		if (defaultReduce != null)
		{
			buf.append("   default:\n");
			String symstr = defaultReduce.lookahead.toString();
			int strlen = symstr.length();
			for (int i = strlen; i < 10; i++)
			{
				buf.append(' ');
			}
			buf.append(defaultReduce).append('\n');
		}
		if (accept != null)
		{
			buf.append("   accept:\n");
			String symstr = accept.lookahead.toString();
			int strlen = symstr.length();
			for (int i = strlen; i < 10; i++)
			{
				buf.append(' ');
			}
			buf.append(accept).append('\n');
		}
		return buf.toString();
	}

	void add(ParserAction.Shift action)
	{
		action.next = shift;
		shift = action;
		numActions++;
		action.lookahead.numStates++;
	}

	void add(ParserAction.Reduce action)
	{
		action.next = reduce;
		reduce = action;
		numActions++;
		action.lookahead.numStates++;
	}

	ParserAction.Conflict resolveConflicts(ParserAction.Conflict last)
	{
		last = resolveShiftReduceConflicts(last);
		return resolveReduceReduceConflicts(last);
	}

	void createDefaultReduceAction(int productionId)
	{
		ParserAction.Reduce removed = null;
		for (ParserAction.Reduce reduce = (ParserAction.Reduce) this.reduce, prevReduce = null; reduce != null; reduce = (ParserAction.Reduce) reduce.next)
		{
			if (reduce.production.id == productionId)
			{
				removeReduceAction(removed = reduce, prevReduce);
				continue;
			}
			prevReduce = reduce;
		}
		if (removed != null)
		{
			removed.next = null;
		}
		defaultReduce = removed;
	}

	private ParserAction.Conflict resolveShiftReduceConflicts(ParserAction.Conflict last)
	{
		if (reduce != null)
		{
		nextShift: 
			for (ParserAction.Shift shift = (ParserAction.Shift) this.shift, prevShift = null; shift != null; shift = (ParserAction.Shift) shift.next)
			{
				for (ParserAction.Reduce reduce = (ParserAction.Reduce) this.reduce, prevReduce = null; reduce != null; reduce = (ParserAction.Reduce) reduce.next)
				{
					if (shift.lookahead == reduce.lookahead)
					{
						ParserAction remove = resolveConflict(shift, reduce);
						if (remove == shift)
						{
							removeShiftAction(shift, prevShift);
							continue nextShift;
						}
						if (remove == reduce)
						{
							removeReduceAction(reduce, prevReduce);
							continue;
						}
						last = new ParserAction.Conflict(this, shift, reduce, last);
					}
					prevReduce = (ParserAction.Reduce) reduce;
				}
				prevShift = (ParserAction.Shift) shift;
			}
		}
		return last;
	}

	private ParserAction.Conflict resolveReduceReduceConflicts(ParserAction.Conflict last)
	{
		if (reduce != null)
		{
		nextReduce:
			for (ParserAction.Reduce reduce1 = (ParserAction.Reduce) this.reduce, prevReduce1 = null; reduce1 != null; reduce1 = (ParserAction.Reduce) reduce1.next)
			{
				for (ParserAction.Reduce reduce2 = (ParserAction.Reduce) reduce1.next, prevReduce2 = reduce1; reduce2 != null; reduce2 = (ParserAction.Reduce) reduce2.next)
				{
					if (reduce1.lookahead == reduce2.lookahead)
					{
						ParserAction remove = resolveConflict(reduce1, reduce2);
						if (remove == reduce1)
						{
							removeReduceAction(reduce1, prevReduce1);
							continue nextReduce;
						}
						if (remove == reduce2)
						{
							removeReduceAction(reduce2, prevReduce2);
							continue;
						}
						last = new ParserAction.Conflict(this, reduce1, reduce2, last);
					}
					prevReduce2 = (ParserAction.Reduce) reduce2;
				}
				prevReduce1 = (ParserAction.Reduce) reduce1;
			}
		}
		return last;
	}

	private void removeShiftAction(ParserAction action, ParserAction prevAction)
	{
		if (prevAction == null)
		{
			shift = action.next;
		}
		else
		{
			prevAction.next = action.next;
		}
		numActions--;
		action.lookahead.numStates--;
	}

	private void removeReduceAction(ParserAction action, ParserAction prevAction)
	{
		if (prevAction == null)
		{
			reduce = action.next;
		}
		else
		{
			prevAction.next = action.next;
		}
		numActions--;
		action.lookahead.numStates--;
	}

	private static ParserAction resolveConflict(ParserAction.Shift shift, ParserAction.Reduce reduce)
	{
		if (shift.lookahead instanceof Terminal)
		{
			Terminal lookahead = (Terminal) shift.lookahead;

			if (lookahead.precedence > reduce.production.precedence)
				return reduce;

			if (reduce.production.precedence > lookahead.precedence)
				return shift;

			switch (lookahead.associativity)
			{
				case 'L':
				{
					return shift;
				}
				case 'R':
				{
					return reduce;
				}
			}
		}
		return null;
	}

	private static ParserAction resolveConflict(ParserAction.Reduce reduce1, ParserAction.Reduce reduce2)
	{
		if (reduce1.production.precedence > reduce2.production.precedence)
			return reduce2;

		if (reduce2.production.precedence > reduce1.production.precedence)
			return reduce1;

		return null;
	}

	private static int countStates(ParserState state)
	{
		int n = 0;
		for (; state != null; state = state.next)
		{
			n++;
		}
		return n;
	}

	static ParserState[] toArray(ParserState state)
	{
		ParserState[] states = new ParserState[countStates(state)];
		int i = 0;
		for (; state != null; state = state.next)
		{
			states[i++] = state;
		}
		return states;
	}

	static Comparator CMP_NUM_ACTIONS = new Comparator()
	                                  {
		                                  public int compare(Object obj1, Object obj2)
		                                  {
			                                  return ((ParserState) obj2).numActions - ((ParserState) obj1).numActions;
			                                  ;
		                                  }
	                                  };
}
