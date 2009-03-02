package beaver.parser;

import static org.junit.Assert.*;

import org.junit.Test;

public class ParserStatesBuilderTest
{
	@Test
	public void testAdder()
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
		testAdderState1(state);
		int n = 0;
		for (; state != null; state = state.next)
		{
			n = state.id;
		}
		assertEquals(5, n);
	}
	
	private void testAdderState1(ParserState state)
	{
		assertEquals(1, state.id);
		assertEquals(
				"  Goal = * Expr\n" +
				"+ Expr = * NUM\n" +
				"+ Expr = * Expr \"+\" Expr",
				state.config.toString() 
		);
		assertNotNull(state.shift);
		ParserAction.Shift shift = (ParserAction.Shift) state.shift;
		assertEquals("NUM", shift.lookahead.toString());
		testAdderState5(shift.dest);
		assertNotNull(shift.next);
		shift = (ParserAction.Shift) shift.next;
		assertEquals("Expr", shift.lookahead.toString());
		testAdderState2(shift.dest);
		assertNull(shift.next);
		
		assertNull(state.reduce);
	}

	private void testAdderState2(ParserState state)
	{
		assertEquals(2, state.id);
		assertEquals(
				"  Goal = Expr *\n" +
				"  Expr = Expr * \"+\" Expr",
				state.config.toString() 
		);
		assertNotNull(state.shift);
		ParserAction.Shift shift = (ParserAction.Shift) state.shift;
		assertEquals("\"+\"", shift.lookahead.toString());
		testAdderState3(shift.dest);
		assertNull(shift.next);
		
		assertNotNull(state.reduce);
		ParserAction.Reduce reduce = (ParserAction.Reduce) state.reduce;
		assertEquals("EOF", reduce.lookahead.toString());
		assertEquals("Goal = Expr", reduce.production.toString());
		assertNull(reduce.next);
	}

	private void testAdderState3(ParserState state)
	{
		assertEquals(3, state.id);
		assertEquals(
				"  Expr = Expr \"+\" * Expr\n" +
				"+ Expr = * NUM\n" +
				"+ Expr = * Expr \"+\" Expr",
				state.config.toString() 
		);
		assertNotNull(state.shift);
		ParserAction.Shift shift = (ParserAction.Shift) state.shift;
		assertEquals("NUM", shift.lookahead.toString());
		assertEquals(5, shift.dest.id);
		assertNotNull(shift.next);
		shift = (ParserAction.Shift) shift.next;
		assertEquals("Expr", shift.lookahead.toString());
		testAdderState4(shift.dest);
		assertNull(shift.next);
		
		assertNull(state.reduce);
	}

	private void testAdderState4(ParserState state)
	{
		assertEquals(4, state.id);
		assertEquals(
				"  Expr = Expr * \"+\" Expr\n" +
				"  Expr = Expr \"+\" Expr *",
				state.config.toString() 
		);
		assertNotNull(state.shift);
		ParserAction.Shift shift = (ParserAction.Shift) state.shift;
		assertEquals("\"+\"", shift.lookahead.toString());
		assertEquals(3, shift.dest.id);
		assertNull(shift.next);
		
		assertNotNull(state.reduce);
		ParserAction.Reduce reduce = (ParserAction.Reduce) state.reduce;
		// Notice shift-reduce conflict here
		assertEquals("\"+\"", reduce.lookahead.toString());
		assertEquals("Expr = Expr \"+\" Expr", reduce.production.toString());
		assertNotNull(reduce.next);
		reduce = (ParserAction.Reduce) reduce.next;
		assertEquals("EOF", reduce.lookahead.toString());
		assertEquals("Expr = Expr \"+\" Expr", reduce.production.toString());
		assertNull(reduce.next);
	}
	
	private void testAdderState5(ParserState state)
	{
		assertEquals(5, state.id);
		assertEquals(
				"  Expr = NUM *",
				state.config.toString() 
		);
		assertNull(state.shift);
		
		assertNotNull(state.reduce);
		ParserAction.Reduce reduce = (ParserAction.Reduce) state.reduce;
		assertEquals("\"+\"", reduce.lookahead.toString());
		assertEquals("Expr = NUM", reduce.production.toString());
		assertNotNull(reduce.next);
		reduce = (ParserAction.Reduce) reduce.next;
		assertEquals("EOF", reduce.lookahead.toString());
		assertEquals("Expr = NUM", reduce.production.toString());
		assertNull(reduce.next);
	}
	
	@Test
	public void testMakingDefaultActions()
	{
		GrammarFactory fact = new GrammarFactory(new String[] {"NUM"});
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
		fact.left()
		    .prec("*")
		    .prec("/");
		fact.left()
		    .prec("+")
		    .prec("-");
		Grammar grammar = fact.getGrammar();
		ParserState firstState = new ParserStatesBuilder().buildParserStates(grammar);
		
		ParserState state = firstState;
		assertEquals(1, state.id);
		assertEquals(3, state.numActions);
		assertNull(state.reduce);
		
		state = state.next;
		assertEquals(2, state.id);
		assertEquals(5, state.numActions);
		assertNotNull(state.reduce);
		assertEquals(1, countParserActions(state.reduce));
		
		state = state.next;
		assertEquals(3, state.id);
		assertEquals(3, state.numActions);
		assertNull(state.reduce);
		
		state = state.next;
		assertEquals(4, state.id);
		assertEquals(10, state.numActions);
		assertNotNull(state.reduce);
		assertEquals(6, countParserActions(state.reduce));
		
		state = state.next;
		assertEquals(5, state.id);
		assertEquals(3, state.numActions);
		assertNull(state.reduce);
		
		state = state.next;
		assertEquals(6, state.id);
		assertEquals(10, state.numActions);
		assertNotNull(state.reduce);
		assertEquals(6, countParserActions(state.reduce));
		
		state = state.next;
		assertEquals(7, state.id);
		assertEquals(3, state.numActions);
		assertNull(state.reduce);
		
		state = state.next;
		assertEquals(8, state.id);
		assertEquals(10, state.numActions);
		assertNotNull(state.reduce);
		assertEquals(6, countParserActions(state.reduce));
		
		state = state.next;
		assertEquals(9, state.id);
		assertEquals(3, state.numActions);
		assertNull(state.reduce);
		
		state = state.next;
		assertEquals(10, state.id);
		assertEquals(10, state.numActions);
		assertNotNull(state.reduce);
		assertEquals(6, countParserActions(state.reduce));
		
		state = state.next;
		assertEquals(11, state.id);
		assertEquals(6, state.numActions);
		assertNotNull(state.reduce);
		assertEquals(6, countParserActions(state.reduce));
		
		state = state.next;
		assertEquals(12, state.id);
		assertEquals(3, state.numActions);
		assertNull(state.reduce);
		
		state = state.next;
		assertEquals(13, state.id);
		assertEquals(5, state.numActions);
		assertNull(state.reduce);
		
		state = state.next;
		assertEquals(14, state.id);
		assertEquals(6, state.numActions);
		assertNotNull(state.reduce);
		assertEquals(6, countParserActions(state.reduce));
		
		assertNull(state.next);
		
		ParserStatesBuilder.makeDefaultReduceActions(firstState, grammar);
		
		state = firstState;
		assertEquals(1, state.id);
		assertEquals(3, state.numActions);
		assertNull(state.reduce);
		
		state = state.next;
		assertEquals(2, state.id);
		assertEquals(5, state.numActions);
		assertNotNull(state.reduce);
		assertEquals(1, countParserActions(state.reduce));
		
		state = state.next;
		assertEquals(3, state.id);
		assertEquals(3, state.numActions);
		assertNull(state.reduce);
		
		state = state.next;
		assertEquals(4, state.id);
		assertEquals(4, state.numActions);
		assertNull(state.reduce);
		assertNotNull(state.defaultReduce);
		
		state = state.next;
		assertEquals(5, state.id);
		assertEquals(3, state.numActions);
		assertNull(state.reduce);
		
		state = state.next;
		assertEquals(6, state.id);
		assertEquals(4, state.numActions);
		assertNull(state.reduce);
		assertNotNull(state.defaultReduce);
		
		state = state.next;
		assertEquals(7, state.id);
		assertEquals(3, state.numActions);
		assertNull(state.reduce);
		
		state = state.next;
		assertEquals(8, state.id);
		assertEquals(4, state.numActions);
		assertNull(state.reduce);
		assertNotNull(state.defaultReduce);
		
		state = state.next;
		assertEquals(9, state.id);
		assertEquals(3, state.numActions);
		assertNull(state.reduce);
		
		state = state.next;
		assertEquals(10, state.id);
		assertEquals(4, state.numActions);
		assertNull(state.reduce);
		assertNotNull(state.defaultReduce);
		
		state = state.next;
		assertEquals(11, state.id);
		assertEquals(0, state.numActions);
		assertNull(state.reduce);
		assertNotNull(state.defaultReduce);
		
		state = state.next;
		assertEquals(12, state.id);
		assertEquals(3, state.numActions);
		assertNull(state.reduce);
		
		state = state.next;
		assertEquals(13, state.id);
		assertEquals(5, state.numActions);
		assertNull(state.reduce);
		
		state = state.next;
		assertEquals(14, state.id);
		assertEquals(0, state.numActions);
		assertNull(state.reduce);
		assertNotNull(state.defaultReduce);
		
		assertNull(state.next);
	}
	
	private static int countParserActions(ParserAction action)
	{
		int n = 0;
		while (action != null)
		{
			n++;
			action = action.next;
		}
		return n;
	}
}
