package beaver.parser;

import static org.junit.Assert.*;

import org.junit.Test;

public class ParserStateActionsConflictResolverTest
{
	@Test
	public void testAdderConflictResolutionWithoutPrecedence()
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
		// state 1
		assertNull(state.resolveConflicts(null));
		state = state.next;
		// state 2
		assertNull(state.resolveConflicts(null));
		state = state.next;
		// state 3
		assertNull(state.resolveConflicts(null));
		state = state.next;
		// state 4
		ParserAction.Conflict conflict = state.resolveConflicts(null);
		assertNotNull(conflict);
		assertTrue(conflict.action1 instanceof ParserAction.Shift);
		assertTrue(conflict.action2 instanceof ParserAction.Reduce);
		assertEquals("+", conflict.action1.lookahead.toString());
		ParserAction.Shift shift = (ParserAction.Shift) conflict.action1;
		assertEquals(3, shift.dest.id);
		ParserAction.Reduce reduce = (ParserAction.Reduce) conflict.action2;		
		assertEquals("Expr = Expr + Expr ;", reduce.production.toString());
		assertNull(conflict.next);
		state = state.next;
		// state 5
		assertNull(state.resolveConflicts(null));
		
		assertNull(state.next);
	}
	
	@Test
	public void testAdderConflictResolutionWithPrecedence_PreferShift()
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
		// this builder does not (yet) support precedences
		// assign one to "+" manually
		for (int i = 1; i < grammar.terminals.length; i++)
		{
			if (grammar.terminals[i].toString().equals("+"))
			{
				grammar.terminals[i].precedence = '\uffff';
				break;
			}
		}
		ParserState state = new ParserStatesBuilder().buildParserStates(grammar);
		// find state 4, where there is a conflict
		state = state.next; // 1 -> 2
		state = state.next; // 2 -> 3
		state = state.next; // 3 -> 4
		// state 4
		assertNull(state.resolveConflicts(null));
		
		assertNotNull(state.shift);
		ParserAction.Shift shift = (ParserAction.Shift) state.shift;
		assertEquals("+", shift.lookahead.toString());
		assertEquals(3, shift.dest.id);
		assertNull(shift.next);
		
		assertNotNull(state.reduce);
		ParserAction.Reduce reduce = (ParserAction.Reduce) state.reduce;
		assertEquals("EOF", reduce.lookahead.toString());
		assertEquals("Expr = Expr + Expr ;", reduce.production.toString());
		assertNull(reduce.next);
		
		state = state.next; // 4 -> 5
		assertNull(state.next);
	}
	
	@Test
	public void testAdderConflictResolutionWithPrecedence_PreferReduce()
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
		// this builder does not (yet) support precedences
		// assign one to EXPR manually
		for (int i = 0; i < grammar.productions.length; i++)
		{
			if (grammar.productions[i].toString().equals("Expr = Expr + Expr ;"))
			{
				grammar.productions[i].precedence = '\uffff';
				break;
			}
		}
		ParserState state = new ParserStatesBuilder().buildParserStates(grammar);
		// find state 4, where there is a conflict
		state = state.next; // 1 -> 2
		state = state.next; // 2 -> 3
		state = state.next; // 3 -> 4
		// state 4
		assertNull(state.resolveConflicts(null));
		
		assertNull(state.shift);
		
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
		
		state = state.next; // 4 -> 5
		assertNull(state.next);
	}

	@Test
	public void testAdderConflictResolutionWithSamePrecedence_RightAssoc()
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
		// this builder does not (yet) support precedences
		// assign one to "+" manually
		for (int i = 1; i < grammar.terminals.length; i++)
		{
			if (grammar.terminals[i].toString().equals("+"))
			{
				grammar.terminals[i].associativity = 'R';
				break;
			}
		}
		ParserState state = new ParserStatesBuilder().buildParserStates(grammar);
		// find state 4, where there is a conflict
		state = state.next; // 1 -> 2
		state = state.next; // 2 -> 3
		state = state.next; // 3 -> 4
		// state 4
		assertNull(state.resolveConflicts(null));
		
		assertNotNull(state.shift);
		ParserAction.Shift shift = (ParserAction.Shift) state.shift;
		assertEquals("+", shift.lookahead.toString());
		assertEquals(3, shift.dest.id);
		assertNull(shift.next);
		
		assertNotNull(state.reduce);
		ParserAction.Reduce reduce = (ParserAction.Reduce) state.reduce;
		assertEquals("EOF", reduce.lookahead.toString());
		assertEquals("Expr = Expr + Expr ;", reduce.production.toString());
		assertNull(reduce.next);
		
		state = state.next; // 4 -> 5
		assertNull(state.next);
	}

	@Test
	public void testAdderConflictResolutionWithSamePrecedence_LeftAssoc()
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
		// this builder does not (yet) support precedences
		// assign one to EXPR manually
		for (int i = 1; i < grammar.terminals.length; i++)
		{
			if (grammar.terminals[i].toString().equals("+"))
			{
				grammar.terminals[i].associativity = 'L';
				break;
			}
		}
		ParserState state = new ParserStatesBuilder().buildParserStates(grammar);
		// find state 4, where there is a conflict
		state = state.next; // 1 -> 2
		state = state.next; // 2 -> 3
		state = state.next; // 3 -> 4
		// state 4
		assertNull(state.resolveConflicts(null));
		
		assertNull(state.shift);
		
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
		
		state = state.next; // 4 -> 5
		assertNull(state.next);
	}
}
