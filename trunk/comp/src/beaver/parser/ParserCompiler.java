package beaver.parser;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;

import beaver.cc.Log;

public class ParserCompiler
{
	private Log    log;
	private String parserName;
	private File   outputDir;

	public ParserCompiler(Log log, String parserName, File outputDir)
	{
		this.log = log;
		this.parserName = parserName;
		this.outputDir = outputDir;
	}

	public boolean compile(Grammar grammar)
	{
		ParserState firstState = new ParserStatesBuilder().buildParserStates(grammar);
		return resolveConflicts(firstState)
			&& writeParsingTables(firstState, grammar)
			&& writeParserSource(grammar);
	}
	
	private boolean resolveConflicts(ParserState firstState)
	{
		boolean _continue_ = true;
		for (ParserState state = firstState; state != null; state = state.next)
		{
			ParserAction.Conflict conflict = state.resolveConflicts(null);
			if (conflict != null)
			{
				StringBuffer text = new StringBuffer(500);
				text.append("Cannot resolve conflict");
				if (conflict.next != null)
				{
					text.append('s');
				}
				text.append(" in state ").append(state);
				for (; conflict != null; conflict = conflict.next)
				{
					text.append("  ").append(conflict).append('\n');
				}
				log.error(text.toString());
				_continue_ = false;
			}
		}
		return _continue_;
	}
	
	private boolean writeParsingTables(ParserState firstState, Grammar grammar)
	{
		CompressedParsingTables tables = new CompressedParsingTables(firstState);
		try
		{
			DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(outputDir, parserName + ".bpt")));
			out.writeBytes("BPT>");
			tables.writeTo(out);
			grammar.writeTo(out);
			out.close();
		}
		catch (IOException e)
		{
			log.error("Failed writing serialized parser tables file: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	private boolean writeParserSource(Grammar grammar)
	{
		try
		{
			PrintWriter out = new PrintWriter(new FileWriter(new File(outputDir, parserName + ".java")));
			writeParserClass(out, grammar);
			out.close();
		}
		catch (IOException e)
		{
			log.error("Failed writing parser source file: " + e.getMessage());
			return false;
		}
		return true;
	}

	private void writeParserClass(PrintWriter out, Grammar grammar)
    {
	    out.print("public abstract class ");
	    out.print(parserName);
	    out.println(" extends beaver.Parser {");
	    writeParserConstructor(out);
	    writeAbstractActions(out, grammar);
	    writeReduceSwitch(out, grammar);
	    out.println("}");
    }
	
	private void writeParserConstructor(PrintWriter out)
	{
		out.print('\t');
	    out.print("public ");
	    out.print(parserName);
		out.println("() {");
		out.print("\t\t");
		out.print("super(");
	    out.print(parserName);
		out.print(".class.getResourceAsStream(\"");
	    out.print(parserName);
		out.println(".bpt\"));");
		out.print('\t');
		out.println("}");
	}
	
	private void writeAbstractActions(final PrintWriter out, Grammar grammar)
	{
		for (int i = 0; i < grammar.productions.length; i++)
        {
			Production rule = grammar.productions[i];
			out.print('\t');
		    out.print("protected abstract ");
		    if (rule.lhs.delegate != null)
		    {
			    out.print(rule.lhs.delegate instanceof Terminal ? "Term" : rule.lhs.delegate.name);
		    }
		    else
		    {
			    out.print(rule.lhs.name);
		    }
		    out.print(" make");
		    out.print(rule.getFullName());
		    out.print('(');
		    forEach(rule.rhs, new ProductionRHSVisitor()
		    {
			    String sep = "";
		    	
				public void visit(int argNum, String type, String name)
		        {
		            out.print(sep);
		            out.print(type);
		            out.print(' ');
		            out.print(name);
		            sep = ", ";
		        }
			});
		    out.print(')');
		    out.println(';');
        }
	}
	
	private void writeReduceSwitch(PrintWriter out, Grammar grammar)
	{
		out.print('\t');
	    out.println("protected Symbol reduce(Symbol[] stack, int top, int rule) {");
		out.print("\t\t");
		out.println("switch (rule) {");
		for (int i = 0; i < grammar.productions.length; i++)
		{
			Production rule = grammar.productions[i];
			out.print("\t\t\t");
			out.print("case ");
			out.print(rule.id);
			out.print(": { // ");
			out.println(rule);
			writeProductionReduceCase(out, rule);
			out.print("\t\t\t");
			out.println("}");
		}
		out.print("\t\t");
		out.println("}");
		out.print("\t\t");
		out.println("throw new IndexOutOfBoundsException(\"production #\" + rule);");
		out.print('\t');
		out.println("}");
	}
	
	private void writeProductionReduceCase(final PrintWriter out, final Production rule)
	{
		final StringBuffer makeArgs = new StringBuffer();
		
	    forEach(rule.rhs, new ProductionRHSVisitor()
	    {
	    	int lastRhsItem = rule.rhs.length - 1; 
		    String sep = "";
	    	
			public void visit(int argNum, String type, String name)
	        {
				out.print("\t\t\t\t");
				out.print(type);
				out.print(' ');
				out.print(name);
				out.print(" = (");
				out.print(type);
				out.print(") stack[top");
				int stackOffset = lastRhsItem - argNum;
				if (stackOffset > 0)
				{
					out.print(" + ");
					out.print(stackOffset);
				}
				out.println("].value();");
				
				makeArgs.append(sep).append(name);
	            sep = ", ";
	        }
		});
	    out.println();
		out.print("\t\t\t\t");
	    out.print("return symbol(make");
	    out.print(rule.getFullName());
	    out.print("(");
	    out.print(makeArgs.toString());
	    out.println("));");    
	}
	
	private static void forEach(Production.RHSElement[] ruleRhs, ProductionRHSVisitor visitor)
	{
        Collection names = new HashSet();
	    for (int i = 0; i < ruleRhs.length; i++)
        {
            Production.RHSElement arg = ruleRhs[i];
            if (arg.symbol.isValueProducer())
            {
                String argType, argName;
                if (arg.symbol instanceof Terminal)
                {
                	argType = "Term";
                	argName = arg.name != null ? arg.name : arg.symbol.name.toLowerCase();  
                }
                else
                {
                	Nonterminal ntArg = (Nonterminal) arg.symbol;
                	if (ntArg.delegate == null)
                	{
                		argType = ntArg.name;
                	}
                	else if (ntArg.delegate instanceof Terminal)
                	{
                		argType = "Term";
                	}
                	else
                	{
                		argType = ntArg.delegate.name;
                	}
                	argName = arg.name != null ? arg.name : Character.toLowerCase(ntArg.name.charAt(0)) + ntArg.name.substring(1); 
                }
                String nameProbe = argName;
                int argNameCount = 1;
                while (names.contains(nameProbe))
                {
                	nameProbe = argName + Integer.toString(++argNameCount); 
                }
                names.add(argName = nameProbe);
    
                visitor.visit(i, argType, argName);
            }
        }
	}
	
	static interface ProductionRHSVisitor
	{
		void visit(int argNum, String type, String name);
	}
}
