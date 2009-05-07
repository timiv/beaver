package beaver.cc;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import beaver.SyntaxErrorException;
import beaver.TestTools;
import beaver.cc.spec.Compiler;
import beaver.cc.spec.NodeVisitor;
import beaver.cc.spec.Spec;

public class CompilerTest
{
	private static NodeVisitor specAstPrinter = BeaverParserTest.specAstPrinter; 
	
	@Test
	public void testSpecASTAfterSymbolDequantification() throws IOException, SyntaxErrorException
	{
		File specFile = new File("../comp/src/beaver/cc/Beaver.bps");
		assertTrue(specFile.exists());
		Spec spec = Compiler.parse(specFile);
		assertNotNull(spec);
		Compiler.collateProductions(spec.ruleList);
		Compiler.expandQuantifiedSymbols(spec.ruleList);	
		specAstPrinter.visit(spec);
		String printOut = specAstPrinter.toString();
		String expected = TestTools.readResource(this.getClass(), "CompilerTest_ExpectedSpecPrintout.txt"); 
		assertEquals(expected, printOut);
	}
	
	@Test
	public void testSpecASTAfterFullDequantification() throws IOException, SyntaxErrorException
	{
		File specFile = new File("../comp/src/beaver/cc/Beaver.bps");
		assertTrue(specFile.exists());
		Spec spec = Compiler.parse(specFile);
		assertNotNull(spec);
		Compiler.collateProductions(spec.ruleList);
		Compiler.expandQuantifiedSymbols(spec.ruleList);	
		if (Compiler.checkInlineRulesCorrectness(spec.ruleList, TestTools.consoleLog))
		{
			Compiler.extractInlinedSymbols(spec.ruleList);
		}
		specAstPrinter.visit(spec);
		String printOut = specAstPrinter.toString();
		String expected = TestTools.readResource(this.getClass(), "CompilerTest_ExpectedSpecPrintout2.txt"); 
		assertEquals(expected, printOut);
	}
}
