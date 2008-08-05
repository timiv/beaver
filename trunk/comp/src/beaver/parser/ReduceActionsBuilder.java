package beaver.parser;

import beaver.parser.ParserAction.Reduce;
import beaver.util.BitSet.BitVisitor;

public class ReduceActionsBuilder implements BitVisitor
{
	private Terminal[]  terminals;
	private ParserState state;
	private Production  prod;

	ReduceActionsBuilder(Terminal[] terminals)
	{
		this.terminals = terminals;
	}

	void set(ParserState s)
	{
		state = s;
	}

	void set(Production p)
	{
		prod = p;
	}

	public void visit(int i)
	{
		state.add(new Reduce(terminals[i], prod));
	}
}
