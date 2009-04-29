package beaver.cc;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

import beaver.SyntaxErrorException;
import beaver.TestTools;
import beaver.cc.spec.AltDef;
import beaver.cc.spec.AltDefList;
import beaver.cc.spec.AltRegExprList;
import beaver.cc.spec.AstTreeWalker;
import beaver.cc.spec.CatRegExpr;
import beaver.cc.spec.CatRegExprList;
import beaver.cc.spec.Compiler;
import beaver.cc.spec.DiffRangeExpr;
import beaver.cc.spec.InlineRhsItem;
import beaver.cc.spec.KeywordRhsItem;
import beaver.cc.spec.Macro;
import beaver.cc.spec.MacroList;
import beaver.cc.spec.MacroRangeExpr;
import beaver.cc.spec.MultCharExprQuantifier;
import beaver.cc.spec.NestedCharExpr;
import beaver.cc.spec.NodeVisitor;
import beaver.cc.spec.OperCharExprQuantifier;
import beaver.cc.spec.PrecSymbolList;
import beaver.cc.spec.Precedence;
import beaver.cc.spec.PrecedenceList;
import beaver.cc.spec.RangeCharExpr;
import beaver.cc.spec.RhsItemList;
import beaver.cc.spec.RhsSymbol;
import beaver.cc.spec.Rule;
import beaver.cc.spec.RuleList;
import beaver.cc.spec.RuleNamePrecSymbol;
import beaver.cc.spec.SimpleRangeExpr;
import beaver.cc.spec.Spec;
import beaver.cc.spec.SymbolRhsItem;
import beaver.cc.spec.Term;
import beaver.cc.spec.TermTextPrecSymbol;
import beaver.cc.spec.TextCharExpr;
import beaver.cc.spec.Token;
import beaver.cc.spec.TokenList;

public class BeaverParserTest
{
	public static NodeVisitor specAstPrinter = new AstTreeWalker()
	{		
		StringWriter txt = new StringWriter(4096);
		PrintWriter out = new PrintWriter(txt);
		int indent = -1;
		
		private void print(String msg)
		{
			for (int i = 0; i < indent; i++)
			{
				out.print('\t');
			}
			out.println(msg);
		}
		
		public void enter(AltDef altDef)
        {
			print("altDef");
        }

		public void enter(AltDefList altDefList)
        {
	        print("altDefList");
        }

		public void enter(AltRegExprList altRegExprList)
        {
	        print("altRegExprList");
        }

		public void enter(CatRegExpr catRegExpr)
        {
	        print("catRegExpr");
        }

		public void enter(CatRegExprList catRegExprList)
        {
	        print("catRegExprList");
        }

		public void enter(DiffRangeExpr diffRangeExpr)
        {
	        print("diffRangeExpr");
        }

		public void enter(InlineRhsItem inlineRhsItem)
        {
	        print("inlineRhsItem");
        }

		public void enter(KeywordRhsItem keywordRhsItem)
        {
	        print("keywordRhsItem");
        }

		public void enter(Macro macro)
        {
	        print("macro");
        }

		public void enter(MacroList macroList)
        {
	        print("macroList");
        }

		public void enter(MacroRangeExpr macroRangeExpr)
        {
	        print("macroRangeExpr");
        }

		public void enter(MultCharExprQuantifier multCharExprQuantifier)
        {
	        print("multCharExprQuantifier");
        }

		public void enter(NestedCharExpr nestedCharExpr)
        {
	        print("nestedCharExpr");
        }

		public void enter(OperCharExprQuantifier operCharExprQuantifier)
        {
	        print("operCharExprQuantifier");
        }

		public void enter(Precedence precedence)
        {
	        print("precedence");
        }

		public void enter(PrecedenceList precedenceList)
        {
	        print("precedenceList");
        }

		public void enter(PrecSymbolList precSymbolList)
        {
	        print("precSymbolList");
        }

		public void enter(RangeCharExpr rangeCharExpr)
        {
	        print("rangeCharExpr");
        }

		public void enter(RhsItemList rhsItemList)
        {
	        print("rhsItemList");
        }

		public void enter(RhsSymbol rhsSymbol)
        {
	        print("rhsSymbol");
        }

		public void enter(Rule rule)
        {
	        print("rule");
        }

		public void enter(RuleList ruleList)
        {
	        print("ruleList");
        }

		public void enter(RuleNamePrecSymbol ruleNamePrecSymbol)
        {
	        print("ruleNamePrecSymbol");
        }

		public void enter(SimpleRangeExpr simpleRangeExpr)
        {
	        print("simpleRangeExpr");
        }

		public void enter(SymbolRhsItem symbolRhsItem)
        {
	        print("symbolRhsItem");
        }

		public void enter(TermTextPrecSymbol termTextPrecSymbol)
        {
	        print("termTextPrecSymbol");
        }

		public void enter(TextCharExpr textCharExpr)
        {
	        print("textCharExpr");
        }

		public void enter(Token token)
        {
	        print("token");
        }

		public void enter(TokenList tokenList)
        {
	        print("tokenList");
        }

		public void visit(Term term)
        {
	        ++indent;
			print("T(" + term.value.toString() + ")");
	        --indent;
        }

		public void visit(AltDef altDef)
        {
	        ++indent;
	        super.visit(altDef);
	        --indent;
        }

		public void visit(AltDefList altDefList)
        {
	        ++indent;
	        super.visit(altDefList);
	        --indent;
        }

		public void visit(AltRegExprList altRegExprList)
        {
	        ++indent;
	        super.visit(altRegExprList);
	        --indent;
        }

		public void visit(CatRegExpr catRegExpr)
        {
	        ++indent;
	        super.visit(catRegExpr);
	        --indent;
        }

		public void visit(CatRegExprList catRegExprList)
        {
	        ++indent;
	        super.visit(catRegExprList);
	        --indent;
        }

		public void visit(DiffRangeExpr diffRangeExpr)
        {
	        ++indent;
	        super.visit(diffRangeExpr);
	        --indent;
        }

		public void visit(InlineRhsItem inlineRhsItem)
        {
	        ++indent;
	        super.visit(inlineRhsItem);
	        --indent;
        }

		public void visit(KeywordRhsItem keywordRhsItem)
        {
	        ++indent;
	        super.visit(keywordRhsItem);
	        --indent;
        }

		public void visit(Macro macro)
        {
	        ++indent;
	        super.visit(macro);
	        --indent;
        }

		public void visit(MacroList macroList)
        {
	        ++indent;
	        super.visit(macroList);
	        --indent;
        }

		public void visit(MacroRangeExpr macroRangeExpr)
        {
	        ++indent;
	        super.visit(macroRangeExpr);
	        --indent;
        }

		public void visit(MultCharExprQuantifier multCharExprQuantifier)
        {
	        ++indent;
	        super.visit(multCharExprQuantifier);
	        --indent;
        }

		public void visit(NestedCharExpr nestedCharExpr)
        {
	        ++indent;
	        super.visit(nestedCharExpr);
	        --indent;
        }

		public void visit(OperCharExprQuantifier operCharExprQuantifier)
        {
	        ++indent;
	        super.visit(operCharExprQuantifier);
	        --indent;
        }

		public void visit(Precedence precedence)
        {
	        ++indent;
	        super.visit(precedence);
	        --indent;
        }

		public void visit(PrecedenceList precedenceList)
        {
	        ++indent;
	        super.visit(precedenceList);
	        --indent;
        }

		public void visit(PrecSymbolList precSymbolList)
        {
	        ++indent;
	        super.visit(precSymbolList);
	        --indent;
        }

		public void visit(RangeCharExpr rangeCharExpr)
        {
	        ++indent;
	        super.visit(rangeCharExpr);
	        --indent;
        }

		public void visit(RhsItemList rhsItemList)
        {
	        ++indent;
	        super.visit(rhsItemList);
	        --indent;
        }

		public void visit(RhsSymbol rhsSymbol)
        {
	        ++indent;
	        super.visit(rhsSymbol);
	        --indent;
        }

		public void visit(Rule rule)
        {
	        ++indent;
	        super.visit(rule);
	        --indent;
        }

		public void visit(RuleList ruleList)
        {
	        ++indent;
	        super.visit(ruleList);
	        --indent;
        }

		public void visit(RuleNamePrecSymbol ruleNamePrecSymbol)
        {
	        ++indent;
	        super.visit(ruleNamePrecSymbol);
	        --indent;
        }

		public void visit(SimpleRangeExpr simpleRangeExpr)
        {
	        ++indent;
	        super.visit(simpleRangeExpr);
	        --indent;
        }

		public void visit(SymbolRhsItem symbolRhsItem)
        {
	        ++indent;
	        super.visit(symbolRhsItem);
	        --indent;
        }

		public void visit(TermTextPrecSymbol termTextPrecSymbol)
        {
	        ++indent;
	        super.visit(termTextPrecSymbol);
	        --indent;
        }

		public void visit(TextCharExpr textCharExpr)
        {
	        ++indent;
	        super.visit(textCharExpr);
	        --indent;
        }

		public void visit(Token token)
        {
	        ++indent;
	        super.visit(token);
	        --indent;
        }

		public void visit(TokenList tokenList)
        {
	        ++indent;
	        super.visit(tokenList);
	        --indent;
        }

		public String toString()
		{
			String str = txt.toString();
			txt.getBuffer().setLength(0);
			return str;
		}
	};
	
	@Test
	public void testSpecAST() throws IOException, SyntaxErrorException
	{
		File specFile = new File("../comp/src/beaver/cc/Beaver.bps");
		assertTrue(specFile.exists());
		Spec spec = Compiler.parse(specFile);
		specAstPrinter.visit(spec);
		String printOut = specAstPrinter.toString();
		String expected = TestTools.readResource(this.getClass(), "BeaverParserTest_ExpectedSpecPrintout.txt"); 
		assertEquals(expected, printOut);
	}
	
}
