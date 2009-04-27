package beaver.cc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import beaver.lexer.Accept;
import beaver.lexer.CharRange;
import beaver.lexer.CharScannerGenerator;
import beaver.lexer.RegExp;
import beaver.parser.Grammar;
import beaver.parser.GrammarFactory;
import beaver.parser.ParserCompiler;
import beaver.parser.Terminal;

public class BootstrapCompiler
{
	private static String packageName = "beaver.cc.spec";
	private static String scannerName = "BeaverScanner";
	private static String scannerBase = "beaver.CharScanner";
	private static String parserName  = "BeaverParser";
	private static File   srcDir      = new File("src/beaver/cc/spec");
	private static File   binDir      = new File("var/beaver/cc/spec");
	
	public static void main(String[] args)
	{
		Grammar grammar = getGrammar();
		Log log = new Log()
		{
		    public void warning(String text)
		    {
			    System.out.print("Warning: ");
			    System.out.println(text);
		    }
		
		    public void error(String text)
		    {
			    System.err.print("Error: ");
			    System.err.println(text);
		    }
		};
		if (!srcDir.exists())
		{
			srcDir.mkdirs();
		}
		if (!binDir.exists())
		{
			binDir.mkdirs();
		}
		ParserCompiler comp = new ParserCompiler(log, parserName, packageName, srcDir);
		
		comp.setPreferShiftOverReduce(true);
		comp.setGenerateAstStubs(true);
		comp.setInlineParserActions(true);
//		comp.setDumpParserStates(true);
		if (comp.compile(grammar))
		{
			RegExp re = compileKeywords(grammar.getKeywords());
			re = new RegExp.Alt(re, compileTokens(grammar.getTokens()));
			
			byte[] bc = new CharScannerGenerator(re, grammar.getEOF().getId()).compile(packageName.replace('.', '/') + "/" + scannerName, scannerBase.replace('.', '/'));
			writeClassFile(log, bc, new File(binDir, scannerName + ".class"));
		}
	}

	private static void writeClassFile(Log log, byte[] bc, File bcFile)
    {
	    try
	    {
	    	OutputStream os = new FileOutputStream(bcFile);
	    	try
	    	{
	    		os.write(bc);
	    	}
	    	finally
	    	{
	    		os.close();
	    	}
	    }
	    catch (IOException e)
	    {
	    	log.error("Failed writing scanner class file: " + e.getMessage());
	    }
    }

	private static Grammar getGrammar()
	{
		GrammarFactory fact = new GrammarFactory(new String[] {"QUANT", "ASSOC", "NUM", "ID", "TEXT", "RANGE"});
		/*
        	Spec
        		=  "%rules" Rule+  ("%precedences"  Precedence+)?  ("%macros"  Macro+)?  "%tokens" Token+
        		;
		 */
		fact.def("Spec").txt("%rules").sym("RuleList").sym("OptPrecedenceList").sym("OptMacroList").txt("%tokens").sym("TokenList").end();
		fact.def("OptPrecedenceList").txt("%precedences").sym("PrecedenceList").end();
		fact.def("OptPrecedenceList").end();
		fact.def("OptMacroList").txt("%macros").sym("MacroList").end();
		fact.def("OptMacroList").end();
		fact.def("RuleList").sym("Rule").end();
		fact.def("RuleList").sym("RuleList").sym("Rule").end();
		fact.def("PrecedenceList").sym("Precedence").end();
		fact.def("PrecedenceList").sym("PrecedenceList").sym("Precedence").end();
		fact.def("MacroList").sym("Macro").end();
		fact.def("MacroList").sym("MacroList").sym("Macro").end();
		fact.def("TokenList").sym("Token").end();
		fact.def("TokenList").sym("TokenList").sym("Token").end();
		/*
        	Rule
        		=  ID  "="  (AltDef  "|")+  ";"
        		;
		 */
		fact.def("Rule").sym("ID").txt("=").sym("AltDefList").txt(";").end();
		fact.def("AltDefList").sym("AltDef").end();
		fact.def("AltDefList").sym("AltDefList").txt("|").sym("AltDef").end();
		/*
        	AltDef
        		=  ruleName:("{"  ID  "}")?  RhsItem*
        		;
		 */
		fact.def("AltDef").sym("OptRuleName").sym("OptRhsItemList").end();
		fact.def("OptRuleName").txt("{").sym("ID").txt("}").end();
		fact.def("OptRuleName").end();	
		fact.def("OptRhsItemList").sym("RhsItemList").end();
		fact.def("OptRhsItemList").end();
		fact.def("RhsItemList").sym("RhsItem").end();
		fact.def("RhsItemList").sym("RhsItemList").sym("RhsItem").end();
		/*
        	RhsItem
        		=  { Keyword }  TEXT
        		|  { Symbol  }               RhsSymbol  
        		|  { Symbol  }  ref:ID  ":"  RhsSymbol  
        		|  { Inline  }               "("  prefix:TEXT?  RhsSymbol  suffix:TEXT?  ")"  QUANT
        		|  { Inline  }  ref:ID  ":"  "("  prefix:TEXT?  RhsSymbol  suffix:TEXT?  ")"  QUANT
        		;
		 */
		fact.def("RhsItem", "Keyword").sym("TEXT").end();
		fact.def("RhsItem", "Symbol").sym("ID","ref").txt(":").sym("RhsSymbol").end();
		fact.def("RhsItem", "Symbol")                         .sym("RhsSymbol").end();
		fact.def("RhsItem", "Inline").sym("ID","ref").txt(":").txt("(").sym("OptText","prefix").sym("RhsSymbol").sym("OptText","suffix").txt(")").sym("QUANT").end();
		fact.def("RhsItem", "Inline")                         .txt("(").sym("OptText","prefix").sym("RhsSymbol").sym("OptText","suffix").txt(")").sym("QUANT").end();
		fact.def("OptText").sym("TEXT").end();
		fact.def("OptText").end();
		/*
        	RhsSymbol
        		=  ID  QUANT?
        		; 
		 */
		fact.def("RhsSymbol").sym("ID").sym("OptQuant").end();
		fact.def("OptQuant").sym("QUANT").end();
		fact.def("OptQuant").end();
		/*
        	Precedence
        		=  (PrecSymbol  ",")+  ":"  ASSOC  ";"
        		;
		 */
		fact.def("Precedence").sym("PrecSymbolList").txt(":").sym("ASSOC").txt(";").end();		
		fact.def("PrecSymbolList").sym("PrecSymbol").end();
		fact.def("PrecSymbolList").sym("PrecSymbolList").txt(",").sym("PrecSymbol").end();
		/*
        	PrecSymbol
        		=  { TermText } TEXT
        		|  { RuleName } ID
        		;
		 */
		fact.def("PrecSymbol", "TermText").sym("TEXT").end();
		fact.def("PrecSymbol", "RuleName").sym("ID").end();
		/*
        	Macro
        		=  ID  "="  altRegExprList:(CatRegExpr+  "|")+  ";"
        		;
		 */
		fact.def("Macro").sym("ID").txt("=").sym("AltRegExprList").txt(";").end();
		fact.def("AltRegExprList").sym("CatRegExprList").end();
		fact.def("AltRegExprList").sym("AltRegExprList").txt("|").sym("CatRegExprList").end();
		fact.def("CatRegExprList").sym("CatRegExpr").end();
		fact.def("CatRegExprList").sym("CatRegExprList").sym("CatRegExpr").end();
		/*
        	CatRegExpr
        		=  CharExpr CharExprQuantifier?
        		;
		 */
		fact.def("CatRegExpr").sym("CharExpr").sym("OptCharExprQuantifier").end();
		fact.def("OptCharExprQuantifier").sym("CharExprQuantifier").end();
		fact.def("OptCharExprQuantifier").end();
		/*
        	CharExpr
        		=  { Text   }  TEXT
        		|  { Range  }  RangeExpr
        		|  { Nested }  "("  (CatRegExpr+  "|")+  ")"
        		;
		 */
		fact.def("CharExpr", "Text").sym("TEXT").end();
		fact.def("CharExpr", "Range").sym("RangeExpr").end();
		fact.def("CharExpr", "Nested").txt("(").sym("AltRegExprList").txt(")").end();
		/*
        	RangeExpr
        		=  { Simple }  RANGE
        		|  { Macro  }  ID
        		|  { Diff   }  min:RangeExpr  "\\"  sub:RangeExpr
        		;
		 */
		fact.def("RangeExpr", "Simple").sym("RANGE").end();
		fact.def("RangeExpr", "Macro").sym("ID").end();
		fact.def("RangeExpr", "Diff").sym("RangeExpr", "min").txt("\\").sym("RangeExpr","sub").end();
		/*
        	CharExprQuantifier
        		=  { Oper }  QUANT
        		|  { Mult }  "{"  min:NUM  "}"
        		|  { Mult }  "{"  min:NUM  ","  max:NUM?  "}"
        		;
		 */
		fact.def("CharExprQuantifier", "Oper").sym("QUANT").end();
		fact.def("CharExprQuantifier", "Mult").txt("{").sym("NUM","min").txt("}").end();
		fact.def("CharExprQuantifier", "Mult").txt("{").sym("NUM","min").txt(",").sym("OptNum","max").txt("}").end();
		fact.def("OptNum").sym("NUM").end();
		fact.def("OptNum").end();
		/*
        	Token
        		=  ID  "="  (CatRegExpr+  "|")+  context:("/" CatRegExpr)?  event:("->" ID)?  ";"
        		;
		 */
		fact.def("Token").sym("ID").txt("=").sym("AltRegExprList").sym("OptContext").sym("OptEvent").txt(";").end();
		fact.def("OptContext").txt("/").sym("CatRegExpr").end();
		fact.def("OptContext").end();
		fact.def("OptEvent").txt("->").sym("ID").end();
		fact.def("OptEvent").end();

		return fact.getGrammar();
	}
	
	private static RegExp compileKeywords(Terminal[] keywords)
	{
		if (keywords.length == 0)
		{
			return new RegExp.Null();
		}
		else
		{
			RegExp re = new RegExp.Rule(RegExp.matchText(keywords[0].getText()), new RegExp.Null(), new Accept(-keywords[0].getId(), Short.MAX_VALUE));
			for (int i = 1; i < keywords.length; i++)
	        {
	    		re = new RegExp.Alt(re, new RegExp.Rule(RegExp.matchText(keywords[i].getText()), new RegExp.Null(), new Accept(-keywords[i].getId(), Short.MAX_VALUE)));
	        }
			return re;
		}
	}
	
	private static RegExp compileTokens(Terminal[] tokens)
	{
		// %macros
		
		RegExp any    = new RegExp.MatchRange(new CharRange("^"));
		RegExp eol    = new RegExp.MatchRange(new CharRange("\\r\\n"));
		RegExp dot    = RegExp.diff(any, eol);
		RegExp esc    = RegExp.diff(dot, new CharRange("\\"));
		RegExp digit  = new RegExp.MatchRange(new CharRange("0-9"));
		RegExp letter = new RegExp.MatchRange(new CharRange("A-Za-z"));
		
		int prec = Short.MAX_VALUE;
		
		// %tokens
		
		// WS = [ \t]
		RegExp re = new RegExp.Rule
		( new RegExp.MatchRange(new CharRange(" \\t"))
		, new RegExp.Null()
		, new Accept(0, --prec)
		);
		
		// NL = "\r" | "\n" | "\r\n"  -> newLine
		re = new RegExp.Alt
		( re
		, new RegExp.Rule
		  ( new RegExp.Alt
			( RegExp.matchText("\r")
			, new RegExp.Alt
			  ( RegExp.matchText("\n")
			  ,	RegExp.matchText("\r\n")
			  )
			)
		  , new RegExp.Null()
		  , new Accept(0, --prec, "newLine")
		  )
		);
		
		// QUANT	= "?" | "*" | "+"
		re = new RegExp.Alt
		( re
		, new RegExp.Rule
		  ( new RegExp.Alt
			( RegExp.matchText("?")
			, new RegExp.Alt
			  ( RegExp.matchText("*")
			  ,	RegExp.matchText("+")
			  )
			)
		  , new RegExp.Null()
		  , new Accept(getTokenId(tokens, "QUANT"), --prec)
		  )
		);
		
		// ASSOC	= "left" | "right" | "none"
		re = new RegExp.Alt
		( re
		, new RegExp.Rule
		  ( new RegExp.Alt
			( RegExp.matchText("left")
			, new RegExp.Alt
			  ( RegExp.matchText("right")
			  ,	RegExp.matchText("none")
			  )
			)
		  , new RegExp.Null()
		  , new Accept(getTokenId(tokens, "ASSOC"), --prec)
		  )
		);
		
		// NUM		= digit+
		re = new RegExp.Alt
		( re
		, new RegExp.Rule
		  ( new RegExp.Close
		    ( digit
		    , '+'
		    )
		  , new RegExp.Null()
		  , new Accept(getTokenId(tokens, "NUM"), --prec)
		  )
		);
		
		// ID      = letter ( letter | digit | [_] )*
		re = new RegExp.Alt
		( re
		, new RegExp.Rule
		  ( new RegExp.Cat
			( letter
			, new RegExp.Close
		      ( new RegExp.Alt
		        ( letter
		        , new RegExp.Alt
		          ( digit
				  , new RegExp.MatchChar('_')
				  )
		        )
		      , '+'
		      )
		    )
		  , new RegExp.Null()
		  , new Accept(getTokenId(tokens, "ID"), --prec)
		  )
		);
		
		// TEXT	= "\"" ( esc \ ["] | "\\" dot )* "\""
		re = new RegExp.Alt
		( re
		, new RegExp.Rule
		  ( new RegExp.Cat
			( RegExp.matchText("\"")
			, new RegExp.Cat
			  (	new RegExp.Close
				( new RegExp.Alt
				  ( RegExp.diff( esc, new CharRange("\""))
				  , new RegExp.Cat
				    ( RegExp.matchText("\\")
				    , dot
				    )
				  )
				, '*'
				)
		      , RegExp.matchText("\"")
		      ) 
		    )
		  , new RegExp.Null()
		  , new Accept(getTokenId(tokens, "TEXT"), --prec)
		  )
		);

		// RANGE	= "[" "^"? ( esc \ [\]] | "\\" dot )* "]"
		re = new RegExp.Alt
		( re
		, new RegExp.Rule
		  ( new RegExp.Cat
			( RegExp.matchText("[")
			, new RegExp.Cat
			  ( new RegExp.Cat
			    ( new RegExp.Close
			      ( RegExp.matchText("^")
			      , '?'
			      )
			    , new RegExp.Close
				  ( new RegExp.Alt
				    ( RegExp.diff( esc, new CharRange("]"))
				    , new RegExp.Cat
				      ( RegExp.matchText("\\")
				      , dot
				      )
				    )
				  , '+'
				  )
			    )
		      , RegExp.matchText("]")
		      ) 
		    )
		  , new RegExp.Null()
		  , new Accept(getTokenId(tokens, "RANGE"), --prec)
		  )
		);
		return re;
	}
	
	private static int getTokenId(Terminal[] tokens, String name)
	{
		for (int i = 0; i < tokens.length; i++)
        {
	        if (tokens[i].toString().equals(name))
	        {
	        	return tokens[i].getId();
	        }
        }
		throw new IllegalStateException("cannot find token " + name);
	}
}
