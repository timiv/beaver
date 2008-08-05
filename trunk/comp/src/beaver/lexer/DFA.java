package beaver.lexer;

public class DFA
{
	DFAState start;
	int      numStates;

	DFA(NFA nfa)
	{
		DFAStateBuilder stateBuilder = new DFAStateBuilder();
		stateBuilder.buildDFAStates(nfa);
		
		this.start = stateBuilder.getStartState(); 
		this.numStates = stateBuilder.getNumberOfStates();
	}
}
