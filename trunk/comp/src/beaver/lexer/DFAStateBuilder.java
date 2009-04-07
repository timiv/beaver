package beaver.lexer;

class DFAStateBuilder
{
	DFAState first;
	DFAState last;
	int      nStates;
	DFAState work;
	
	DFAState getStartState()
	{
		return first;
	}
	
	int getNumberOfStates()
	{
		return nStates;
	}
	
	void buildDFAStates(NFA nfa)
	{
		createStates(nfa);
		removeUnreachable();
		compactTransitions();
		minimize();
		compactTransitions();
		findSCC();
		assignStateIds();
	}

	DFAState addState(DFAClosure closure)
	{
		DFAState state = new DFAState(closure.getKernel(), closure.isPreCtx());

		nStates++;

		if (last != null)
			last = last.next = state;
		else
			last = state;

		state.link = work;
		work = state;

		return state;
	}

	DFAState getWorkState()
	{
		DFAState st = work;
		work = st.link;
		st.link = null;
		return st;
	}

	DFAState findState(DFAClosure closure)
	{
		for (DFAState state = first; state != null; state = state.next)
		{
			if (closure.kernelMatches(state.kernel))
			{
				return state;
			}
		}
		return null;
	}

	DFAState getState(DFAClosure closure)
	{
		DFAState state = findState(closure);
		if (state == null)
		{
			state = addState(closure);
		}
		return state;
	}

	void createStates(final NFA nfa)
    {
	    final NFAEdgeCollector edgeCollector = new NFAEdgeCollector();
		final DFAClosure closure = new DFAClosure();

		last = null;
		nStates = 0;
		work = null;

		nfa.start.accept(closure);
		first = addState(closure);

		while (work != null)
		{
			final DFAState st = getWorkState();

			for (int i = 0; i < st.kernel.length; i++)
			{
				st.kernel[i].accept(edgeCollector);
			}
			st.accept = edgeCollector.accept;

			edgeCollector.transitions.accept(new CharMap.EntryVisitor()
			{
				public void visit(char key, Object value)
				{
					closure.reset();
					((NFANode.Array) value).accept(closure);

					st.addTransition(((CharClass) nfa.cset.classes.get(key)).range, getState(closure));
				}
			});
			edgeCollector.reset();
		}
    }

	void assignStateIds()
    {
	    int id = 0;
		for (DFAState st = first; st != null; st = st.next)
		{
			st.id = id++;
		}
		if (id != nStates)
			throw new IllegalStateException("Wrong number of states");
    }

	void removeUnreachable()
	{
		first.markReachable();

		for (DFAState st = first.next, ps = first; st != null; st = st.next)
		{
			if (st.id >= 0) // not marked
			{
				ps.next = st.next;
				nStates--;
			}
			else
			{
				ps = st;
			}
		}
	}

	void compactTransitions()
	{
		for (DFAState s = first; s != null; s = s.next)
		{
			s.compactTransitions();
		}
	}

	void minimize()
	{
		// create initial partition
		DFAState[] sets = new DFAState[nStates];

		int lastSet = -1;
		for (DFAState st = first; st != null; st = st.next)
		{
			if (st.accept != null && st.firstTransition == null)
			{
				int i = 0;
				for (; i <= lastSet; i++)
				{
					DFAState x = sets[i];
					if (st.accept == x.accept)
						break;
				}
				st.id = i;
				st.link = sets[i];
				sets[i] = st;

				if (lastSet < i)
				{
					lastSet = i;
				}
			}
		}

		int firstSet = lastSet + 1;
		for (DFAState st = first; st != null; st = st.next)
		{
			if (st.accept != null && st.firstTransition != null)
			{
				int i = firstSet;
				for (; i <= lastSet; i++)
				{
					DFAState x = sets[i];
					if (st.accept == x.accept)
						break;
				}
				st.id = i;
				st.link = sets[i];
				sets[i] = st;

				if (lastSet < i)
				{
					lastSet = i;
				}
			}
		}

		int nextSet = lastSet + 1;
		for (DFAState st = first; st != null; st = st.next)
		{
			if (st.accept == null)
			{
				st.id = nextSet;
				st.link = sets[nextSet];
				sets[nextSet] = st;

				if (lastSet < nextSet)
				{
					lastSet = nextSet;
				}
			}
		}

		// partition existing sets into subsets
		while (lastSet == nextSet)
		{
			for (int i = firstSet; i <= lastSet; i++)
			{
				nextSet = lastSet + 1;

				DFAState ref = sets[i], st = ref.link, ps = ref;
				while (st != null)
				{
					if (!st.isSameAs(ref))
					{
						lastSet = nextSet;

						ps.link = st.link;
						st.id = nextSet;
						st.link = sets[nextSet];
						sets[nextSet] = st;

						st = ps.link;
					}
					else
					{
						ps = st;
						st = st.link;
					}
				}
			}
		}

		// coalesce states of each subset
		for (int i = 0; i <= lastSet; i++)
		{
			for (DFAStateTransition tr = sets[i].firstTransition; tr != null; tr = tr.next)
			{
				tr.toState = sets[tr.toState.id];
			}
		}

		// mark "extra" states
		for (int i = 0; i <= lastSet; i++)
		{
			for (DFAState st = sets[i].link; st != null; st = st.link)
			{
				st.id = -1;
			}
		}

		// remove "extra" states
		for (DFAState ps = first, st = ps.next; st != null; st = st.next)
		{
			if (st.id < 0)
			{
				ps.next = st.next;
				nStates--;
			}
			else
			{
				ps = st;
			}
		}
	}

	void findSCC()
	{
		SCCFinder scc = new SCCFinder(nStates);
		for (DFAState st = first; st != null; st = st.next)
		{
			if (st.depth == 0)
			{
				scc.traverse(st);
			}
		}
		SCCFinder.calcDepth(first);
	}
}
