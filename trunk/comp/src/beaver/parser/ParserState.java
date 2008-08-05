package beaver.parser;

class ParserState
{
	int          id;
	ItemSet      config;
	ParserAction shift;
	ParserAction reduce;

	ParserState  next;

	ParserState(ItemSet kernel)
	{
		config = kernel;
	}

	void add(ParserAction.Shift action)
	{
		action.next = shift;
		shift = action;
	}

	void add(ParserAction.Reduce action)
	{
		action.next = reduce;
		reduce = action;
	}
	
	ParserAction.Conflict resolveConflicts(ParserAction.Conflict last)
	{
		last = resolveShiftReduceConflicts(last);
		return resolveReduceReduceConflicts(last); 
	}

	private ParserAction.Conflict resolveShiftReduceConflicts(ParserAction.Conflict last)
	{
		if (reduce != null)
		{
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
							break;
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
							break;
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
}
