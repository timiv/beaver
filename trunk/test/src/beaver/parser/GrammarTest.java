package beaver.parser;

import static org.junit.Assert.*;
import org.junit.Test;

import beaver.util.BitSet;

public class GrammarTest
{
	@Test
	public void testReducibleGrammar()
	{
		GrammarFactory fact = new GrammarFactory(new String[] {"NUM", "ID"});
		fact.def("Expr")
			.sym("NUM")
			.end();
		fact.def("Expr")
			.sym("Expr")
			.txt("+")
			.sym("Expr")
			.end();
		Grammar grammar = fact.getGrammar();
		ParserState state = new ParserStatesBuilder().buildParserStates(grammar);
		BitSet unreducibleProductionsIdxSet = grammar.findUnreducibleProductions(state);
		assertEquals(0, unreducibleProductionsIdxSet.size());
	}
	
	@Test
	public void testGrammarWithUnreducibleProductions()
	{
		GrammarFactory fact = new GrammarFactory(new String[] {"NUM", "ID"});
		fact.def("Expr")
			.sym("NUM")
			.end();
		fact.def("Expr")
			.sym("Expr")
			.txt("+")
			.sym("Expr")
			.end();
		fact.def("Stmt")
			.sym("ID")
			.txt("=")
			.sym("Expr")
			.txt(";")
			.end();
		final Grammar grammar = fact.getGrammar();
		ParserState state = new ParserStatesBuilder().buildParserStates(grammar);
		BitSet unreducibleProductionsIdxSet = grammar.findUnreducibleProductions(state);
		assertEquals(1, unreducibleProductionsIdxSet.size());
		unreducibleProductionsIdxSet.forEachBitAccept(new BitSet.BitVisitor()
		{
			@Override
            public void visit(int i)
            {
				assertEquals("Stmt = ID = Expr ; ;", grammar.productions[i].toString());
            }
		});
	}
}
