package beaver.parser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class CompressedParsingTablesTest
{
	private Grammar grammar;
	private ParserState firstState;
	
	@Before
	public void compileTest()
	{
		GrammarFactory fact = new GrammarFactory(new String[] {"NUM", "ID"});
		fact.def("Eval", "Expr")
			.sym("Expr")
			.end();
		fact.def("Eval", "Stmt")
			.sym("Stmt")
			.end();
		fact.def("Stmt")
			.sym("ID")
			.txt("=")
			.sym("Expr")
			.txt(";")
			.end();
		fact.def("Expr", "Number")
			.sym("NUM")
			.end();
		fact.def("Expr", "Var")
			.sym("ID")
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
		this.grammar = fact.getGrammar();
		this.firstState = new ParserStatesBuilder().buildParserStates(grammar);
		for (ParserState state = firstState; state != null; state = state.next)
		{
			ParserAction.Conflict conflict = state.resolveConflicts(null);
			if (conflict != null)
			{
				StringBuilder strb = new StringBuilder(100);
				strb.append("Cannot resolve state ").append(state.id).append(" conflicts:\n");
			    for (; conflict != null; conflict = conflict.next)
			    {
			    	strb.append("  ").append(conflict.action1).append(" or ").append(conflict.action2).append('\n');
			    }
			    throw new IllegalStateException(strb.toString());
			}
		}
//		print(grammar.productions);
		/*
             -1: Goal = Eval 
             -2: Eval = { Expr } Expr 
             -3: Eval = { Stmt } Stmt 
             -4: Stmt = ID = Expr ; 
             -5: Expr = { Number } NUM 
             -6: Expr = { Var } ID 
             -7: Expr = { Nested } ( Expr ) 
             -8: Expr = { Add } Expr + Expr 
             -9: Expr = { Sub } Expr - Expr 
            -10: Expr = { Mul } Expr * Expr 
            -11: Expr = { Div } Expr / Expr 
		 */
//		for (ParserState state = firstState; state != null; state = state.next)
//		{
//			System.out.println(state);
//		}		
		/*
          1:
           shift:
                 ( -> 14
                ID -> 18
               NUM -> 12
              Stmt -> 17
              Expr -> 3
              Eval -> 2
           accept:
              Goal -> ACCEPT
        
          2:
           reduce:
               EOF -> Goal = Eval ;
        
          3:
           shift:
                 / -> 10
                 * -> 8
                 - -> 6
                 + -> 4
           reduce:
               EOF -> Eval = { Expr } Expr ;
        
          4:
           shift:
                 ( -> 14
                ID -> 13
               NUM -> 12
              Expr -> 5
        
          5:
           shift:
                 / -> 10
                 * -> 8
           reduce:
                 - -> Expr = { Add } Expr + Expr ;
                 + -> Expr = { Add } Expr + Expr ;
                 ) -> Expr = { Add } Expr + Expr ;
                 ; -> Expr = { Add } Expr + Expr ;
               EOF -> Expr = { Add } Expr + Expr ;
        
          6:
           shift:
                 ( -> 14
                ID -> 13
               NUM -> 12
              Expr -> 7
        
          7:
           shift:
                 / -> 10
                 * -> 8
           reduce:
                 - -> Expr = { Sub } Expr - Expr ;
                 + -> Expr = { Sub } Expr - Expr ;
                 ) -> Expr = { Sub } Expr - Expr ;
                 ; -> Expr = { Sub } Expr - Expr ;
               EOF -> Expr = { Sub } Expr - Expr ;
        
          8:
           shift:
                 ( -> 14
                ID -> 13
               NUM -> 12
              Expr -> 9
        
          9:
           reduce:
                 / -> Expr = { Mul } Expr * Expr ;
                 * -> Expr = { Mul } Expr * Expr ;
                 - -> Expr = { Mul } Expr * Expr ;
                 + -> Expr = { Mul } Expr * Expr ;
                 ) -> Expr = { Mul } Expr * Expr ;
                 ; -> Expr = { Mul } Expr * Expr ;
               EOF -> Expr = { Mul } Expr * Expr ;
        
         10:
           shift:
                 ( -> 14
                ID -> 13
               NUM -> 12
              Expr -> 11
        
         11:
           reduce:
                 / -> Expr = { Div } Expr / Expr ;
                 * -> Expr = { Div } Expr / Expr ;
                 - -> Expr = { Div } Expr / Expr ;
                 + -> Expr = { Div } Expr / Expr ;
                 ) -> Expr = { Div } Expr / Expr ;
                 ; -> Expr = { Div } Expr / Expr ;
               EOF -> Expr = { Div } Expr / Expr ;
        
         12:
           reduce:
                 / -> Expr = { Number } NUM ;
                 * -> Expr = { Number } NUM ;
                 - -> Expr = { Number } NUM ;
                 + -> Expr = { Number } NUM ;
                 ) -> Expr = { Number } NUM ;
                 ; -> Expr = { Number } NUM ;
               EOF -> Expr = { Number } NUM ;
        
         13:
           reduce:
                 / -> Expr = { Var } ID ;
                 * -> Expr = { Var } ID ;
                 - -> Expr = { Var } ID ;
                 + -> Expr = { Var } ID ;
                 ) -> Expr = { Var } ID ;
                 ; -> Expr = { Var } ID ;
               EOF -> Expr = { Var } ID ;
        
         14:
           shift:
                 ( -> 14
                ID -> 13
               NUM -> 12
              Expr -> 15
        
         15:
           shift:
                 / -> 10
                 * -> 8
                 - -> 6
                 + -> 4
                 ) -> 16
        
         16:
           reduce:
                 / -> Expr = { Nested } ( Expr ) ;
                 * -> Expr = { Nested } ( Expr ) ;
                 - -> Expr = { Nested } ( Expr ) ;
                 + -> Expr = { Nested } ( Expr ) ;
                 ) -> Expr = { Nested } ( Expr ) ;
                 ; -> Expr = { Nested } ( Expr ) ;
               EOF -> Expr = { Nested } ( Expr ) ;
        
         17:
           reduce:
               EOF -> Eval = { Stmt } Stmt ;
        
         18:
           shift:
                 = -> 19
           reduce:
                 / -> Expr = { Var } ID ;
                 * -> Expr = { Var } ID ;
                 - -> Expr = { Var } ID ;
                 + -> Expr = { Var } ID ;
               EOF -> Expr = { Var } ID ;
        
         19:
           shift:
                 ( -> 14
                ID -> 13
               NUM -> 12
              Expr -> 20
        
         20:
           shift:
                 / -> 10
                 * -> 8
                 - -> 6
                 + -> 4
                 ; -> 21
        
         21:
           reduce:
               EOF -> Stmt = ID = Expr ; ;

		 */
	}
	
	@Test
	public void testCompression()
	{
		/*
              EOF
              : =
              : : ;
              : : : (
              : : : : )
              : : : : : +
              : : : : : : -
              : : : : : : : *
              : : : : : : : : /
              : : : : : : : : : NUM
              : : : : : : : : : : ID
              : : : : : : : : : : : Eval
              : : : : : : : : : : : : Expr
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
           1:       x           x x x x x x
           2: x                            
           3: x         x x x x            
           4:       x           x x   x    
           5: x   x   x x x x x            
           6:       x           x x   x    
           7: x   x   x x x x x            
           8:       x           x x   x    
           9: x   x   x x x x x            
          10:       x           x x   x    
          11: x   x   x x x x x            
          12: x   x   x x x x x            
          13: x   x   x x x x x            
          14:       x           x x   x    
          15:         x x x x x            
          16: x   x   x x x x x            
          17: x                            
          18: x x       x x x x            
          19:       x           x x   x    
          20:     x     x x x x            
          21: x                            
		 */
		CompressedParsingTables tables = new CompressedParsingTables();
		tables.init(firstState);
		// print(tables);
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
           5: x x x x x x x                
           7: x x x x x x x                
           9: x x x x x x x                
          11: x x x x x x x                
          12: x x x x x x x                
          13: x x x x x x x                
          16: x x x x x x x                
           1:               x x x x   x x x
          18: x x x x x             x      
           3: x x x x x                    
          15:   x x x x   x                
          20:   x x x x x                  
           4:               x x x x        
           6:               x x x x        
           8:               x x x x        
          10:               x x x x        
          14:               x x x x        
          19:               x x x x        
           2: x                            
          17: x                            
          21: x                            
		 */
		assertEquals( 5, tables.states[0].id);	
		assertEquals( 0, tables.stateActionsMinIdx[0]);
		assertEquals( 6, tables.stateActionsMaxIdx[0]);
		
		assertEquals( 7, tables.states[1].id);	
		assertEquals( 0, tables.stateActionsMinIdx[1]);
		assertEquals( 6, tables.stateActionsMaxIdx[1]);
		
		assertEquals( 9, tables.states[2].id);	
		assertEquals( 0, tables.stateActionsMinIdx[2]);
		assertEquals( 6, tables.stateActionsMaxIdx[2]);
		
		assertEquals(11, tables.states[3].id);	
		assertEquals( 0, tables.stateActionsMinIdx[3]);
		assertEquals( 6, tables.stateActionsMaxIdx[3]);
		
		assertEquals(12, tables.states[4].id);	
		assertEquals( 0, tables.stateActionsMinIdx[4]);
		assertEquals( 6, tables.stateActionsMaxIdx[4]);
		
		assertEquals(13, tables.states[5].id);	
		assertEquals( 0, tables.stateActionsMinIdx[5]);
		assertEquals( 6, tables.stateActionsMaxIdx[5]);
		
		assertEquals(16, tables.states[6].id);	
		assertEquals( 0, tables.stateActionsMinIdx[6]);
		assertEquals( 6, tables.stateActionsMaxIdx[6]);
		
		assertEquals( 1, tables.states[7].id);	
		assertEquals( 7, tables.stateActionsMinIdx[7]);
		assertEquals(14, tables.stateActionsMaxIdx[7]);
		
		assertEquals(18, tables.states[8].id);	
		assertEquals( 0, tables.stateActionsMinIdx[8]);
		assertEquals(11, tables.stateActionsMaxIdx[8]);
		
		assertEquals( 3, tables.states[9].id);	
		assertEquals( 0, tables.stateActionsMinIdx[9]);
		assertEquals( 4, tables.stateActionsMaxIdx[9]);
		
		assertEquals(15, tables.states[10].id);	
		assertEquals( 1, tables.stateActionsMinIdx[10]);
		assertEquals( 6, tables.stateActionsMaxIdx[10]);
		
		assertEquals(20, tables.states[11].id);	
		assertEquals( 1, tables.stateActionsMinIdx[11]);
		assertEquals( 5, tables.stateActionsMaxIdx[11]);
		
		assertEquals( 4, tables.states[12].id);	
		assertEquals( 7, tables.stateActionsMinIdx[12]);
		assertEquals(10, tables.stateActionsMaxIdx[12]);
		
		assertEquals( 6, tables.states[13].id);	
		assertEquals( 7, tables.stateActionsMinIdx[13]);
		assertEquals(10, tables.stateActionsMaxIdx[13]);
		
		assertEquals( 8, tables.states[14].id);	
		assertEquals( 7, tables.stateActionsMinIdx[14]);
		assertEquals(10, tables.stateActionsMaxIdx[14]);
		
		assertEquals(10, tables.states[15].id);	
		assertEquals( 7, tables.stateActionsMinIdx[15]);
		assertEquals(10, tables.stateActionsMaxIdx[15]);
		
		assertEquals(14, tables.states[16].id);	
		assertEquals( 7, tables.stateActionsMinIdx[16]);
		assertEquals(10, tables.stateActionsMaxIdx[16]);
		
		assertEquals(19, tables.states[17].id);	
		assertEquals( 7, tables.stateActionsMinIdx[17]);
		assertEquals(10, tables.stateActionsMaxIdx[17]);
		
		assertEquals( 2, tables.states[18].id);	
		assertEquals( 0, tables.stateActionsMinIdx[18]);
		assertEquals( 0, tables.stateActionsMaxIdx[18]);
		
		assertEquals(17, tables.states[19].id);	
		assertEquals( 0, tables.stateActionsMinIdx[19]);
		assertEquals( 0, tables.stateActionsMaxIdx[19]);
		
		assertEquals(21, tables.states[20].id);	
		assertEquals( 0, tables.stateActionsMinIdx[20]);
		assertEquals( 0, tables.stateActionsMaxIdx[20]);
		
		int[] stateActions = new int[tables.symbols.length];
		assertEquals(15, stateActions.length);
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
           5: x x x x x x x                
		 * 
          5:
           shift:
                 / -> 10
                 * -> 8
           reduce:
                 - -> Expr = { Add } Expr + Expr ;
                 + -> Expr = { Add } Expr + Expr ;
                 ) -> Expr = { Add } Expr + Expr ;
                 ; -> Expr = { Add } Expr + Expr ;
               EOF -> Expr = { Add } Expr + Expr ;
		 */
		tables.loadStateActions(0, stateActions);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, 0, 0, 0, 0, 0, 0, 0, 0}, stateActions);	
		tables.packStateActions(0, stateActions);
		assertEquals(7, tables.packedActionsSize);
		assertEquals(0, tables.stateActionsOffset[0]);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8}, getPackedActions(tables));	
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 6}, getCtrlLookaheadIndexes(tables));	
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
           7: x x x x x x x                
		 * 
          7:
           shift:
                 / -> 10
                 * -> 8
           reduce:
                 - -> Expr = { Sub } Expr - Expr ;
                 + -> Expr = { Sub } Expr - Expr ;
                 ) -> Expr = { Sub } Expr - Expr ;
                 ; -> Expr = { Sub } Expr - Expr ;
               EOF -> Expr = { Sub } Expr - Expr ;
		 */
		tables.loadStateActions(1, stateActions);
		assertArrayEquals(new int[] {-9, -9, -9, 8, 10, -9, -9, 0, 0, 0, 0, 0, 0, 0, 0}, stateActions);	
		tables.packStateActions(1, stateActions);
		assertEquals(14, tables.packedActionsSize);
		assertEquals(7, tables.stateActionsOffset[1]);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9}, getPackedActions(tables));	
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6}, getCtrlLookaheadIndexes(tables));
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
           9: x x x x x x x                
		 * 
          9:
           reduce:
                 / -> Expr = { Mul } Expr * Expr ;
                 * -> Expr = { Mul } Expr * Expr ;
                 - -> Expr = { Mul } Expr * Expr ;
                 + -> Expr = { Mul } Expr * Expr ;
                 ) -> Expr = { Mul } Expr * Expr ;
                 ; -> Expr = { Mul } Expr * Expr ;
               EOF -> Expr = { Mul } Expr * Expr ;
		 */
		tables.loadStateActions(2, stateActions);
		assertArrayEquals(new int[] {-10, -10, -10, -10, -10, -10, -10, 0, 0, 0, 0, 0, 0, 0, 0}, stateActions);	
		tables.packStateActions(2, stateActions);
		assertEquals(21, tables.packedActionsSize);
		assertEquals(14, tables.stateActionsOffset[2]);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10}, getPackedActions(tables));	
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6}, getCtrlLookaheadIndexes(tables));
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
          11: x x x x x x x                
		 * 
         11:
           reduce:
                 / -> Expr = { Div } Expr / Expr ;
                 * -> Expr = { Div } Expr / Expr ;
                 - -> Expr = { Div } Expr / Expr ;
                 + -> Expr = { Div } Expr / Expr ;
                 ) -> Expr = { Div } Expr / Expr ;
                 ; -> Expr = { Div } Expr / Expr ;
               EOF -> Expr = { Div } Expr / Expr ;
		 */
		tables.loadStateActions(3, stateActions);
		assertArrayEquals(new int[] {-11, -11, -11, -11, -11, -11, -11, 0, 0, 0, 0, 0, 0, 0, 0}, stateActions);	
		tables.packStateActions(3, stateActions);
		assertEquals(28, tables.packedActionsSize);
		assertEquals(21, tables.stateActionsOffset[3]);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11}, getPackedActions(tables));	
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6}, getCtrlLookaheadIndexes(tables));
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
          12: x x x x x x x                
		 * 
         12:
           reduce:
                 / -> Expr = { Number } NUM ;
                 * -> Expr = { Number } NUM ;
                 - -> Expr = { Number } NUM ;
                 + -> Expr = { Number } NUM ;
                 ) -> Expr = { Number } NUM ;
                 ; -> Expr = { Number } NUM ;
               EOF -> Expr = { Number } NUM ;
		 */
		tables.loadStateActions(4, stateActions);
		assertArrayEquals(new int[] {-5, -5, -5, -5, -5, -5, -5, 0, 0, 0, 0, 0, 0, 0, 0}, stateActions);	
		tables.packStateActions(4, stateActions);
		assertEquals(35, tables.packedActionsSize);
		assertEquals(28, tables.stateActionsOffset[4]);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -5, -5, -5, -5, -5, -5, -5}, getPackedActions(tables));	
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6}, getCtrlLookaheadIndexes(tables));
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
          13: x x x x x x x                
		 * 
         13:
           reduce:
                 / -> Expr = { Var } ID ;
                 * -> Expr = { Var } ID ;
                 - -> Expr = { Var } ID ;
                 + -> Expr = { Var } ID ;
                 ) -> Expr = { Var } ID ;
                 ; -> Expr = { Var } ID ;
               EOF -> Expr = { Var } ID ;
		 */
		tables.loadStateActions(5, stateActions);
		assertArrayEquals(new int[] {-6, -6, -6, -6, -6, -6, -6, 0, 0, 0, 0, 0, 0, 0, 0}, stateActions);	
		tables.packStateActions(5, stateActions);
		assertEquals(42, tables.packedActionsSize);
		assertEquals(35, tables.stateActionsOffset[5]);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -5, -5, -5, -5, -5, -5, -5, -6, -6, -6, -6, -6, -6, -6}, getPackedActions(tables));	
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6}, getCtrlLookaheadIndexes(tables));
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
          16: x x x x x x x                
		 * 
         16:
           reduce:
                 / -> Expr = { Nested } ( Expr ) ;
                 * -> Expr = { Nested } ( Expr ) ;
                 - -> Expr = { Nested } ( Expr ) ;
                 + -> Expr = { Nested } ( Expr ) ;
                 ) -> Expr = { Nested } ( Expr ) ;
                 ; -> Expr = { Nested } ( Expr ) ;
               EOF -> Expr = { Nested } ( Expr ) ;
		 */
		tables.loadStateActions(6, stateActions);
		assertArrayEquals(new int[] {-7, -7, -7, -7, -7, -7, -7, 0, 0, 0, 0, 0, 0, 0, 0}, stateActions);	
		tables.packStateActions(6, stateActions);
		assertEquals(49, tables.packedActionsSize);
		assertEquals(42, tables.stateActionsOffset[6]);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -5, -5, -5, -5, -5, -5, -5, -6, -6, -6, -6, -6, -6, -6, -7, -7, -7, -7, -7, -7, -7}, getPackedActions(tables));	
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6}, getCtrlLookaheadIndexes(tables));
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
           1:               x x x x   x x x
         *
              EOF + - * / ; ) ( NUM ID Expr = Eval Stmt
              0   1 2 3 4 5 6 7 8   9  10  11 12   13
		 * 
          1:
           shift:
                 ( -> 14
                ID -> 18
               NUM -> 12
              Stmt -> 17
              Expr -> 3
              Eval -> 2
           accept:
              Goal -> ACCEPT
		 */
		tables.loadStateActions(7, stateActions);
		assertArrayEquals(new int[] {0, 0, 0, 0, 0, 0, 0, 14, 12, 18, 3, 0, 2, 17, -12}, stateActions);	
		tables.packStateActions(7, stateActions);
		assertEquals(57, tables.packedActionsSize);
		assertEquals(42, tables.stateActionsOffset[7]);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -5, -5, -5, -5, -5, -5, -5, -6, -6, -6, -6, -6, -6, -6, -7, -7, -7, -7, -7, -7, -7, 14, 12, 18, 3, 0, 2, 17, -12}, getPackedActions(tables));	
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, -1, 12, 13, 14}, getCtrlLookaheadIndexes(tables));
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
          18: x x x x x             x    
		 * 
         18:
           shift:
                 = -> 19
           reduce:
                 / -> Expr = { Var } ID ;
                 * -> Expr = { Var } ID ;
                 - -> Expr = { Var } ID ;
                 + -> Expr = { Var } ID ;
               EOF -> Expr = { Var } ID ;
		 */
		tables.loadStateActions(8, stateActions);
		assertArrayEquals(new int[] {-6, -6, -6, -6, -6, 0, 0, 0, 0, 0, 0, 19, 0, 0, 0}, stateActions);	
		tables.packStateActions(8, stateActions);
		assertEquals(69, tables.packedActionsSize);
		assertEquals(57, tables.stateActionsOffset[8]);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -5, -5, -5, -5, -5, -5, -5, -6, -6, -6, -6, -6, -6, -6, -7, -7, -7, -7, -7, -7, -7, 14, 12, 18, 3, 0, 2, 17, -12, -6, -6, -6, -6, -6, 0, 0, 0, 0, 0, 0, 19}, getPackedActions(tables));	
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, -1, 12, 13, 14, 0, 1, 2, 3, 4, -1, -1, -1, -1, -1, -1, 11}, getCtrlLookaheadIndexes(tables));
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
           3: x x x x x                  
		 * 
          3:
           shift:
                 / -> 10
                 * -> 8
                 - -> 6
                 + -> 4
           reduce:
               EOF -> Eval = { Expr } Expr ;
		 */
		tables.loadStateActions(9, stateActions);
		assertArrayEquals(new int[] {-2, 4, 6, 8, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, stateActions);	
		tables.packStateActions(9, stateActions);
		assertEquals(69, tables.packedActionsSize);
		assertEquals(62, tables.stateActionsOffset[9]);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -5, -5, -5, -5, -5, -5, -5, -6, -6, -6, -6, -6, -6, -6, -7, -7, -7, -7, -7, -7, -7, 14, 12, 18, 3, 0, 2, 17, -12, -6, -6, -6, -6, -6, -2, 4, 6, 8, 10, 0, 19}, getPackedActions(tables));	
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, -1, 12, 13, 14, 0, 1, 2, 3, 4, 0, 1, 2, 3, 4, -1, 11}, getCtrlLookaheadIndexes(tables));
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
          15:   x x x x   x              
		 * 
         15:
           shift:
                 / -> 10
                 * -> 8
                 - -> 6
                 + -> 4
                 ) -> 16
		 */
		tables.loadStateActions(10, stateActions);
		assertArrayEquals(new int[] {0, 4, 6, 8, 10, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0}, stateActions);	
		tables.packStateActions(10, stateActions);
		assertEquals(75, tables.packedActionsSize);
		assertEquals(68, tables.stateActionsOffset[10]);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -5, -5, -5, -5, -5, -5, -5, -6, -6, -6, -6, -6, -6, -6, -7, -7, -7, -7, -7, -7, -7, 14, 12, 18, 3, 0, 2, 17, -12, -6, -6, -6, -6, -6, -2, 4, 6, 8, 10, 0, 19, 4, 6, 8, 10, 0, 16}, getPackedActions(tables));	
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, -1, 12, 13, 14, 0, 1, 2, 3, 4, 0, 1, 2, 3, 4, -1, 11, 1, 2, 3, 4, -1, 6}, getCtrlLookaheadIndexes(tables));
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
          20:   x x x x x                
		 * 
         20:
           shift:
                 / -> 10
                 * -> 8
                 - -> 6
                 + -> 4
                 ; -> 21
		 */
		tables.loadStateActions(11, stateActions);
		assertArrayEquals(new int[] {0, 4, 6, 8, 10, 21, 0, 0, 0, 0, 0, 0, 0, 0, 0}, stateActions);	
		tables.packStateActions(11, stateActions);
		/*
		 * Note: this state actions intersect without conflicts with actions of state 3  
		 */
		assertEquals(75, tables.packedActionsSize);
		assertEquals(62, tables.stateActionsOffset[11]);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -5, -5, -5, -5, -5, -5, -5, -6, -6, -6, -6, -6, -6, -6, -7, -7, -7, -7, -7, -7, -7, 14, 12, 18, 3, 0, 2, 17, -12, -6, -6, -6, -6, -6, -2, 4, 6, 8, 10, 21, 19, 4, 6, 8, 10, 0, 16}, getPackedActions(tables));	
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, -1, 12, 13, 14, 0, 1, 2, 3, 4, 0, 1, 2, 3, 4, 5, 11, 1, 2, 3, 4, -1, 6}, getCtrlLookaheadIndexes(tables));
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
           4:               x x x x      
		 * 
          4:
           shift:
                 ( -> 14
                ID -> 13
               NUM -> 12
              Expr -> 5
		 */
		tables.loadStateActions(12, stateActions);
		assertArrayEquals(new int[] {0, 0, 0, 0, 0, 0, 0, 14, 12, 13, 5, 0, 0, 0, 0}, stateActions);	
		tables.packStateActions(12, stateActions);
		assertEquals(79, tables.packedActionsSize);
		assertEquals(68, tables.stateActionsOffset[12]);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -5, -5, -5, -5, -5, -5, -5, -6, -6, -6, -6, -6, -6, -6, -7, -7, -7, -7, -7, -7, -7, 14, 12, 18, 3, 0, 2, 17, -12, -6, -6, -6, -6, -6, -2, 4, 6, 8, 10, 21, 19, 4, 6, 8, 10, 0, 16, 14, 12, 13, 5}, getPackedActions(tables));	
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, -1, 12, 13, 14, 0, 1, 2, 3, 4, 0, 1, 2, 3, 4, 5, 11, 1, 2, 3, 4, -1, 6, 7, 8, 9, 10}, getCtrlLookaheadIndexes(tables));
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
           6:               x x x x      
		 * 
          6:
           shift:
                 ( -> 14
                ID -> 13
               NUM -> 12
              Expr -> 7
		 */
		tables.loadStateActions(13, stateActions);
		assertArrayEquals(new int[] {0, 0, 0, 0, 0, 0, 0, 14, 12, 13, 7, 0, 0, 0, 0}, stateActions);	
		tables.packStateActions(13, stateActions);
		assertEquals(83, tables.packedActionsSize);
		assertEquals(72, tables.stateActionsOffset[13]);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -5, -5, -5, -5, -5, -5, -5, -6, -6, -6, -6, -6, -6, -6, -7, -7, -7, -7, -7, -7, -7, 14, 12, 18, 3, 0, 2, 17, -12, -6, -6, -6, -6, -6, -2, 4, 6, 8, 10, 21, 19, 4, 6, 8, 10, 0, 16, 14, 12, 13, 5, 14, 12, 13, 7}, getPackedActions(tables));	
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, -1, 12, 13, 14, 0, 1, 2, 3, 4, 0, 1, 2, 3, 4, 5, 11, 1, 2, 3, 4, -1, 6, 7, 8, 9, 10, 7, 8, 9, 10}, getCtrlLookaheadIndexes(tables));
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
           8:               x x x x      
		 * 
          8:
           shift:
                 ( -> 14
                ID -> 13
               NUM -> 12
              Expr -> 9
		 */
		tables.loadStateActions(14, stateActions);
		assertArrayEquals(new int[] {0, 0, 0, 0, 0, 0, 0, 14, 12, 13, 9, 0, 0, 0, 0}, stateActions);	
		tables.packStateActions(14, stateActions);
		assertEquals(87, tables.packedActionsSize);
		assertEquals(76, tables.stateActionsOffset[14]);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -5, -5, -5, -5, -5, -5, -5, -6, -6, -6, -6, -6, -6, -6, -7, -7, -7, -7, -7, -7, -7, 14, 12, 18, 3, 0, 2, 17, -12, -6, -6, -6, -6, -6, -2, 4, 6, 8, 10, 21, 19, 4, 6, 8, 10, 0, 16, 14, 12, 13, 5, 14, 12, 13, 7, 14, 12, 13, 9}, getPackedActions(tables));	
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, -1, 12, 13, 14, 0, 1, 2, 3, 4, 0, 1, 2, 3, 4, 5, 11, 1, 2, 3, 4, -1, 6, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10}, getCtrlLookaheadIndexes(tables));
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
          10:               x x x x      
		 * 
         10:
           shift:
                 ( -> 14
                ID -> 13
               NUM -> 12
              Expr -> 11
		 */
		tables.loadStateActions(15, stateActions);
		assertArrayEquals(new int[] {0, 0, 0, 0, 0, 0, 0, 14, 12, 13, 11, 0, 0, 0, 0}, stateActions);	
		tables.packStateActions(15, stateActions);
		assertEquals(91, tables.packedActionsSize);
		assertEquals(80, tables.stateActionsOffset[15]);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -5, -5, -5, -5, -5, -5, -5, -6, -6, -6, -6, -6, -6, -6, -7, -7, -7, -7, -7, -7, -7, 14, 12, 18, 3, 0, 2, 17, -12, -6, -6, -6, -6, -6, -2, 4, 6, 8, 10, 21, 19, 4, 6, 8, 10, 0, 16, 14, 12, 13, 5, 14, 12, 13, 7, 14, 12, 13, 9, 14, 12, 13, 11}, getPackedActions(tables));	
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, -1, 12, 13, 14, 0, 1, 2, 3, 4, 0, 1, 2, 3, 4, 5, 11, 1, 2, 3, 4, -1, 6, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10}, getCtrlLookaheadIndexes(tables));
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
          14:               x x x x      
		 * 
         14:
           shift:
                 ( -> 14
                ID -> 13
               NUM -> 12
              Expr -> 15
		 */
		tables.loadStateActions(16, stateActions);
		assertArrayEquals(new int[] {0, 0, 0, 0, 0, 0, 0, 14, 12, 13, 15, 0, 0, 0, 0}, stateActions);	
		tables.packStateActions(16, stateActions);
		assertEquals(95, tables.packedActionsSize);
		assertEquals(84, tables.stateActionsOffset[16]);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -5, -5, -5, -5, -5, -5, -5, -6, -6, -6, -6, -6, -6, -6, -7, -7, -7, -7, -7, -7, -7, 14, 12, 18, 3, 0, 2, 17, -12, -6, -6, -6, -6, -6, -2, 4, 6, 8, 10, 21, 19, 4, 6, 8, 10, 0, 16, 14, 12, 13, 5, 14, 12, 13, 7, 14, 12, 13, 9, 14, 12, 13, 11, 14, 12, 13, 15}, getPackedActions(tables));	
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, -1, 12, 13, 14, 0, 1, 2, 3, 4, 0, 1, 2, 3, 4, 5, 11, 1, 2, 3, 4, -1, 6, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10}, getCtrlLookaheadIndexes(tables));
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
          19:               x x x x      
		 * 
         19:
           shift:
                 ( -> 14
                ID -> 13
               NUM -> 12
              Expr -> 20
		 */
		tables.loadStateActions(17, stateActions);
		assertArrayEquals(new int[] {0, 0, 0, 0, 0, 0, 0, 14, 12, 13, 20, 0, 0, 0, 0}, stateActions);	
		tables.packStateActions(17, stateActions);
		assertEquals(99, tables.packedActionsSize);
		assertEquals(88, tables.stateActionsOffset[17]);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -5, -5, -5, -5, -5, -5, -5, -6, -6, -6, -6, -6, -6, -6, -7, -7, -7, -7, -7, -7, -7, 14, 12, 18, 3, 0, 2, 17, -12, -6, -6, -6, -6, -6, -2, 4, 6, 8, 10, 21, 19, 4, 6, 8, 10, 0, 16, 14, 12, 13, 5, 14, 12, 13, 7, 14, 12, 13, 9, 14, 12, 13, 11, 14, 12, 13, 15, 14, 12, 13, 20}, getPackedActions(tables));	
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, -1, 12, 13, 14, 0, 1, 2, 3, 4, 0, 1, 2, 3, 4, 5, 11, 1, 2, 3, 4, -1, 6, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10}, getCtrlLookaheadIndexes(tables));
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
           2: x                          
		 * 
          2:
           reduce:
               EOF -> Goal = Eval ;
		 */
		tables.loadStateActions(18, stateActions);
		assertArrayEquals(new int[] {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, stateActions);	
		tables.packStateActions(18, stateActions);
		assertEquals(99, tables.packedActionsSize);
		assertEquals(53, tables.stateActionsOffset[18]);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -5, -5, -5, -5, -5, -5, -5, -6, -6, -6, -6, -6, -6, -6, -7, -7, -7, -7, -7, -7, -7, 14, 12, 18, 3, -1, 2, 17, -12, -6, -6, -6, -6, -6, -2, 4, 6, 8, 10, 21, 19, 4, 6, 8, 10, 0, 16, 14, 12, 13, 5, 14, 12, 13, 7, 14, 12, 13, 9, 14, 12, 13, 11, 14, 12, 13, 15, 14, 12, 13, 20}, getPackedActions(tables));	
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0, 12, 13, 14, 0, 1, 2, 3, 4, 0, 1, 2, 3, 4, 5, 11, 1, 2, 3, 4, -1, 6, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10}, getCtrlLookaheadIndexes(tables));
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
          17: x                          
		 * 
         17:
           reduce:
               EOF -> Eval = { Stmt } Stmt ;
		 */
		tables.loadStateActions(19, stateActions);
		assertArrayEquals(new int[] {-3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, stateActions);	
		tables.packStateActions(19, stateActions);
		assertEquals(99, tables.packedActionsSize);
		assertEquals(73, tables.stateActionsOffset[19]);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -5, -5, -5, -5, -5, -5, -5, -6, -6, -6, -6, -6, -6, -6, -7, -7, -7, -7, -7, -7, -7, 14, 12, 18, 3, -1, 2, 17, -12, -6, -6, -6, -6, -6, -2, 4, 6, 8, 10, 21, 19, 4, 6, 8, 10, -3, 16, 14, 12, 13, 5, 14, 12, 13, 7, 14, 12, 13, 9, 14, 12, 13, 11, 14, 12, 13, 15, 14, 12, 13, 20}, getPackedActions(tables));	
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0, 12, 13, 14, 0, 1, 2, 3, 4, 0, 1, 2, 3, 4, 5, 11, 1, 2, 3, 4, 0, 6, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10}, getCtrlLookaheadIndexes(tables));
		/*
              EOF
              : +
              : : -
              : : : *
              : : : : /
              : : : : : ;
              : : : : : : )
              : : : : : : : (
              : : : : : : : : NUM
              : : : : : : : : : ID
              : : : : : : : : : : Expr
              : : : : : : : : : : : =
              : : : : : : : : : : : : Eval
              : : : : : : : : : : : : : Stmt
              : : : : : : : : : : : : : : Goal
          21: x                          
		 * 
         21:
           reduce:
               EOF -> Stmt = ID = Expr ;
		 */
		tables.loadStateActions(20, stateActions);
		assertArrayEquals(new int[] {-4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, stateActions);	
		tables.packStateActions(20, stateActions);
		assertEquals(100, tables.packedActionsSize);
		assertEquals( 99, tables.stateActionsOffset[20]);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -5, -5, -5, -5, -5, -5, -5, -6, -6, -6, -6, -6, -6, -6, -7, -7, -7, -7, -7, -7, -7, 14, 12, 18, 3, -1, 2, 17, -12, -6, -6, -6, -6, -6, -2, 4, 6, 8, 10, 21, 19, 4, 6, 8, 10, -3, 16, 14, 12, 13, 5, 14, 12, 13, 7, 14, 12, 13, 9, 14, 12, 13, 11, 14, 12, 13, 15, 14, 12, 13, 20, -4}, getPackedActions(tables));	
		assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0, 12, 13, 14, 0, 1, 2, 3, 4, 0, 1, 2, 3, 4, 5, 11, 1, 2, 3, 4, 0, 6, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 0}, getCtrlLookaheadIndexes(tables));
	}
	
	@Test
	public void testActionPacking()
	{
		CompressedParsingTables tables = new CompressedParsingTables(firstState);
		assertEquals(100, tables.packedActionsSize);
		assertArrayEquals(new int[] {0, 7, 14, 21, 28, 35, 42, 42, 57, 62, 68, 62, 68, 72, 76, 80, 84, 88, 53, 73, 99}, tables.stateActionsOffset);
		assertArrayEquals(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -5, -5, -5, -5, -5, -5, -5, -6, -6, -6, -6, -6, -6, -6, -7, -7, -7, -7, -7, -7, -7, 14, 12, 18, 3, -1, 2, 17, -12, -6, -6, -6, -6, -6, -2, 4, 6, 8, 10, 21, 19, 4, 6, 8, 10, -3, 16, 14, 12, 13, 5, 14, 12, 13, 7, 14, 12, 13, 9, 14, 12, 13, 11, 14, 12, 13, 15, 14, 12, 13, 20, -4}, getPackedActions(tables));	
		assertArrayEquals(new int[] {0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0, 12, 13, 14, 0, 1, 2, 3, 4, 0, 1, 2, 3, 4, 5, 11, 1, 2, 3, 4, 0, 6, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 0}, getCtrlLookaheadIndexes(tables));
	}
	
	@Test
	public void testSerialization() throws IOException
	{
		CompressedParsingTables tables = new CompressedParsingTables(firstState);
		// prepare expected output
		ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
		DataOutputStream data = new DataOutputStream(out);
		data.writeBytes("BPT:");
		/*
            EOF
            : =
            : : ;
            : : : (
            : : : : )
            : : : : : +
            : : : : : : -
            : : : : : : : *
            : : : : : : : : /
            : : : : : : : : : NUM
            : : : : : : : : : : ID
            : : : : : : : : : : : Eval
            : : : : : : : : : : : : Expr
            : : : : : : : : : : : : : Stmt
            : : : : : : : : : : : : : : Goal
		 * 
            EOF
            : +
            : : -
            : : : *
            : : : : /
            : : : : : ;
            : : : : : : )
            : : : : : : : (
            : : : : : : : : NUM
            : : : : : : : : : ID
            : : : : : : : : : : Expr
            : : : : : : : : : : : =
            : : : : : : : : : : : : Eval
            : : : : : : : : : : : : : Stmt
            : : : : : : : : : : : : : : Goal
		 */
		data.writeChar(15); // number of symbols
		writeUnsignedData(new int[] {0, 11, 5, 7, 6, 1, 2, 3, 4, 8, 9, 12, 10, 13, 14}, data);
		data.writeChar(21); // number of states
		/*
		 * Sorted states order
		 *  5  7  9 11 12 13 16  1 18  3 15 20  4  6  8 10 14 19  2 17 21
		 *  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20
		 */
		// stateActionsMinIdx
		data.writeChar( 7);
		data.writeChar( 0);
		data.writeChar( 0);
		data.writeChar( 7);
		data.writeChar( 0);
		data.writeChar( 7);
		data.writeChar( 0);
		data.writeChar( 7);
		data.writeChar( 0);
		data.writeChar( 7);
		data.writeChar( 0);
		data.writeChar( 0);
		data.writeChar( 0);
		data.writeChar( 7);
		data.writeChar( 1);
		data.writeChar( 0);
		data.writeChar( 0);
		data.writeChar( 0);
		data.writeChar( 7);
		data.writeChar( 1);
		data.writeChar( 0);
		// stateActionsMaxIdx
		data.writeChar(14);
		data.writeChar( 0);
		data.writeChar( 4);
		data.writeChar(10);
		data.writeChar( 6);
		data.writeChar(10);
		data.writeChar( 6);
		data.writeChar(10);
		data.writeChar( 6);
		data.writeChar(10);
		data.writeChar( 6);
		data.writeChar( 6);
		data.writeChar( 6);
		data.writeChar(10);
		data.writeChar( 6);
		data.writeChar( 6);
		data.writeChar( 0);
		data.writeChar(11);
		data.writeChar(10);
		data.writeChar( 5);
		data.writeChar( 0);
		// minOffset
		data.writeShort(0);
		/*
		 * Sorted states order
		 *         5  7   9  11  12  13  16   1  18   3  15  20   4   6   8  10  14  19   2  17  21
		 */
//		new int[] {0, 7, 14, 21, 28, 35, 42, 42, 57, 62, 68, 62, 68, 72, 76, 80, 84, 88, 53, 73, 99}
		
		writeUnsignedData(new int[] {42, 53, 62, 68, 0, 72, 7, 76, 14, 80, 21, 28, 35, 84, 68, 42, 73, 57, 88, 62, 99}, data);
		// packed actions size
		data.writeChar(100);
		writeSignedData(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -5, -5, -5, -5, -5, -5, -5, -6, -6, -6, -6, -6, -6, -6, -7, -7, -7, -7, -7, -7, -7, 14, 12, 18, 3, -1, 2, 17, -12, -6, -6, -6, -6, -6, -2, 4, 6, 8, 10, 21, 19, 4, 6, 8, 10, -3, 16, 14, 12, 13, 5, 14, 12, 13, 7, 14, 12, 13, 9, 14, 12, 13, 11, 14, 12, 13, 15, 14, 12, 13, 20, -4}, data);
		writeUnsignedData(new int[] {0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0, 12, 13, 14, 0, 1, 2, 3, 4, 0, 1, 2, 3, 4, 5, 11, 1, 2, 3, 4, 0, 6, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 7, 8, 9, 10, 0}, data);
		
		data.flush();
		byte[] expectedStream = out.toByteArray();
		out.reset();
		
		tables.write(data);
		data.flush();
		byte[] serializedStream = out.toByteArray();
		assertArrayEquals(expectedStream, serializedStream);
	}
	
	private static int[] getPackedActions(CompressedParsingTables tables)
	{
		int[] actions = new int[tables.packedActionsSize];
		System.arraycopy(tables.packedParserActions, 0, actions, 0, actions.length);
		return actions;
	}
	
	private static int[] getCtrlLookaheadIndexes(CompressedParsingTables tables)
	{
		int[] indexes = new int[tables.packedActionsSize];
		System.arraycopy(tables.packedParserActionCtrlIdxs, 0, indexes, 0, indexes.length);
		return indexes;
	}
	
	private static void writeUnsignedData(int[] data, DataOutput out) throws IOException
	{
		for (int i = 0; i < data.length; i++)
        {
	        out.writeChar(data[i]);
        }
	}
	
	private static void writeSignedData(int[] data, DataOutput out) throws IOException
	{
		for (int i = 0; i < data.length; i++)
        {
	        out.writeShort(data[i]);
        }
	}

/*
	private static boolean isLookahead(Symbol symbol, ParserState state)
	{
		for (ParserAction action = state.shift; action != null; action = action.next)
		{
			if (symbol == action.lookahead)
			{
				return true;
			}
		}
		for (ParserAction action = state.reduce; action != null; action = action.next)
		{
			if (symbol == action.lookahead)
			{
				return true;
			}
		}
		return state.accept != null && symbol == state.accept.lookahead;
	}
	
	private static void print(CompressedParsingTables tables)
	{
        for (int i = 0; i < tables.symbols.length; i++)
        {
        	System.out.print("     ");
        	for (int c = i; c > 0; c--)
            {
            	System.out.print(" :");
            }
        	System.out.print(' ');
        	System.out.println(tables.symbols[i]);
        }
		for (int i = 0; i < tables.states.length; i++)
        {
	        ParserState state = tables.states[i];
	        System.out.format("%1$4d:", state.id);
	        
	        for (int j = 0; j < tables.symbols.length; j++)
            {
	            if (isLookahead(tables.symbols[j], 	state))
	            {
	            	System.out.print(' ');
	            	System.out.print('x');
	            }
	            else
	            {
	            	System.out.print("  ");
	            }
            }
	        System.out.println();
        }
	}
	
	private static void print(Production[] productions)
	{
		for (int i = 0; i < productions.length; i++)
		{
			System.out.format("%1$3d: ", ~productions[i].id);
			System.out.println(productions[i]);
		}
	}
*/	
}
