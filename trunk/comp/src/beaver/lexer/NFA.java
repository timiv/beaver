package beaver.lexer;

class NFA
{
	NFANode start;
	CharSet cset;
	
	NFA(RegExp re)
	{
		cset = new CharSet(re);
		NFACompiler comp = new NFACompiler(cset);
		re.accept(comp);
		start = comp.getStart();
	}
}
