package beaver.lexer;

class SCCFinder
{
	DFAState[] stack;
	int        top;

	SCCFinder(int nStates)
	{
		stack = new DFAState[nStates];
	}

	void traverse(DFAState st)
	{
		stack[top++] = st;
		int d = st.depth = top;

		for (DFAStateTransition tr = st.firstTransition; tr != null; tr = tr.next)
		{
			DFAState dest = tr.toState;

			if (dest.depth == 0)
			{
				traverse(dest);
			}
			if (dest.depth < st.depth)
			{
				st.depth = dest.depth;
			}
		}

		if (st.depth == d)
		{
			do
			{
				stack[--top].depth = Integer.MAX_VALUE;
				stack[top].link = st;
			}
			while (stack[top] != st);
		}
	}

	static void calcDepth(DFAState start)
	{
		for (DFAState st = start.next; st != null; st = st.next)
		{
			if (!st.isInNonTrivialSCC())
			{
				st.link = null;
			}
		}
		for (DFAState st = start; st != null; st = st.next)
		{
			st.depth = Integer.MAX_VALUE;
		}
		for (DFAState st = start; st != null; st = st.next)
		{
			st.calcMaxDepth();
		}
	}
}
