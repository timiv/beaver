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
				"  Goal = * Expr ;\n" +
				"+ Expr = * NUM ;\n" +
				"+ Expr = * Expr + Expr ;",
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
				"  Goal = Expr * ;\n" +
				"  Expr = Expr * + Expr ;",
				state.config.toString() 
		);
		assertNotNull(state.shift);
		ParserAction.Shift shift = (ParserAction.Shift) state.shift;
		assertEquals("+", shift.lookahead.toString());
		testAdderState3(shift.dest);
		assertNull(shift.next);
		
		assertNotNull(state.reduce);
		ParserAction.Reduce reduce = (ParserAction.Reduce) state.reduce;
		assertEquals("EOF", reduce.lookahead.toString());
		assertEquals("Goal = Expr ;", reduce.production.toString());
		assertNull(reduce.next);
	}

	private void testAdderState3(ParserState state)
	{
		assertEquals(3, state.id);
		assertEquals(
				"  Expr = Expr + * Expr ;\n" +
				"+ Expr = * NUM ;\n" +
				"+ Expr = * Expr + Expr ;",
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
				"  Expr = Expr * + Expr ;\n" +
				"  Expr = Expr + Expr * ;",
				state.config.toString() 
		);
		assertNotNull(state.shift);
		ParserAction.Shift shift = (ParserAction.Shift) state.shift;
		assertEquals("+", shift.lookahead.toString());
		assertEquals(3, shift.dest.id);
		assertNull(shift.next);
		
		assertNotNull(state.reduce);
		ParserAction.Reduce reduce = (ParserAction.Reduce) state.reduce;
		// Notice shift-reduce conflict here
		assertEquals("+", reduce.lookahead.toString());
		assertEquals("Expr = Expr + Expr ;", reduce.production.toString());
		assertNotNull(reduce.next);
		reduce = (ParserAction.Reduce) reduce.next;
		assertEquals("EOF", reduce.lookahead.toString());
		assertEquals("Expr = Expr + Expr ;", reduce.production.toString());
		assertNull(reduce.next);
	}
	
	private void testAdderState5(ParserState state)
	{
		assertEquals(5, state.id);
		assertEquals(
				"  Expr = NUM * ;",
				state.config.toString() 
		);
		assertNull(state.shift);
		
		assertNotNull(state.reduce);
		ParserAction.Reduce reduce = (ParserAction.Reduce) state.reduce;
		assertEquals("+", reduce.lookahead.toString());
		assertEquals("Expr = NUM ;", reduce.production.toString());
		assertNotNull(reduce.next);
		reduce = (ParserAction.Reduce) reduce.next;
		assertEquals("EOF", reduce.lookahead.toString());
		assertEquals("Expr = NUM ;", reduce.production.toString());
		assertNull(reduce.next);
	}
}
