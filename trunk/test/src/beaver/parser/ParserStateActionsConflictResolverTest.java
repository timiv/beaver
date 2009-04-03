package beaver.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class ParserStateActionsConflictResolverTest
{
	@Test
	public void testConflictResolutionWithoutPrecedence()
	{
		GrammarFactory fact = new GrammarFactory(new String[] {"NUM"});
		fact.def("OptList")
			.sym("List")
			.end();
		fact.def("OptList")
			.end();
		fact.def("List")
			.sym("OptNum")
			.end();
		fact.def("List")
			.sym("List")
			.sym("OptNum")
			.end();
		fact.def("OptNum")
			.sym("NUM")
			.end();
		fact.def("OptNum")
			.end();
		Grammar grammar = fact.getGrammar();
		ParserState state = new ParserStatesBuilder().buildParserStates(grammar);
		/*
		 * 1:
		   shift:
		       NUM -> 5
		    OptNum -> 6
		      List -> 3
		   OptList -> 2
		   reduce:
		       NUM -> OptNum =
		       EOF -> OptNum =
		       EOF -> OptList =
		   accept:
		      Goal -> ACCEPT		
		 */
		ParserAction.Conflict conflict = state.resolveConflicts(null, false);
		assertNotNull(conflict);
		assertTrue(conflict.action1 instanceof ParserAction.Reduce);
		assertTrue(conflict.action2 instanceof ParserAction.Reduce);
		assertEquals("EOF", conflict.action1.lookahead.toString());
		ParserAction.Reduce reduce = (ParserAction.Reduce) conflict.action1;		
		assertEquals("OptNum =", reduce.production.toString());
		reduce = (ParserAction.Reduce) conflict.action2;		
		assertEquals("OptList =", reduce.production.toString());		
		assertNotNull(conflict = conflict.next);	
		assertTrue(conflict.action1 instanceof ParserAction.Shift);
		assertTrue(conflict.action2 instanceof ParserAction.Reduce);
		assertEquals("NUM", conflict.action1.lookahead.toString());
		ParserAction.Shift shift = (ParserAction.Shift) conflict.action1;
		assertEquals(5, shift.dest.id);
		reduce = (ParserAction.Reduce) conflict.action2;		
		assertEquals("OptNum =", reduce.production.toString());
		assertNull(conflict.next);
		
		state = state.next;
		/*
		 * 2:
		   reduce:
		       EOF -> Goal = OptList		
		 */
		conflict = state.resolveConflicts(null, false);
		assertNull(conflict);	

		state = state.next;
		/*
		 * 3:
		   shift:
		       NUM -> 5
		    OptNum -> 4
		   reduce:
		       NUM -> OptNum =
		       EOF -> OptNum =
		       EOF -> OptList = List		
		 */
		conflict = state.resolveConflicts(null, false);
		assertNotNull(conflict);
		assertTrue(conflict.action1 instanceof ParserAction.Reduce);
		assertTrue(conflict.action2 instanceof ParserAction.Reduce);
		assertEquals("EOF", conflict.action1.lookahead.toString());
		reduce = (ParserAction.Reduce) conflict.action1;		
		assertEquals("OptNum =", reduce.production.toString());
		reduce = (ParserAction.Reduce) conflict.action2;		
		assertEquals("OptList = List", reduce.production.toString());		
		assertNotNull(conflict = conflict.next);	
		assertTrue(conflict.action1 instanceof ParserAction.Shift);
		assertTrue(conflict.action2 instanceof ParserAction.Reduce);
		assertEquals("NUM", conflict.action1.lookahead.toString());
		shift = (ParserAction.Shift) conflict.action1;
		assertEquals(5, shift.dest.id);
		reduce = (ParserAction.Reduce) conflict.action2;		
		assertEquals("OptNum =", reduce.production.toString());
		assertNull(conflict.next);

		state = state.next;
		/*
		 * 4:
		   reduce:
		       NUM -> List = List OptNum
		       EOF -> List = List OptNum

		 */
		conflict = state.resolveConflicts(null, false);
		assertNull(conflict);	
		
		state = state.next;
		/*
		 * 5:
		   reduce:
		       NUM -> OptNum = NUM
		       EOF -> OptNum = NUM
				
		 */
		conflict = state.resolveConflicts(null, false);
		assertNull(conflict);	

		state = state.next;
		/*
		 * 6:
		   reduce:
		       NUM -> List = OptNum
		       EOF -> List = OptNum	
		 */
		conflict = state.resolveConflicts(null, false);
		assertNull(conflict);	
		
		assertNull(state.next);
	}
	
	@Test
	public void testConflictResolutionWithImplicitPreferShiftPrecedence()
	{
		GrammarFactory fact = new GrammarFactory(new String[] {"NUM"});
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
        /*
            1:
           shift:
               NUM -> 5
              Expr -> 2
           accept:
              Goal -> ACCEPT
        
            2:
               shift:
                   "+" -> 3
               reduce:
                   EOF -> Goal = Expr
        
            3:
               shift:
                   NUM -> 5
                  Expr -> 4
        
            4:
               shift:
                   "+" -> 3
               reduce:
                   "+" -> Expr = Expr "+" Expr
                   EOF -> Expr = Expr "+" Expr
                   
            5:
               reduce:
                   "+" -> Expr = NUM
                   EOF -> Expr = NUM
         */
		// find state 4, where there is a conflict
		assertNotNull(state); 				// 1
		assertNotNull(state = state.next); 	// 1 -> 2
		assertNotNull(state = state.next); 	// 2 -> 3
		assertNotNull(state = state.next);  // 3 -> 4
		// state 4
		assertNull(state.resolveConflicts(null, true));
		/*
            4:
               shift:
                   "+" -> 3
               reduce:
                   EOF -> Expr = Expr "+" Expr
		 */
		assertNotNull(state.shift);
		ParserAction.Shift shift = (ParserAction.Shift) state.shift;
		assertEquals("\"+\"", shift.lookahead.toString());
		assertEquals(3, shift.dest.id);
		assertNull(shift.next);
		
		assertNotNull(state.reduce);
		ParserAction.Reduce reduce = (ParserAction.Reduce) state.reduce;
		assertEquals("EOF", reduce.lookahead.toString());
		assertEquals("Expr = Expr \"+\" Expr", reduce.production.toString());
		assertNull(reduce.next);
		
		assertNotNull(state = state.next); // 4 -> 5
		assertNull(state.next);
	}
	
	
	@Test
	public void testConflictResolutionWithExplicitPrecedence_PreferShift()
	{
		GrammarFactory fact = new GrammarFactory(new String[] {"NUM"});
		fact.def("Expr")
			.sym("NUM")
			.end();
		fact.def("Expr", "Add")
			.sym("Expr")
			.txt("+")
			.sym("Expr")
			.end();
		fact.none()
			.prec("+");
		fact.none()
			.prec("Expr", "Add");
		Grammar grammar = fact.getGrammar();
		ParserState state = new ParserStatesBuilder().buildParserStates(grammar);
        /*
            1:
           shift:
               NUM -> 5
              Expr -> 2
           accept:
              Goal -> ACCEPT
        
            2:
               shift:
                   "+" -> 3
               reduce:
                   EOF -> Goal = Expr
        
            3:
               shift:
                   NUM -> 5
                  Expr -> 4
        
            4:
               shift:
                   "+" -> 3
               reduce:
                   "+" -> Expr = Expr "+" Expr
                   EOF -> Expr = Expr "+" Expr
                   
            5:
               reduce:
                   "+" -> Expr = NUM
                   EOF -> Expr = NUM
         */
		// find state 4, where there is a conflict
		assertNotNull(state); 				// 1
		assertNotNull(state = state.next); 	// 1 -> 2
		assertNotNull(state = state.next); 	// 2 -> 3
		assertNotNull(state = state.next);  // 3 -> 4
		// state 4
		assertNull(state.resolveConflicts(null, false));
		/*
            4:
               shift:
                   "+" -> 3
               reduce:
                   EOF -> Expr = Expr "+" Expr
		 */
		assertNotNull(state.shift);
		ParserAction.Shift shift = (ParserAction.Shift) state.shift;
		assertEquals("\"+\"", shift.lookahead.toString());
		assertEquals(3, shift.dest.id);
		assertNull(shift.next);
		
		assertNotNull(state.reduce);
		ParserAction.Reduce reduce = (ParserAction.Reduce) state.reduce;
		assertEquals("EOF", reduce.lookahead.toString());
		assertEquals("Expr = { Add } Expr \"+\" Expr", reduce.production.toString());
		assertNull(reduce.next);
		
		assertNotNull(state = state.next); // 4 -> 5
		assertNull(state.next);
	}
	
	@Test
	public void testConflictResolutionWithExplicitPrecedence_PreferReduce()
	{
		GrammarFactory fact = new GrammarFactory(new String[] {"NUM"});
		fact.def("Expr", "Num")
			.sym("NUM")
			.end();
		fact.def("Expr", "Add")
			.sym("Expr")
			.txt("+")
			.sym("Expr")
			.end();
		fact.none()
			.prec("Expr", "Add");
		Grammar grammar = fact.getGrammar();
		ParserState state = new ParserStatesBuilder().buildParserStates(grammar);
		// find state 4, where there is a conflict
		assertNotNull(state); 				// 1
		assertNotNull(state = state.next); 	// 1 -> 2
		assertNotNull(state = state.next); 	// 2 -> 3
		assertNotNull(state = state.next);  // 3 -> 4
		// state 4
		assertNull(state.resolveConflicts(null, false));
		/*
            4:
               reduce:
                   "+" -> Expr = Expr "+" Expr
                   EOF -> Expr = Expr "+" Expr
		 */
		assertNull(state.shift);
		
		assertNotNull(state.reduce);
		ParserAction.Reduce reduce = (ParserAction.Reduce) state.reduce;
		assertEquals("\"+\"", reduce.lookahead.toString());
		assertEquals("Expr = { Add } Expr \"+\" Expr", reduce.production.toString());
		assertNotNull(reduce.next);
		reduce = (ParserAction.Reduce) reduce.next;
		assertEquals("EOF", reduce.lookahead.toString());
		assertEquals("Expr = { Add } Expr \"+\" Expr", reduce.production.toString());
		assertNull(reduce.next);
		
		state = state.next; // 4 -> 5
		assertNull(state.next);
	}

	@Test
	public void testConflictResolutionWithSameExplicitPrecedence_RightAssoc()
	{
		GrammarFactory fact = new GrammarFactory(new String[] {"NUM"});
		fact.def("Expr")
			.sym("NUM")
			.end();
		fact.def("Expr", "Add")
			.sym("Expr")
			.txt("+")
			.sym("Expr")
			.end();
		fact.right()
			.prec("+")
			.prec("Expr", "Add");
		Grammar grammar = fact.getGrammar();
		ParserState state = new ParserStatesBuilder().buildParserStates(grammar);
		// find state 4, where there is a conflict
		assertNotNull(state); 				// 1
		assertNotNull(state = state.next); 	// 1 -> 2
		assertNotNull(state = state.next); 	// 2 -> 3
		assertNotNull(state = state.next);  // 3 -> 4
		// state 4
		assertNull(state.resolveConflicts(null, false));
		/*
            4:
               shift:
                   "+" -> 3
               reduce:
                   EOF -> Expr = Expr "+" Expr
    	 */
    	assertNotNull(state.shift);
    	ParserAction.Shift shift = (ParserAction.Shift) state.shift;
    	assertEquals("\"+\"", shift.lookahead.toString());
    	assertEquals(3, shift.dest.id);
    	assertNull(shift.next);
    	
    	assertNotNull(state.reduce);
    	ParserAction.Reduce reduce = (ParserAction.Reduce) state.reduce;
    	assertEquals("EOF", reduce.lookahead.toString());
    	assertEquals("Expr = { Add } Expr \"+\" Expr", reduce.production.toString());
    	assertNull(reduce.next);
    	
    	assertNotNull(state = state.next); // 4 -> 5
    	assertNull(state.next);
	}

	@Test
	public void testConflictResolutionWithSameExplicitPrecedence_LeftAssoc()
	{
		GrammarFactory fact = new GrammarFactory(new String[] {"NUM"});
		fact.def("Expr")
			.sym("NUM")
			.end();
		fact.def("Expr", "Add")
			.sym("Expr")
			.txt("+")
			.sym("Expr")
			.end();
		fact.left()
			.prec("+")
			.prec("Expr", "Add");
		Grammar grammar = fact.getGrammar();
		ParserState state = new ParserStatesBuilder().buildParserStates(grammar);
		// find state 4, where there is a conflict
		assertNotNull(state); 				// 1
		assertNotNull(state = state.next); 	// 1 -> 2
		assertNotNull(state = state.next); 	// 2 -> 3
		assertNotNull(state = state.next);  // 3 -> 4
		// state 4
		assertNull(state.resolveConflicts(null, false));
		/*
            4:
               reduce:
                   "+" -> Expr = Expr "+" Expr
                   EOF -> Expr = Expr "+" Expr
    	 */
    	assertNull(state.shift);
    	
    	assertNotNull(state.reduce);
    	ParserAction.Reduce reduce = (ParserAction.Reduce) state.reduce;
    	assertEquals("\"+\"", reduce.lookahead.toString());
    	assertEquals("Expr = { Add } Expr \"+\" Expr", reduce.production.toString());
    	assertNotNull(reduce.next);
    	reduce = (ParserAction.Reduce) reduce.next;
    	assertEquals("EOF", reduce.lookahead.toString());
    	assertEquals("Expr = { Add } Expr \"+\" Expr", reduce.production.toString());
    	assertNull(reduce.next);
    	
    	state = state.next; // 4 -> 5
    	assertNull(state.next);
	}
	
	@Test
	public void exprWithPrecedenceConflictsShouldBeResolved()
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
		/* 
		 * Initial parser actions:
		 * 
              1: shift (, NUM, Expr
            
              2: shift /, *, -, +
                   EOF Goal = Expr ;
            
              3: shift (, NUM, Expr
            
              4: shift /, *, -, +
                     / Expr = { Add } Expr + Expr ;
                     * Expr = { Add } Expr + Expr ;
                     - Expr = { Add } Expr + Expr ;
                     + Expr = { Add } Expr + Expr ;
                     ) Expr = { Add } Expr + Expr ;
                   EOF Expr = { Add } Expr + Expr ;
            
              5: shift (, NUM, Expr
            
              6: shift /, *, -, +
                     / Expr = { Sub } Expr - Expr ;
                     * Expr = { Sub } Expr - Expr ;
                     - Expr = { Sub } Expr - Expr ;
                     + Expr = { Sub } Expr - Expr ;
                     ) Expr = { Sub } Expr - Expr ;
                   EOF Expr = { Sub } Expr - Expr ;
            
              7: shift (, NUM, Expr
            
              8: shift /, *, -, +
                     / Expr = { Mul } Expr * Expr ;
                     * Expr = { Mul } Expr * Expr ;
                     - Expr = { Mul } Expr * Expr ;
                     + Expr = { Mul } Expr * Expr ;
                     ) Expr = { Mul } Expr * Expr ;
                   EOF Expr = { Mul } Expr * Expr ;
            
              9: shift (, NUM, Expr
            
             10: shift /, *, -, +
                     / Expr = { Div } Expr / Expr ;
                     * Expr = { Div } Expr / Expr ;
                     - Expr = { Div } Expr / Expr ;
                     + Expr = { Div } Expr / Expr ;
                     ) Expr = { Div } Expr / Expr ;
                   EOF Expr = { Div } Expr / Expr ;
            
             11:     / Expr = { Number } NUM ;
                     * Expr = { Number } NUM ;
                     - Expr = { Number } NUM ;
                     + Expr = { Number } NUM ;
                     ) Expr = { Number } NUM ;
                   EOF Expr = { Number } NUM ;
            
             12: shift (, NUM, Expr
            
             13: shift /, *, -, +, )
            
             14:     / Expr = { Nested } ( Expr ) ;
                     * Expr = { Nested } ( Expr ) ;
                     - Expr = { Nested } ( Expr ) ;
                     + Expr = { Nested } ( Expr ) ;
                     ) Expr = { Nested } ( Expr ) ;
                   EOF Expr = { Nested } ( Expr ) ;
         *
		 */
		// go to state 4
		ParserState state = firstState;
		state = state.next; // 2
		state = state.next; // 3
		state = state.next; // 4
		ParserAction.Conflict conflict = state.resolveConflicts(null, false);
		assertNull(conflict);
		assertUniqueLookaheads(state);
		state = state.next; // 5
		state = state.next; // 6
		conflict = state.resolveConflicts(null, false);
		assertNull(conflict);
		assertUniqueLookaheads(state);		
		state = state.next; // 7
		state = state.next; // 8
		conflict = state.resolveConflicts(null, false);
		assertNull(conflict);
		assertUniqueLookaheads(state);		
		state = state.next; // 9
		state = state.next; // 10
		conflict = state.resolveConflicts(null, false);
		assertNull(conflict);
		assertUniqueLookaheads(state);		
	}
	
	private static void assertUniqueLookaheads(ParserState state)
	{
		Set<Symbol> lookaheads = new HashSet<Symbol>();
        for (ParserAction action = state.shift; action != null; action = action.next)
        {
        	assertTrue(lookaheads.add(action.lookahead));
        }
        for (ParserAction action = state.reduce; action != null; action = action.next)
        {
        	assertTrue(lookaheads.add(action.lookahead));
        }
	}
}
