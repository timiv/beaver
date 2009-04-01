package beaver.parser;

import java.io.File;
import java.io.IOException;

import beaver.cc.Log;

public class ParserTestFixtures
{
	public static Grammar getExprCalcGrammar()
	{
		GrammarFactory fact = new GrammarFactory(new String[] {"NUM", "ID"});
		fact.def("Eval")
     		.sym("OptStmtList")
			.sym("OptExpr")
    		.end();
		fact.def("OptStmtList")
     		.sym("StmtList")
    		.end();
		fact.def("OptStmtList")
			.end();
		fact.def("OptExpr")
		    .sym("Expr")
		    .end();
		fact.def("OptExpr")
			.end();
		fact.def("StmtList", "New")
     		.sym("Stmt")
    		.end();
		fact.def("StmtList", "Ext")
     		.sym("StmtList")
     		.sym("Stmt")
    		.end();
		fact.def("Stmt")
			.sym("ID")
			.txt("=")
			.sym("Expr")
			.txt(";")
			.end();
		fact.def("Expr", "Num")
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
    		.sym("Expr", "left")
    		.txt("*")
    		.sym("Expr", "right")
    		.end();
		fact.def("Expr", "Div")
    		.sym("Expr", "left")
    		.txt("/")
    		.sym("Expr", "right")
    		.end();
		fact.left()
		    .prec("*")
		    .prec("/");
		fact.left()
		    .prec("+")
		    .prec("-");
		return fact.getGrammar();
/*
       1: EOF
      13: "="
       9: ";"
       2: "("
      10: ")"
       5: "+"
       6: "-"
       7: "*"
       8: "/"
       3: NUM
       4: ID
      17: Eval
      14: OptStmtList
      15: OptExpr
      16: StmtList
      11: Expr
      12: Stmt
    
      -1: Eval = OptStmtList OptExpr
      -2: OptStmtList = StmtList
      -3: OptStmtList =
      -4: OptExpr = Expr
      -5: OptExpr =
      -6: StmtList = { New } Stmt
      -7: StmtList = { Ext } StmtList Stmt
      -8: Stmt = ID "=" Expr ";"
      -9: Expr = { Num } NUM
     -10: Expr = { Var } ID
     -11: Expr = { Nested } "(" Expr ")"
     -12: Expr = { Add } left:Expr "+" right:Expr
     -13: Expr = { Sub } left:Expr "-" right:Expr
     -14: Expr = { Mul } left:Expr "*" right:Expr
     -15: Expr = { Div } left:Expr "/" right:Expr
    
      1:
       shift:
            ID -> 20
          Stmt -> 24
      StmtList -> 18
    OptStmtList -> 2
       reduce:
           NUM -> OptStmtList =
           "(" -> OptStmtList =
           EOF -> OptStmtList =
       accept:
          Eval -> ACCEPT
    
      2:
       shift:
           "(" -> 15
            ID -> 14
           NUM -> 13
          Expr -> 4
       OptExpr -> 3
       reduce:
           EOF -> OptExpr =
    
      3:
       reduce:
           EOF -> Eval = OptStmtList OptExpr
    
      4:
       shift:
           "/" -> 11
           "*" -> 9
           "-" -> 7
           "+" -> 5
       reduce:
           EOF -> OptExpr = Expr
    
      5:
       shift:
           "(" -> 15
            ID -> 14
           NUM -> 13
          Expr -> 6
    
      6:
       shift:
           "/" -> 11
           "*" -> 9
       reduce:
           "-" -> Expr = { Add } left:Expr "+" right:Expr
           "+" -> Expr = { Add } left:Expr "+" right:Expr
           ")" -> Expr = { Add } left:Expr "+" right:Expr
           ";" -> Expr = { Add } left:Expr "+" right:Expr
           EOF -> Expr = { Add } left:Expr "+" right:Expr
    
      7:
       shift:
           "(" -> 15
            ID -> 14
           NUM -> 13
          Expr -> 8
    
      8:
       shift:
           "/" -> 11
           "*" -> 9
       reduce:
           "-" -> Expr = { Sub } left:Expr "-" right:Expr
           "+" -> Expr = { Sub } left:Expr "-" right:Expr
           ")" -> Expr = { Sub } left:Expr "-" right:Expr
           ";" -> Expr = { Sub } left:Expr "-" right:Expr
           EOF -> Expr = { Sub } left:Expr "-" right:Expr
    
      9:
       shift:
           "(" -> 15
            ID -> 14
           NUM -> 13
          Expr -> 10
    
     10:
       reduce:
           "/" -> Expr = { Mul } left:Expr "*" right:Expr
           "*" -> Expr = { Mul } left:Expr "*" right:Expr
           "-" -> Expr = { Mul } left:Expr "*" right:Expr
           "+" -> Expr = { Mul } left:Expr "*" right:Expr
           ")" -> Expr = { Mul } left:Expr "*" right:Expr
           ";" -> Expr = { Mul } left:Expr "*" right:Expr
           EOF -> Expr = { Mul } left:Expr "*" right:Expr
    
     11:
       shift:
           "(" -> 15
            ID -> 14
           NUM -> 13
          Expr -> 12
    
     12:
       reduce:
           "/" -> Expr = { Div } left:Expr "/" right:Expr
           "*" -> Expr = { Div } left:Expr "/" right:Expr
           "-" -> Expr = { Div } left:Expr "/" right:Expr
           "+" -> Expr = { Div } left:Expr "/" right:Expr
           ")" -> Expr = { Div } left:Expr "/" right:Expr
           ";" -> Expr = { Div } left:Expr "/" right:Expr
           EOF -> Expr = { Div } left:Expr "/" right:Expr
    
     13:
       reduce:
           "/" -> Expr = { Num } NUM
           "*" -> Expr = { Num } NUM
           "-" -> Expr = { Num } NUM
           "+" -> Expr = { Num } NUM
           ")" -> Expr = { Num } NUM
           ";" -> Expr = { Num } NUM
           EOF -> Expr = { Num } NUM
    
     14:
       reduce:
           "/" -> Expr = { Var } ID
           "*" -> Expr = { Var } ID
           "-" -> Expr = { Var } ID
           "+" -> Expr = { Var } ID
           ")" -> Expr = { Var } ID
           ";" -> Expr = { Var } ID
           EOF -> Expr = { Var } ID
    
     15:
       shift:
           "(" -> 15
            ID -> 14
           NUM -> 13
          Expr -> 16
    
     16:
       shift:
           "/" -> 11
           "*" -> 9
           "-" -> 7
           "+" -> 5
           ")" -> 17
    
     17:
       reduce:
           "/" -> Expr = { Nested } "(" Expr ")"
           "*" -> Expr = { Nested } "(" Expr ")"
           "-" -> Expr = { Nested } "(" Expr ")"
           "+" -> Expr = { Nested } "(" Expr ")"
           ")" -> Expr = { Nested } "(" Expr ")"
           ";" -> Expr = { Nested } "(" Expr ")"
           EOF -> Expr = { Nested } "(" Expr ")"
    
     18:
       shift:
            ID -> 20
          Stmt -> 19
       reduce:
           NUM -> OptStmtList = StmtList
           "(" -> OptStmtList = StmtList
           EOF -> OptStmtList = StmtList
    
     19:
       reduce:
            ID -> StmtList = { Ext } StmtList Stmt
           NUM -> StmtList = { Ext } StmtList Stmt
           "(" -> StmtList = { Ext } StmtList Stmt
           EOF -> StmtList = { Ext } StmtList Stmt
    
     20:
       shift:
           "=" -> 21
    
     21:
       shift:
           "(" -> 15
            ID -> 14
           NUM -> 13
          Expr -> 22
    
     22:
       shift:
           "/" -> 11
           "*" -> 9
           "-" -> 7
           "+" -> 5
           ";" -> 23
    
     23:
       reduce:
            ID -> Stmt = ID "=" Expr ";"
           NUM -> Stmt = ID "=" Expr ";"
           "(" -> Stmt = ID "=" Expr ";"
           EOF -> Stmt = ID "=" Expr ";"
    
     24:
       reduce:
            ID -> StmtList = { New } Stmt
           NUM -> StmtList = { New } Stmt
           "(" -> StmtList = { New } Stmt
           EOF -> StmtList = { New } Stmt

 */
	}

	public static void compileParser(Grammar grammar, String parserName, String packageName, File saveDir) throws IOException
	{
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
		}, parserName, packageName, saveDir);
		
		comp.setGenerateAstStubs(true);
		comp.setDumpParserStates(true);
		comp.compile(grammar);
	}
}
