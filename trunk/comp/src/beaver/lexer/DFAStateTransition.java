package beaver.lexer;

class DFAStateTransition
{
	DFAStateTransition next;
	CharRange          onChars;
	DFAState           toState;

	DFAStateTransition(CharRange onChars, DFAState toState, DFAStateTransition next)
	{
		this.onChars = onChars;
		this.toState = toState;
		this.next = next;
	}

	boolean equals(DFAStateTransition t)
	{
		return this.toState.id == t.toState.id && onChars.equals(t.onChars);
	}
}
