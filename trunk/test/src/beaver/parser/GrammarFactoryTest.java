package beaver.parser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GrammarFactoryTest
{
	@Test
	public void testAddingProductions()
	{
		GrammarFactory fact = new GrammarFactory(new String[] {"NUM", "ID"});
		fact.def("Expr", "Number")
			.sym("NUM")
			.end();
		fact.def("Expr", "Nested")
			.txt("(")
			.sym("Expr")
			.txt(")")
			.end();
		fact.def("Expr", "Add")
			.sym("Expr")
			.txt("+")
			.sym("Expr")
			.end();
		fact.def("Expr", "Sub")
    		.sym("Expr")
    		.txt("-")
    		.sym("Expr")
    		.end();
		fact.def("Expr", "Mul")
    		.sym("Expr")
    		.txt("*")
    		.sym("Expr")
    		.end();
		fact.def("Expr", "Div")
    		.sym("Expr")
    		.txt("/")
    		.sym("Expr")
    		.end();
		Grammar grammar = fact.getGrammar();
		assertEquals(1 + 6 + 1, grammar.terminals.length);
		assertEquals("EOF", grammar.terminals[0].toString());
		assertEquals("\"(\"",   grammar.terminals[1].toString());
		assertEquals("\")\"",   grammar.terminals[2].toString());
		assertEquals("\"+\"",   grammar.terminals[3].toString());
		assertEquals("\"-\"",   grammar.terminals[4].toString());
		assertEquals("\"*\"",   grammar.terminals[5].toString());
		assertEquals("\"/\"",   grammar.terminals[6].toString());
		assertEquals("NUM", grammar.terminals[7].toString());
		assertEquals(2, grammar.nonterminals.length);
		assertEquals("Expr", grammar.nonterminals[0].toString());
		assertEquals("Goal", grammar.nonterminals[1].toString());
		assertEquals(7, grammar.productions.length);
		assertEquals("Goal = Expr",                grammar.productions[0].toString());
		assertEquals("Expr = { Number } NUM",      grammar.productions[1].toString());
		assertEquals("Expr = { Nested } \"(\" Expr \")\"", grammar.productions[2].toString());
		assertEquals("Expr = { Add } Expr \"+\" Expr", grammar.productions[3].toString());
		assertEquals("Expr = { Sub } Expr \"-\" Expr", grammar.productions[4].toString());
		assertEquals("Expr = { Mul } Expr \"*\" Expr", grammar.productions[5].toString());
		assertEquals("Expr = { Div } Expr \"/\" Expr", grammar.productions[6].toString());
	}
	
	@Test
	public void testPrecedence()
	{
		GrammarFactory fact = new GrammarFactory(new String[] {"NUM", "ID"});
		fact.def("Expr", "Number")
			.sym("NUM")
			.end();
		fact.def("Expr", "Nested")
			.txt("(")
			.sym("Expr")
			.txt(")")
			.end();
		fact.def("Expr", "Add")
			.sym("Expr")
			.txt("+")
			.sym("Expr")
			.end();
		fact.def("Expr", "Sub")
    		.sym("Expr")
    		.txt("-")
    		.sym("Expr")
    		.end();
		fact.def("Expr", "Mul")
    		.sym("Expr")
    		.txt("*")
    		.sym("Expr")
    		.end();
		fact.def("Expr", "Div")
    		.sym("Expr")
    		.txt("/")
    		.sym("Expr")
    		.end();
		fact.none()
		    .prec("Expr", "Nested"); // this precedence is not something that a real grammar would need
		fact.left()
		    .prec("*")
		    .prec("/");
		fact.left()
		    .prec("+")
		    .prec("-");
		Grammar grammar = fact.getGrammar();
		assertEquals(1 + 6 + 1, grammar.terminals.length);
		assertEquals("EOF", grammar.terminals[0].toString());
		assertEquals("\"(\"",   grammar.terminals[1].toString());
		assertEquals("\")\"",   grammar.terminals[2].toString());
		assertEquals("\"+\"",   grammar.terminals[3].toString());
		assertEquals("\"-\"",   grammar.terminals[4].toString());
		assertEquals("\"*\"",   grammar.terminals[5].toString());
		assertEquals("\"/\"",   grammar.terminals[6].toString());
		assertEquals("NUM", grammar.terminals[7].toString());
		assertEquals(2, grammar.nonterminals.length);
		assertEquals("Expr", grammar.nonterminals[0].toString());
		assertEquals("Goal", grammar.nonterminals[1].toString());
		assertEquals(7, grammar.productions.length);
		assertEquals("Goal = Expr",                grammar.productions[0].toString());
		assertEquals("Expr = { Number } NUM",      grammar.productions[1].toString());
		assertEquals("Expr = { Nested } \"(\" Expr \")\"", grammar.productions[2].toString());
		assertEquals("Expr = { Add } Expr \"+\" Expr", grammar.productions[3].toString());
		assertEquals("Expr = { Sub } Expr \"-\" Expr", grammar.productions[4].toString());
		assertEquals("Expr = { Mul } Expr \"*\" Expr", grammar.productions[5].toString());
		assertEquals("Expr = { Div } Expr \"/\" Expr", grammar.productions[6].toString());
		
		assertEquals('\ufffe', grammar.productions[2].precedence);
		assertEquals('\ufffd', grammar.terminals[5].precedence);
		assertEquals('L', grammar.terminals[5].associativity);
		assertEquals('\ufffd', grammar.terminals[6].precedence);
		assertEquals('L', grammar.terminals[6].associativity);
		assertEquals('\ufffc', grammar.terminals[3].precedence);
		assertEquals('L', grammar.terminals[3].associativity);
		assertEquals('\ufffc', grammar.terminals[4].precedence);
		assertEquals('L', grammar.terminals[4].associativity);
		assertEquals('\ufffd', grammar.productions[5].precedence);
		assertEquals('\ufffd', grammar.productions[6].precedence);
		assertEquals('\ufffc', grammar.productions[3].precedence);
		assertEquals('\ufffc', grammar.productions[4].precedence);
	}
}
