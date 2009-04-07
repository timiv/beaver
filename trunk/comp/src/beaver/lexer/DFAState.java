package beaver.lexer;


class DFAState
{
	DFAState           next;

	Accept             accept;
	boolean            isPreCtx;
	DFAStateTransition firstTransition;

	int                nTransitions;
	int                id;
	int                depth;
	DFAState           link;
	NFANode[]          kernel;

	DFAState(NFANode[] kernel, boolean isPreCtx)
	{
		this.kernel = kernel;
		this.isPreCtx = isPreCtx;
	}

	boolean isSameAs(DFAState st)
	{
		if (nTransitions != st.nTransitions)
			return false;
		for (DFAStateTransition tr = firstTransition; tr != null; tr = tr.next)
		{
			if (!st.hasTransition(tr))
				return false;
		}
		return true;
	}
	
	void addTransition(CharRange onChars, DFAState toState)
	{
		firstTransition = new DFAStateTransition(onChars, toState, firstTransition);
		nTransitions++;
	}

	boolean hasTransition(DFAStateTransition t)
	{
		for (DFAStateTransition tr = firstTransition; tr != null; tr = tr.next)
		{
			if (tr.equals(t))
				return true;
		}
		return false;
	}

	boolean isInNonTrivialSCC()
	{
		if (link != this)
			return true;

		for (DFAStateTransition tr = firstTransition; tr != null; tr = tr.next)
		{
			if (tr.toState.link == this)
				return true;
		}
		return false;
	}

	void calcMaxDepth()
	{
		if (depth == Integer.MAX_VALUE)
		{
			int mm = 0;

			for (DFAStateTransition tr = firstTransition; tr != null; tr = tr.next)
			{
				DFAState dest = tr.toState;

				int m = 1;

				if (dest.link == null)
				{
					dest.calcMaxDepth();
					m += dest.depth;
				}

				if (m > mm)
				{
					mm = m;
				}
			}
			depth = mm;
		}
	}

	void compactTransitions()
	{
		for (DFAStateTransition tr = firstTransition; tr != null; tr = tr.next)
		{
			CharRange expandedRange = null; 
			
			for (DFAStateTransition test = tr.next, prev = tr; test != null; test = test.next)
			{
				if (test.toState == tr.toState)
				{
					if (expandedRange == null)
					{
						expandedRange = new CharRange();
						expandedRange.add(tr.onChars);
						tr.onChars = expandedRange;
					}
					tr.onChars.add(test.onChars);
					prev.next = test.next;
					nTransitions--;
				}
				else
				{
					prev = test;
				}
			}
		}
	}

	void markReachable()
	{
		if (id >= 0)
		{
			id = -1;

			for (DFAStateTransition tr = firstTransition; tr != null; tr = tr.next)
			{
				tr.toState.markReachable();
			}
		}
	}
}
