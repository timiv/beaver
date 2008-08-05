package beaver.lexer;

class DFAPrinter
{
	static void print(DFA dfa)
	{
		for (DFAState st = dfa.start; st != null; st = st.next)
		{
			System.out.println(st.id + ": = " + st.accept);
			for (DFAStateTransition tr = st.firstTransition; tr != null; tr = tr.next)
			{
				System.out.println("    [" + tr.onChars + "] -> " + tr.toState.id);
			}
		}
	}
}
