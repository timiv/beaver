package beaver.parser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

import beaver.cc.Log;

public class ParserCompilerTest
{
	private static Grammar getTestGrammar()
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
			.sym("Expr", "left")
			.txt("+")
			.sym("Expr", "right")
			.end();
		fact.def("Expr", "Sub")
    		.sym("Expr", "left")
    		.txt("-")
    		.sym("Expr", "right")
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
		return fact.getGrammar();
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

	private static byte[] getExpectedData() throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
		DataOutputStream data = new DataOutputStream(out);
		data.writeBytes("BPT>");
		data.writeChar(21); // number of states
		// packed state lookaheads ranges
		data.writeInt((15 << 16) | 8);
		data.writeInt(( 1 << 16) | 1);
		data.writeInt(( 5 << 16) | 1);
		data.writeInt((11 << 16) | 8);
		data.writeInt(( 7 << 16) | 1);
		data.writeInt((11 << 16) | 8);
		data.writeInt(( 7 << 16) | 1);
		data.writeInt((11 << 16) | 8);
		data.writeInt(( 7 << 16) | 1);
		data.writeInt((11 << 16) | 8);
		data.writeInt(( 7 << 16) | 1);
		data.writeInt(( 7 << 16) | 1);
		data.writeInt(( 7 << 16) | 1);
		data.writeInt((11 << 16) | 8);
		data.writeInt(( 7 << 16) | 2);
		data.writeInt(( 7 << 16) | 1);
		data.writeInt(( 1 << 16) | 1);
		data.writeInt((12 << 16) | 1);
		data.writeInt((11 << 16) | 8);
		data.writeInt(( 6 << 16) | 2);
		data.writeInt(( 1 << 16) | 1);
		// minOffset
		data.writeShort(-1);
		
		writeUnsignedData(new int[] {42, 53, 62, 68, 0, 72, 7, 76, 14, 80, 21, 28, 35, 84, 68, 42, 73, 57, 88, 62, 99}, data);
		// packed actions size
		data.writeChar(100);
		writeSignedData(new int[] {-8, -8, -8, 8, 10, -8, -8, -9, -9, -9, 8, 10, -9, -9, -10, -10, -10, -10, -10, -10, -10, -11, -11, -11, -11, -11, -11, -11, -5, -5, -5, -5, -5, -5, -5, -6, -6, -6, -6, -6, -6, -6, -7, -7, -7, -7, -7, -7, -7, 14, 12, 18, 3, -1, 2, 17, -12, -6, -6, -6, -6, -6, -2, 4, 6, 8, 10, 21, 19, 4, 6, 8, 10, -3, 16, 14, 12, 13, 5, 14, 12, 13, 7, 14, 12, 13, 9, 14, 12, 13, 11, 14, 12, 13, 15, 14, 12, 13, 20, -4}, data);
		writeUnsignedData(new int[] {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 1, 13, 14, 15, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 6, 12, 2, 3, 4, 5, 1, 7, 8, 9, 10, 11, 8, 9, 10, 11, 8, 9, 10, 11, 8, 9, 10, 11, 8, 9, 10, 11, 8, 9, 10, 11, 1}, data);
		// default reduce actions (none here)
		writeUnsignedData(new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, data);
		
		data.writeChar(11); // number of productions
		data.writeInt((15 << 16) | 1); //  0: Goal = Eval
		data.writeInt((13 << 16) | 1); //  1: Eval = { Expr } Expr
		data.writeInt((13 << 16) | 1); //  2: Eval = { Stmt } Stmt
		data.writeInt((14 << 16) | 4); //  3: Stmt = ID = Expr ;
		data.writeInt((11 << 16) | 1); //  4: Expr = { Number } NUM
		data.writeInt((11 << 16) | 1); //  5: Expr = { Var } ID
		data.writeInt((11 << 16) | 3); //  6: Expr = { Nested } ( Expr ) 
		data.writeInt((11 << 16) | 3); //  7: Expr = { Add } Expr + Expr
		data.writeInt((11 << 16) | 3); //  8: Expr = { Sub } Expr - Expr
		data.writeInt((11 << 16) | 3); //  9: Expr = { Mul } Expr * Expr
		data.writeInt((11 << 16) | 3); // 10: Expr = { Div } Expr / Expr
		
		data.writeChar(15); // number of symbols
		
        data.writeUTF("EOF");   
        data.writeUTF("\"+\""); 
        data.writeUTF("\"-\""); 
        data.writeUTF("\"*\"");
        data.writeUTF("\"/\"");
        data.writeUTF("\";\"");
        data.writeUTF("\")\"");
        data.writeUTF("\"(\"");
        data.writeUTF("NUM");
        data.writeUTF("ID");
        data.writeUTF("Expr");
        data.writeUTF("\"=\"");
        data.writeUTF("Eval");
        data.writeUTF("Stmt");
        data.writeUTF("Goal");
        data.writeByte(4);
		
		data.flush();
		return out.toByteArray();
	}
	
	@Test
	public void testWritingSerializedParsingTablesFile() throws IOException
	{
		File outDir = new File("/temp");
		ParserCompiler comp = new ParserCompiler(new Log()
		{
			@Override
		    public void warning(String text)
		    {
			    System.out.print("Warning: ");
			    System.out.println(text);
		    }
		
			@Override
		    public void error(String text)
		    {
			    System.err.print("Error: ");
			    System.err.println(text);
		    }
		}, "ExprCalc", "test1", outDir);
		
		comp.compile(getTestGrammar());
		
		File bptFile = new File(outDir, "ExprCalc.bpt"); 
		assertTrue(bptFile.exists());
		byte[] data = new byte[(int) bptFile.length()];	
		FileInputStream in = new FileInputStream(bptFile);
		in.read(data);
		in.close();
		bptFile.delete();
		
		assertArrayEquals(getExpectedData(), data);
	}
	
	private static ParserCompiler getCompiler(String parserName, File outDir)
	{
		return new ParserCompiler(new Log()
		{
			@Override
		    public void warning(String text)
		    {
			    System.out.print("Warning: ");
			    System.out.println(text);
		    }
		
			@Override
		    public void error(String text)
		    {
			    System.err.print("Error: ");
			    System.err.println(text);
		    }
		}, parserName, "test2", outDir);
	}
	
	private void testWritingParserSource(ParserCompiler comp, Grammar grammar, String fileNameOfExpectedOutput) throws IOException
	{
		comp.compile(grammar);
		
		File srcFile = new File(comp.outputDir, comp.parserName + ".java");
		assertTrue(srcFile.exists());
		char[] buf = new char[(int) srcFile.length()];
		FileReader in = new FileReader(srcFile);
		in.read(buf);
		in.close();
//		srcFile.delete();
		String actualSource = new String(buf);
		
		InputStreamReader is = new InputStreamReader(this.getClass().getResourceAsStream(fileNameOfExpectedOutput));
		StringBuilder txt = new StringBuilder(buf.length);
		for (int cnt = is.read(buf); cnt > 0; cnt = is.read(buf))
		{
			txt.append(buf, 0, cnt);
		}
		String expectedSource = txt.toString();
		
		assertEquals(expectedSource, actualSource);
	}
	
	@Test
	public void testWritingParserSource() throws IOException
	{
		ParserCompiler comp = getCompiler("ExprCalc", new File("/temp"));
		comp.setPreferShiftOverReduce(true);
		testWritingParserSource(comp, getTestGrammar(), "ParserCompilerTest_ExpectedParserSource.txt");
	}
	
	@Test
	public void testWritingMoreRealisticExprCalc() throws IOException
	{
		ParserCompiler comp = getCompiler("ExprCalc2", new File("/temp"));
		comp.setPreferShiftOverReduce(true);
		testWritingParserSource(comp, ParserTestFixtures.getExprCalcGrammar(), "ParserCompilerTest_ExprCalc2ParserSource.txt");
	}
	
	@Test
	public void testWritingExprCalc() throws IOException
	{
		ParserCompiler comp = getCompiler("ExprCalc3", new File("/temp"));
		comp.setPreferShiftOverReduce(true);
		comp.setDoNotWritePassThroughActions(true);
		testWritingParserSource(comp, ParserTestFixtures.getExprCalcGrammar(), "ParserCompilerTest_ExprCalc3ParserSource.txt");
	}
	
	@Test
	public void testWritingExprCalcWithAst() throws IOException
	{
		ParserCompiler comp = getCompiler("ExprCalc4", new File("/temp"));
		comp.setPreferShiftOverReduce(true);
		comp.setGenerateAstStubs(new String[] { "TextTerm" });
		testWritingParserSource(comp, ParserTestFixtures.getExprCalcGrammar(), "ParserCompilerTest_ExprCalc4ParserSource.txt");
	}
}
