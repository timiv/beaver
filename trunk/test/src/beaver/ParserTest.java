package beaver;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import beaver.parser.ParserTestFixtures;

public class ParserTest
{
	private static String parserName  = "ExprParser";
	private static String packageName = "test3";
	private static File   testDir     = new File("tst/test3");

	@BeforeClass
	public static void compileTestParser() throws IOException
	{
		if (!testDir.exists())
		{
			testDir.mkdirs();
		}
		ParserTestFixtures.compileParser(ParserTestFixtures.getExprCalcGrammar(), parserName, packageName, testDir);
	}
	
	static class TestParser extends Parser
	{
		TestParser(File bptFile) throws IOException
		{
			super(new FileInputStream(bptFile));
		}
		
		protected Object makeTerm(int id, Object text, int line, int column)
		{
			return text;
		}
		
		protected Object reduce(Object[] stack, int top, int rule)
		{
			return null;
		}
		
		String[] getSymbolNames()
		{
			return super.symbols;
		}
	}

	private static String quote(String txt)
	{
		return '"' + txt + '"';
	}
	
	@Test
	public void testParserInit() throws IOException
	{
		File bptFile = new File(testDir, parserName + ".bpt"); 
		assertTrue(bptFile.exists());
		
		TestParser parser = new TestParser(bptFile);
		String[] symNames = parser.getSymbolNames();
		assertArrayEquals(new String[] {null, "EOF", quote("("), "NUM", "ID", quote("+"), quote("-"), quote("*"), quote("/"), quote(";"), quote(")"), "Expr", "Stmt", quote("="), "OptStmtList", "OptExpr", "StmtList", "Eval"}, symNames);	
	}
	
	@Test
	public void testParserActions() throws IOException
	{
		File bptFile = new File(testDir, parserName + ".bpt"); 
		assertTrue(bptFile.exists());
		
		TestParser parser = new TestParser(bptFile);
		parser.stackTop--;
		parser.stackStates[parser.stackTop] = 1 - 1;
		
        assertEquals( -3, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals(  0, parser.findAction( 9)); // ";" 
        assertEquals( -3, parser.findAction( 2)); // "(" 
        assertEquals(  0, parser.findAction(10)); // ")" 
        assertEquals(  0, parser.findAction( 5)); // "+" 
        assertEquals(  0, parser.findAction( 6)); // "-" 
        assertEquals(  0, parser.findAction( 7)); // "*" 
        assertEquals(  0, parser.findAction( 8)); // "/" 
        assertEquals( -3, parser.findAction( 3)); // NUM 
        assertEquals( 20, parser.findAction( 4)); // ID  
        assertEquals(-16, parser.findAction(17)); // Eval
        assertEquals(  2, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals( 18, parser.findAction(16)); // StmtList
        assertEquals(  0, parser.findAction(11)); // Expr
        assertEquals( 24, parser.findAction(12)); // Stmt

		parser.stackStates[parser.stackTop] = 2 - 1;
        
        assertEquals( -5, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals(  0, parser.findAction( 9)); // ";" 
        assertEquals( 15, parser.findAction( 2)); // "(" 
        assertEquals(  0, parser.findAction(10)); // ")" 
        assertEquals(  0, parser.findAction( 5)); // "+" 
        assertEquals(  0, parser.findAction( 6)); // "-" 
        assertEquals(  0, parser.findAction( 7)); // "*" 
        assertEquals(  0, parser.findAction( 8)); // "/" 
        assertEquals( 13, parser.findAction( 3)); // NUM 
        assertEquals( 14, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  3, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals(  4, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt

        parser.stackStates[parser.stackTop] = 3 - 1;
		
        assertEquals( -1, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals(  0, parser.findAction( 9)); // ";" 
        assertEquals(  0, parser.findAction( 2)); // "(" 
        assertEquals(  0, parser.findAction(10)); // ")" 
        assertEquals(  0, parser.findAction( 5)); // "+" 
        assertEquals(  0, parser.findAction( 6)); // "-" 
        assertEquals(  0, parser.findAction( 7)); // "*" 
        assertEquals(  0, parser.findAction( 8)); // "/" 
        assertEquals(  0, parser.findAction( 3)); // NUM 
        assertEquals(  0, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals(  0, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt

        parser.stackStates[parser.stackTop] = 4 - 1;
		
        assertEquals( -4, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals(  0, parser.findAction( 9)); // ";" 
        assertEquals(  0, parser.findAction( 2)); // "(" 
        assertEquals(  0, parser.findAction(10)); // ")" 
        assertEquals(  5, parser.findAction( 5)); // "+" 
        assertEquals(  7, parser.findAction( 6)); // "-" 
        assertEquals(  9, parser.findAction( 7)); // "*" 
        assertEquals( 11, parser.findAction( 8)); // "/" 
        assertEquals(  0, parser.findAction( 3)); // NUM 
        assertEquals(  0, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals(  0, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt

        parser.stackStates[parser.stackTop] = 5 - 1;
        
        assertEquals(  0, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals(  0, parser.findAction( 9)); // ";" 
        assertEquals( 15, parser.findAction( 2)); // "(" 
        assertEquals(  0, parser.findAction(10)); // ")" 
        assertEquals(  0, parser.findAction( 5)); // "+" 
        assertEquals(  0, parser.findAction( 6)); // "-" 
        assertEquals(  0, parser.findAction( 7)); // "*" 
        assertEquals(  0, parser.findAction( 8)); // "/" 
        assertEquals( 13, parser.findAction( 3)); // NUM 
        assertEquals( 14, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals(  6, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt
        
        parser.stackStates[parser.stackTop] = 6 - 1;
        
        assertEquals(-12, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals(-12, parser.findAction( 9)); // ";" 
        assertEquals(  0, parser.findAction( 2)); // "(" 
        assertEquals(-12, parser.findAction(10)); // ")" 
        assertEquals(-12, parser.findAction( 5)); // "+" 
        assertEquals(-12, parser.findAction( 6)); // "-" 
        assertEquals(  9, parser.findAction( 7)); // "*" 
        assertEquals( 11, parser.findAction( 8)); // "/" 
        assertEquals(  0, parser.findAction( 3)); // NUM 
        assertEquals(  0, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals(  0, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt

        parser.stackStates[parser.stackTop] = 7 - 1;
        
        assertEquals(  0, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals(  0, parser.findAction( 9)); // ";" 
        assertEquals( 15, parser.findAction( 2)); // "(" 
        assertEquals(  0, parser.findAction(10)); // ")" 
        assertEquals(  0, parser.findAction( 5)); // "+" 
        assertEquals(  0, parser.findAction( 6)); // "-" 
        assertEquals(  0, parser.findAction( 7)); // "*" 
        assertEquals(  0, parser.findAction( 8)); // "/" 
        assertEquals( 13, parser.findAction( 3)); // NUM 
        assertEquals( 14, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals(  8, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt

        parser.stackStates[parser.stackTop] = 8 - 1;
        
        assertEquals(-13, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals(-13, parser.findAction( 9)); // ";" 
        assertEquals(  0, parser.findAction( 2)); // "(" 
        assertEquals(-13, parser.findAction(10)); // ")" 
        assertEquals(-13, parser.findAction( 5)); // "+" 
        assertEquals(-13, parser.findAction( 6)); // "-" 
        assertEquals(  9, parser.findAction( 7)); // "*" 
        assertEquals( 11, parser.findAction( 8)); // "/" 
        assertEquals(  0, parser.findAction( 3)); // NUM 
        assertEquals(  0, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals(  0, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt

        parser.stackStates[parser.stackTop] = 9 - 1;
        
        assertEquals(  0, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals(  0, parser.findAction( 9)); // ";" 
        assertEquals( 15, parser.findAction( 2)); // "(" 
        assertEquals(  0, parser.findAction(10)); // ")" 
        assertEquals(  0, parser.findAction( 5)); // "+" 
        assertEquals(  0, parser.findAction( 6)); // "-" 
        assertEquals(  0, parser.findAction( 7)); // "*" 
        assertEquals(  0, parser.findAction( 8)); // "/" 
        assertEquals( 13, parser.findAction( 3)); // NUM 
        assertEquals( 14, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals( 10, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt

        parser.stackStates[parser.stackTop] = 10 - 1;
        
        assertEquals(-14, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals(-14, parser.findAction( 9)); // ";" 
        assertEquals(  0, parser.findAction( 2)); // "(" 
        assertEquals(-14, parser.findAction(10)); // ")" 
        assertEquals(-14, parser.findAction( 5)); // "+" 
        assertEquals(-14, parser.findAction( 6)); // "-" 
        assertEquals(-14, parser.findAction( 7)); // "*" 
        assertEquals(-14, parser.findAction( 8)); // "/" 
        assertEquals(  0, parser.findAction( 3)); // NUM 
        assertEquals(  0, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals(  0, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt

        parser.stackStates[parser.stackTop] = 11 - 1;
        
        assertEquals(  0, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals(  0, parser.findAction( 9)); // ";" 
        assertEquals( 15, parser.findAction( 2)); // "(" 
        assertEquals(  0, parser.findAction(10)); // ")" 
        assertEquals(  0, parser.findAction( 5)); // "+" 
        assertEquals(  0, parser.findAction( 6)); // "-" 
        assertEquals(  0, parser.findAction( 7)); // "*" 
        assertEquals(  0, parser.findAction( 8)); // "/" 
        assertEquals( 13, parser.findAction( 3)); // NUM 
        assertEquals( 14, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals( 12, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt

        parser.stackStates[parser.stackTop] = 12 - 1;
        
        assertEquals(-15, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals(-15, parser.findAction( 9)); // ";" 
        assertEquals(  0, parser.findAction( 2)); // "(" 
        assertEquals(-15, parser.findAction(10)); // ")" 
        assertEquals(-15, parser.findAction( 5)); // "+" 
        assertEquals(-15, parser.findAction( 6)); // "-" 
        assertEquals(-15, parser.findAction( 7)); // "*" 
        assertEquals(-15, parser.findAction( 8)); // "/" 
        assertEquals(  0, parser.findAction( 3)); // NUM 
        assertEquals(  0, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals(  0, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt

        parser.stackStates[parser.stackTop] = 13 - 1;
        
        assertEquals( -9, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals( -9, parser.findAction( 9)); // ";" 
        assertEquals(  0, parser.findAction( 2)); // "(" 
        assertEquals( -9, parser.findAction(10)); // ")" 
        assertEquals( -9, parser.findAction( 5)); // "+" 
        assertEquals( -9, parser.findAction( 6)); // "-" 
        assertEquals( -9, parser.findAction( 7)); // "*" 
        assertEquals( -9, parser.findAction( 8)); // "/" 
        assertEquals(  0, parser.findAction( 3)); // NUM 
        assertEquals(  0, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals(  0, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt

        parser.stackStates[parser.stackTop] = 14 - 1;
        
        assertEquals(-10, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals(-10, parser.findAction( 9)); // ";" 
        assertEquals(  0, parser.findAction( 2)); // "(" 
        assertEquals(-10, parser.findAction(10)); // ")" 
        assertEquals(-10, parser.findAction( 5)); // "+" 
        assertEquals(-10, parser.findAction( 6)); // "-" 
        assertEquals(-10, parser.findAction( 7)); // "*" 
        assertEquals(-10, parser.findAction( 8)); // "/" 
        assertEquals(  0, parser.findAction( 3)); // NUM 
        assertEquals(  0, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals(  0, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt

        parser.stackStates[parser.stackTop] = 15 - 1;
        
        assertEquals(  0, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals(  0, parser.findAction( 9)); // ";" 
        assertEquals( 15, parser.findAction( 2)); // "(" 
        assertEquals(  0, parser.findAction(10)); // ")" 
        assertEquals(  0, parser.findAction( 5)); // "+" 
        assertEquals(  0, parser.findAction( 6)); // "-" 
        assertEquals(  0, parser.findAction( 7)); // "*" 
        assertEquals(  0, parser.findAction( 8)); // "/" 
        assertEquals( 13, parser.findAction( 3)); // NUM 
        assertEquals( 14, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals( 16, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt

        parser.stackStates[parser.stackTop] = 16 - 1;
        
        assertEquals(  0, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals(  0, parser.findAction( 9)); // ";" 
        assertEquals(  0, parser.findAction( 2)); // "(" 
        assertEquals( 17, parser.findAction(10)); // ")" 
        assertEquals(  5, parser.findAction( 5)); // "+" 
        assertEquals(  7, parser.findAction( 6)); // "-" 
        assertEquals(  9, parser.findAction( 7)); // "*" 
        assertEquals( 11, parser.findAction( 8)); // "/" 
        assertEquals(  0, parser.findAction( 3)); // NUM 
        assertEquals(  0, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals(  0, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt

        parser.stackStates[parser.stackTop] = 17 - 1;
        
        assertEquals(-11, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals(-11, parser.findAction( 9)); // ";" 
        assertEquals(  0, parser.findAction( 2)); // "(" 
        assertEquals(-11, parser.findAction(10)); // ")" 
        assertEquals(-11, parser.findAction( 5)); // "+" 
        assertEquals(-11, parser.findAction( 6)); // "-" 
        assertEquals(-11, parser.findAction( 7)); // "*" 
        assertEquals(-11, parser.findAction( 8)); // "/" 
        assertEquals(  0, parser.findAction( 3)); // NUM 
        assertEquals(  0, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals(  0, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt

        parser.stackStates[parser.stackTop] = 18 - 1;
        
        assertEquals( -2, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals(  0, parser.findAction( 9)); // ";" 
        assertEquals( -2, parser.findAction( 2)); // "(" 
        assertEquals(  0, parser.findAction(10)); // ")" 
        assertEquals(  0, parser.findAction( 5)); // "+" 
        assertEquals(  0, parser.findAction( 6)); // "-" 
        assertEquals(  0, parser.findAction( 7)); // "*" 
        assertEquals(  0, parser.findAction( 8)); // "/" 
        assertEquals( -2, parser.findAction( 3)); // NUM 
        assertEquals( 20, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals(  0, parser.findAction(11)); // Expr
        assertEquals( 19, parser.findAction(12)); // Stmt

        parser.stackStates[parser.stackTop] = 19 - 1;
        
        assertEquals( -7, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals(  0, parser.findAction( 9)); // ";" 
        assertEquals( -7, parser.findAction( 2)); // "(" 
        assertEquals(  0, parser.findAction(10)); // ")" 
        assertEquals(  0, parser.findAction( 5)); // "+" 
        assertEquals(  0, parser.findAction( 6)); // "-" 
        assertEquals(  0, parser.findAction( 7)); // "*" 
        assertEquals(  0, parser.findAction( 8)); // "/" 
        assertEquals( -7, parser.findAction( 3)); // NUM 
        assertEquals( -7, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals(  0, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt

        parser.stackStates[parser.stackTop] = 20 - 1;
        
        assertEquals(  0, parser.findAction( 1)); // EOF 
        assertEquals( 21, parser.findAction(13)); // "=" 
        assertEquals(  0, parser.findAction( 9)); // ";" 
        assertEquals(  0, parser.findAction( 2)); // "(" 
        assertEquals(  0, parser.findAction(10)); // ")" 
        assertEquals(  0, parser.findAction( 5)); // "+" 
        assertEquals(  0, parser.findAction( 6)); // "-" 
        assertEquals(  0, parser.findAction( 7)); // "*" 
        assertEquals(  0, parser.findAction( 8)); // "/" 
        assertEquals(  0, parser.findAction( 3)); // NUM 
        assertEquals(  0, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals(  0, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt

        parser.stackStates[parser.stackTop] = 21 - 1;
        
        assertEquals(  0, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals(  0, parser.findAction( 9)); // ";" 
        assertEquals( 15, parser.findAction( 2)); // "(" 
        assertEquals(  0, parser.findAction(10)); // ")" 
        assertEquals(  0, parser.findAction( 5)); // "+" 
        assertEquals(  0, parser.findAction( 6)); // "-" 
        assertEquals(  0, parser.findAction( 7)); // "*" 
        assertEquals(  0, parser.findAction( 8)); // "/" 
        assertEquals( 13, parser.findAction( 3)); // NUM 
        assertEquals( 14, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals( 22, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt

        parser.stackStates[parser.stackTop] = 22 - 1;
        
        assertEquals(  0, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals( 23, parser.findAction( 9)); // ";" 
        assertEquals(  0, parser.findAction( 2)); // "(" 
        assertEquals(  0, parser.findAction(10)); // ")" 
        assertEquals(  5, parser.findAction( 5)); // "+" 
        assertEquals(  7, parser.findAction( 6)); // "-" 
        assertEquals(  9, parser.findAction( 7)); // "*" 
        assertEquals( 11, parser.findAction( 8)); // "/" 
        assertEquals(  0, parser.findAction( 3)); // NUM 
        assertEquals(  0, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals(  0, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt

        parser.stackStates[parser.stackTop] = 23 - 1;
        
        assertEquals( -8, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals(  0, parser.findAction( 9)); // ";" 
        assertEquals( -8, parser.findAction( 2)); // "(" 
        assertEquals(  0, parser.findAction(10)); // ")" 
        assertEquals(  0, parser.findAction( 5)); // "+" 
        assertEquals(  0, parser.findAction( 6)); // "-" 
        assertEquals(  0, parser.findAction( 7)); // "*" 
        assertEquals(  0, parser.findAction( 8)); // "/" 
        assertEquals( -8, parser.findAction( 3)); // NUM 
        assertEquals( -8, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals(  0, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt

        parser.stackStates[parser.stackTop] = 24 - 1;
        
        assertEquals( -6, parser.findAction( 1)); // EOF 
        assertEquals(  0, parser.findAction(13)); // "=" 
        assertEquals(  0, parser.findAction( 9)); // ";" 
        assertEquals( -6, parser.findAction( 2)); // "(" 
        assertEquals(  0, parser.findAction(10)); // ")" 
        assertEquals(  0, parser.findAction( 5)); // "+" 
        assertEquals(  0, parser.findAction( 6)); // "-" 
        assertEquals(  0, parser.findAction( 7)); // "*" 
        assertEquals(  0, parser.findAction( 8)); // "/" 
        assertEquals( -6, parser.findAction( 3)); // NUM 
        assertEquals( -6, parser.findAction( 4)); // ID  
        assertEquals(  0, parser.findAction(17)); // Eval
        assertEquals(  0, parser.findAction(14)); // OptStmtList
        assertEquals(  0, parser.findAction(15)); // OptExpr 
        assertEquals(  0, parser.findAction(16)); // StmtList
        assertEquals(  0, parser.findAction(11)); // Expr
        assertEquals(  0, parser.findAction(12)); // Stmt
	}

	@Test
	public void testParsing() throws SyntaxErrorException, IOException
	{
		Scanner input = new Scanner()
		{
			// Input: a = 7; b = a * 19; c = 42; (b * b - 4 * a * c)
			//        ----+----1----+----2----+----3----+----4----+----
			int[]    tokenIds  = new int[]    {   4, -13,   3,  -9,   4, -13,   4,  -7,    3,  -9,   4, -13,    3,  -9,  -2,  4,  -7,   4,  -6,   3,  -7,   4,  -7,   4,  -10 };
			String[] tokenText = new String[] { "a", "=", "7", ";", "b", "=", "a", "*", "19", ";", "c", "=", "42", ";", "(", "b", "*", "b", "-", "4", "*", "a", "*", "c", ")" };
			int[]    tokenCols = new int[]    {   1,   3,   5,   6,   8,  10,  12,  14,   16,  18,  20,  22,   24,  26,  28,  29,  31,  33,  35,  37,  39,  41,  43,  45,  46 };
			int      index = -1;
			
            public int getNextToken() throws UnexpectedCharacterException, IOException
            {
            	if (++index >= tokenIds.length)
            	{
            		return -1; // EOF
            	}
	            return tokenIds[index];
            }

            public int getTokenColumn()
            {
	            return tokenCols[index];
            }

            public int getTokenLine()
            {
	            return 1;
            }

            public Object getTokenText()
            {
	            return tokenText[index];
            }
		};
		Parser parser = new test3.ExprParser();
		Object goal = parser.parse(input);
		assertTrue(goal instanceof test3.Eval);
		test3.Eval eval = (test3.Eval) goal;
		
		test3.AstTreeWalker treeWalker = new test3.AstTreeWalker()
		{
			StringBuilder txt = new StringBuilder(200);
			
            public void enter(test3.AddExpr addExpr)
            {
            	txt.append("+ ");
            }

			public void enter(test3.Eval eval)
            {
				txt.append("eval ");
            }

            public void enter(test3.MulExpr mulExpr)
            {
            	txt.append("* ");
            }

            public void enter(test3.NumExpr numExpr)
            {
            	txt.append("num ");
            }

            public void enter(test3.Stmt stmt)
            {
            	txt.append("def ");
            }
            
            public void leave(test3.Stmt stmt)
            {
            	txt.append("; ");
            }

            public void enter(test3.StmtList stmtList)
            {
            	txt.append("[ ");
            }

            public void leave(test3.StmtList stmtList)
            {
            	txt.append("] ");
            }
            
            public void enter(test3.SubExpr subExpr)
            {
            	txt.append("- ");
            }

            public void enter(test3.VarExpr varExpr)
            {
            	txt.append("var ");
            }
			
            public void visit(test3.Term node)
            {
            	txt.append(node.value).append(' ');
            }
            
            public String toString()
            {
            	return txt.toString();
            }
		};
		treeWalker.visit(eval);
		assertEquals("eval [ def a num 7 ; def b * var a num 19 ; def c num 42 ; ] - * var b var b * * num 4 var a var c ", treeWalker.toString());
	}
}

