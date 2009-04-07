/**
 * 
 */
package beaver.lexer;

import beaver.UnexpectedCharacterException;

public class DFAInterpreter
{
	private DFA    dfa;
	private char[] txt;
	private int    ptr;
	private int    end;

	DFAInterpreter(DFA dfa)
	{
		this.dfa = dfa;
		this.txt = new char[1024];
	}

	int next(CharReader text) throws UnexpectedCharacterException
	{
		DFAState st = dfa.start;
		Accept acc = null;
		int mark = text.mark();
		end = ptr = 0;
		do
		{
			if (st.accept != null)
			{
				mark = text.mark();
			}
			
			int c = text.readChar();
			if (c < 0)
			{
				if (ptr > 0 && acc == null)
					throw new IllegalStateException("unexpected EOF");
				else
					break;
			}

			if (st.accept != null)
			{
				acc = st.accept;
				end = ptr;
			}
			
			DFAStateTransition tr = st.firstTransition;
			while (tr != null && !tr.onChars.contains((char) c))
			{
				tr = tr.next;
			}
			
			if (tr == null)
			{
				if (acc == null)
					throw new UnexpectedCharacterException();
				else
					break;
			}
			
			txt[ptr++] = (char) c;
			st = tr.toState;
		}
		while (st.firstTransition != null);
		
		if (st.firstTransition == null)
		{
			if ((acc = st.accept) == null)
				throw new IllegalStateException("invalid DFA");
			end = ptr;
		}
		else
		{
			text.reset(mark);
		}
		return acc != null ? acc.id : 0;
	}

	String text()
	{
		return new String(txt, 0, end);
	}

}
