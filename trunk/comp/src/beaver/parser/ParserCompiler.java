package beaver.parser;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import beaver.cc.Log;

public class ParserCompiler
{
	Log     log;
	String  parserName;
	File    outputDir;
	boolean doNotWritePassThroughActions;
	boolean generateAstStubs;

	public ParserCompiler(Log log, String parserName, File outputDir)
	{
		this.log = log;
		this.parserName = parserName;
		this.outputDir = outputDir;
	}
	
	public void setDoNotWritePassThroughActions(boolean flag)
	{
		doNotWritePassThroughActions = flag;
	}
	
	public void setGenerateAstStubs(boolean flag)
	{
		if ((generateAstStubs = flag))
		{
			doNotWritePassThroughActions = true;
		}
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
    	writeParserActions(out, grammar);
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
	
	private void writeParserActions(PrintWriter out, Grammar grammar)
	{
		for (int i = 0; i < grammar.productions.length; i++)
        {
			Production rule = grammar.productions[i];
			String returnType = getType(rule.lhs);
			if (doNotWritePassThroughActions)
			{
				if (!rule.isValueProducer())
				{
					continue;
				}
				Symbol ruleValue = rule.findSingleValue();
				if (ruleValue != null && getType(ruleValue).equals(returnType))
				{
					continue;
				}
			}
		    if (generateAstStubs)
		    {
		    	writeAstAction(out, rule);
		    }
		    else
		    {
		    	writeAbstractAction(out, rule);
		    }
        }
	}

	private void writeAstAction(PrintWriter out, Production rule)
	{
		String returnType = getType(rule.lhs);
		out.print('\t');
	    out.print("protected ");
	    out.print(returnType);
	    out.print(" make");
	    out.print(rule.getFullName());
	    out.print("(");	    
	    String sep = "";
	    for (int j = 0; j < rule.rhs.length; j++)
        {
	    	Production.RHSElement arg = rule.rhs[j];
            if (arg.fieldType != null)
            {
	            out.print(sep);
	            out.print(arg.fieldType);
	            out.print(' ');
	            out.print(arg.fieldName);
	            sep = ", ";
            }
        }
	    out.println(") {");
		out.print("\t\t");
	    if (rule.lhs.isListProducer())
	    {
		    out.print("return ");
	    	boolean isAdd = false;
		    for (int j = 0; j < rule.rhs.length; j++)
	        {
		    	Production.RHSElement arg = rule.rhs[j];
	            if (arg.fieldType != null)
	            {
	            	if (arg.fieldType.equals(returnType))
	            	{
	            		out.print(arg.fieldName);
	            		out.print(".add(");
	            		isAdd = true;
	            	}
	            	else if (isAdd)
	            	{
	            		out.print(arg.fieldName);
	            		out.print(")");
	            	}
	            	else
	            	{
	            		out.print("new ");
	            		out.print(returnType);
	            		out.print("(");
	            		out.print(arg.fieldName);
	            		out.print(")");
	            	}
	            }
	        }
		    out.println(";");
	    }
	    else
	    {
		    out.print("return ");
		    out.print("new ");
		    out.print(rule.getFullName());
		    out.print("(");	    
		    sep = "";
		    for (int j = 0; j < rule.rhs.length; j++)
	        {
		    	Production.RHSElement arg = rule.rhs[j];
	            if (arg.fieldType != null)
	            {
		            out.print(sep);
		            out.print(arg.fieldName);
		            sep = ", ";
	            }
	        }
		    out.print(")");	    
		    out.println(";");	    
	    }
		out.print('\t');
	    out.println('}');
	}
	
	private void writeAbstractAction(PrintWriter out, Production rule)
	{
		out.print('\t');
	    out.print("protected ");
	    out.print("abstract ");
	    out.print(getType(rule.lhs));
	    out.print(" make");
	    out.print(rule.getFullName());
	    out.print('(');	    
	    String sep = "";
	    for (int j = 0; j < rule.rhs.length; j++)
        {
	    	Production.RHSElement arg = rule.rhs[j];
            if (arg.fieldType != null)
            {
	            out.print(sep);
	            out.print(arg.fieldType);
	            out.print(' ');
	            out.print(arg.fieldName);
	            sep = ", ";
            }
        }
	    out.print(')');
	    out.println(';');
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
	
	private void writeProductionReduceCase(PrintWriter out, Production rule)
	{
		StringBuffer argsBuffer = new StringBuffer();
    	int lastRhsItem = rule.rhs.length - 1; 
	    String sep = "";
	    for (int i = 0; i < rule.rhs.length; i++)
        {
	    	Production.RHSElement arg = rule.rhs[i];
            if (arg.fieldType != null)
            {
				out.print("\t\t\t\t");
				out.print(arg.fieldType);
				out.print(' ');
				out.print(arg.fieldName);
				out.print(" = (");
				out.print(arg.fieldType);
				out.print(") stack[top");
				int stackOffset = lastRhsItem - i;
				if (stackOffset > 0)
				{
					out.print(" + ");
					out.print(stackOffset);
				}
				out.println("].value();");
				
				argsBuffer.append(sep).append(arg.fieldName);
	            sep = ", ";
            }
        }
	    out.println();
		out.print("\t\t\t\t");
	    out.print("return symbol(");
	    Symbol ruleValue;
	    String args = argsBuffer.toString();
		if (doNotWritePassThroughActions && args.length() == 0)
		{
		    out.print("null");
		}
		else if (doNotWritePassThroughActions && (ruleValue = rule.findSingleValue()) != null && getType(ruleValue).equals(getType(rule.lhs)))
		{
		    out.print(args);
		}
		else
		{
		    out.print("make");
		    out.print(rule.getFullName());
		    out.print("(");
		    out.print(args);
		    out.print(")");
		}
	    out.println(");");    
	}
	
	private static String getType(Symbol symbol)
	{
        if (symbol instanceof Terminal)
        {
        	return "Term";
        }
    	Nonterminal nt = (Nonterminal) symbol;
    	if (nt.delegate == null)
    	{
    		return nt.name;
    	}
    	else if (nt.delegate instanceof Terminal)
    	{
    		return "Term";
    	}
    	else
    	{
    		return nt.delegate.name;
    	}
	}
}
