/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp.lexer;

import java.util.ArrayList;

import beaver.util.CharMap;

class DFA
{
	State start;
	int   nStates;
	int   maxFill;

	DFA(NFA nfa, CharSet cset)
	{
		Builder builder = new Builder();
		start = builder.buildDFA(nfa, cset);
		nStates = builder.getNumStates();

		removeUnreachable();
		compactTransitions();
		minimize();
		compactTransitions();
		findSCCs();

		int id = 0;
		maxFill = 1;
		for (State st = start; st != null; st = st.next)
		{
			if (maxFill < st.depth)
			{
				maxFill = st.depth;
			}
			st.sid = id++;
		}
		if (id != nStates)
			throw new IllegalStateException("Wrong number of states");
	}

	private void removeUnreachable()
	{
		start.markReachable();

		for (State st = start.next, ps = start; st != null; st = st.next)
		{
			if (st.sid == 0) // not marked
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

	private void compactTransitions()
	{
		for (State s = start; s != null; s = s.next)
		{
			s.compactTransitions();
		}
	}

	private void minimize()
	{
		// create initial partition
		State[] sets = new State[nStates];
		int lastSet = -1;
		for (State st = start; st != null; st = st.next)
		{
			if (st.accept != 0 && st.move == null)
			{
				int i = 0;
				for (; i <= lastSet; i++)
				{
					State x = sets[i];
					if (st.accept == x.accept)
						break;
				}
				st.sid = i;
				st.link = sets[i];
				sets[i] = st;

				if (lastSet < i)
				{
					lastSet = i;
				}
			}
		}
		int firstSet = lastSet + 1;
		for (State st = start; st != null; st = st.next)
		{
			if (st.accept != 0 && st.move != null)
			{
				int i = firstSet;
				for (; i <= lastSet; i++)
				{
					State x = sets[i];
					if (st.accept == x.accept)
						break;
				}
				st.sid = i;
				st.link = sets[i];
				sets[i] = st;

				if (lastSet < i)
				{
					lastSet = i;
				}
			}
		}
		int nextSet = lastSet + 1;
		for (State st = start; st != null; st = st.next)
		{
			if (st.accept == 0)
			{
				st.sid = nextSet;
				st.link = sets[nextSet];
				sets[nextSet] = st;

				if (lastSet < nextSet)
				{
					lastSet = nextSet;
				}
			}
		}

		// partion existing sets into subsets
		while (lastSet == nextSet)
		{
			for (int i = firstSet; i <= lastSet; i++)
			{
				nextSet = lastSet + 1;

				State ref = sets[i], st = ref.link, ps = ref;
				while (st != null)
				{
					if (st.isDistinguishableFrom(ref))
					{
						lastSet = nextSet;

						ps.link = st.link;
						st.sid = nextSet;
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
			for (State.Transition t = sets[i].move; t != null; t = t.next)
			{
				t.dest = sets[t.dest.sid];
			}
		}
		
		// mark "extra" states
		for (int i = 0; i <= lastSet; i++)
		{
			for ( State st = sets[i].link; st != null; st = st.link )
			{
				st.sid = -1;
			}
		}
		
		// remove "extra" states
		for (State ps = start, st = ps.next; st != null; st = st.next)
		{
			if ( st.sid < 0 )
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

	private void findSCCs()
	{
		SCCBuilder scc = new SCCBuilder(nStates);
		for (State st = start; st != null; st = st.next)
		{
			if (st.depth == 0)
			{
				scc.traverse(st);
			}
		}
		SCCBuilder.calcDepth(start);
	}

	static class State
	{
		State      next;
		NFA.Node[] kernel;
		boolean    isPreCtx;
		int        accept;
		String     eventName;
		int        eventId;
		Transition move;
		int        nTransitions;

		int        sid;
		int        depth;
		State      link;

		State(Closure closure)
		{
			kernel = closure.getKernel();
			isPreCtx = closure.isPreCtx();
			eventId = -1;
		}

		void markReachable()
		{
			if (sid == 0)
			{
				sid = -1;

				for (Transition t = move; t != null; t = t.next)
				{
					t.dest.markReachable();
					++nTransitions;
				}
			}
		}

		boolean isDistinguishableFrom(State st)
		{
			if (this.nTransitions != st.nTransitions)
				return true;
		compareGoTos: 
			for (Transition t = move; t != null; t = t.next)
			{
				for (Transition x = st.move; x != null; x = x.next)
				{
					if (t.equals(x))
						continue compareGoTos;
				}
				return true;
			}
			return false;
		}

		boolean isInNonTrivialSCC()
		{
			if (link != this)
				return true;

			for (Transition t = move; t != null; t = t.next)
			{
				if (t.dest.link == this)
					return true;
			}
			return false;
		}

		void calcMaxDist()
		{
			if (depth == Integer.MAX_VALUE)
			{
				int mm = 0;

				for (Transition t = move; t != null; t = t.next)
				{
					State to = t.dest;
					int m = 1;

					if (to.link == null)
					{
						to.calcMaxDist();
						m += to.depth;
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
			for (Transition t = move; t != null; t = t.next)
			{
				CharClass.Span newSpan = null;
				for (Transition n = t.next, p = t; n != null; n = n.next)
				{
					if (n.dest == t.dest)
					{
						newSpan = CharClass.Span.join(n.span, newSpan);
						p.next = n.next;
					}
					else
					{
						p = n;
					}
				}
				if (newSpan != null)
				{
					newSpan = CharClass.Span.join(t.span, newSpan);
					CharClass.Span.merge(newSpan);
					t.span = newSpan;
					t.nSpans = 0;
				}
			}
		}

		static class Transition
		{
			CharClass.Span span;
			int            nSpans;
			State          dest;
			Transition     next;

			Transition(CharClass.Span span, State dest, Transition next)
			{
				this.span = span;
				this.dest = dest;
				this.next = next;
			}

			int getNumSpans()
			{
				if (nSpans == 0)
				{
					int n = 0;
					for (CharClass.Span s = span; s != null; s = s.next)
					{
						n++;
					}
					nSpans = n;
				}
				return nSpans;
			}

			boolean equals(Transition t)
			{
				if (this.dest.sid != t.dest.sid || this.getNumSpans() != t.getNumSpans())
					return false;
				compareSpans: for (CharClass.Span s = span; s != null; s = s.next)
				{
					for (CharClass.Span x = t.span; x != null; x = x.next)
					{
						if (x.equals(s))
							continue compareSpans;
					}
					return false;
				}
				return true;
			}
		}
	}

	static class SCCBuilder
	{
		State[] stack;
		int     top;

		SCCBuilder(int nStates)
		{
			stack = new State[nStates];
		}

		void traverse(State st)
		{
			stack[top++] = st;
			int d = st.depth = top;

			for (State.Transition tr = st.move; tr != null; tr = tr.next)
			{
				State to = tr.dest;
				if (to.depth == 0)
				{
					this.traverse(to);
				}
				if (to.depth < st.depth)
				{
					st.depth = to.depth;
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

		static void calcDepth(State start)
		{
			for (State st = start.next; st != null; st = st.next)
			{
				if (!st.isInNonTrivialSCC())
				{
					st.link = null;
				}
			}
			for (State st = start; st != null; st = st.next)
			{
				st.depth = Integer.MAX_VALUE;
			}
			for (State st = start; st != null; st = st.next)
			{
				st.calcMaxDist();
			}
		}
	}

	static class Builder
	{
		private State     start;
		private int       nStates;
		private State     last;
		private ArrayList work;

		State buildDFA(NFA nfa, CharSet cset)
		{
			nStates = 0;
			last = null;
			work = new ArrayList();

			Closure closure = new Closure();
			nfa.start.accept(closure);
			start = addState(closure);

			TransitionBuilder transitionBuilder = new TransitionBuilder(cset, closure);
			Move move = new Move();

			while (!work.isEmpty())
			{
				State state = (State) work.remove(work.size() - 1);

				for (int i = 0; i < state.kernel.length; i++)
				{
					state.kernel[i].accept(move);
				}
				state.accept = move.accept;
				state.eventName = move.eventName;

				transitionBuilder.fromState = state;
				move.transitions.forEachEntryAccept(transitionBuilder);

				move.reset();
			}
			return start;
		}

		int getNumStates()
		{
			return nStates;
		}

		private State getState(Closure closure)
		{
			State state = findState(closure);
			if (state == null)
			{
				state = addState(closure);
			}
			return state;
		}

		private State addState(Closure closure)
		{
			State state = new State(closure);

			nStates++;

			if (last != null)
				last = last.next = state;
			else
				last = state;
			work.add(state);

			return state;
		}

		private State findState(Closure closure)
		{
			for (State state = start; state != null; state = state.next)
			{
				if (closure.kernelMatches(state.kernel))
				{
					return state;
				}
			}
			return null;
		}

		class TransitionBuilder implements CharMap.EntryVisitor
		{
			State           fromState;

			private CharSet cset;
			private Closure closure;

			TransitionBuilder(CharSet cset, Closure closure)
			{
				this.cset = cset;
				this.closure = closure;
			}

			public void visit(char c, Object value)
			{
				closure.reset();
				((NFA.NodeArray) value).forEachNodeAccept(closure);

				fromState.move = new State.Transition(cset.getCharClass(c).span, getState(closure), fromState.move);
			}
		}
	}

	static class Closure implements NFA.NodeVisitor
	{
		private NFA.NodeArray kernel = new NFA.NodeArray();
		private boolean       ctxNodeFound;

		public void visit(NFA.Char node)
		{
			if (!node.marked)
			{
				if (node.isCtxStart())
				{
					ctxNodeFound = true;
				}
				node.marked = true;
				kernel.add(node);
			}
		}

		public void visit(NFA.Fork fork)
		{
			if (fork.isCtxStart())
			{
				ctxNodeFound = true;
			}
			fork.next.accept(this);
			fork.alt.accept(this);
		}

		public void visit(NFA.Term term)
		{
			if (!term.marked)
			{
				term.marked = true;
				kernel.add(term);
			}
		}

		void reset()
		{
			kernel.clear();
			ctxNodeFound = false;
		}

		NFA.Node[] getKernel()
		{
			return kernel.getNodes();
		}

		boolean kernelMatches(NFA.Node[] stateKernel)
		{
			return this.kernel.matches(stateKernel);
		}

		boolean isPreCtx()
		{
			return ctxNodeFound;
		}
	}

	static class Move implements NFA.NodeVisitor
	{
		int           accept;
		String        eventName;
		final CharMap transitions = new CharMap();

		public void visit(NFA.Char node)
		{
			for (int i = 0; i < node.charClasses.length; i++)
			{
				CharClass cls = node.charClasses[i];
				NFA.NodeArray charTransitions = (NFA.NodeArray) transitions.get(cls.id);
				if (charTransitions == null)
				{
					transitions.put(cls.id, charTransitions = new NFA.NodeArray());
				}
				charTransitions.add(node.next);
			}
		}

		public void visit(NFA.Fork fork)
		{
			// there are no forks in a kernel
		}

		public void visit(NFA.Term term)
		{
			if (accept == 0 || accept > 0 && accept > term.accept || accept < 0 && accept < term.accept)
			{
				accept = term.accept;
				eventName = term.eventName;
			}
		}

		void reset()
		{
			accept = 0;
			transitions.clear();
		}
	}
}
