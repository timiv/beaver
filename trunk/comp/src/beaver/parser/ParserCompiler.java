package beaver.parser;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
			&& writeParserSource(grammar)
			&& (!generateAstStubs 
				|| writeAstTermStub()
				&& writeAstListStubs(grammar) 
				&& writeNodeVisitor(grammar)
				&& writeAstNodeStubs(grammar));
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
			if (doNotWritePassThroughActions)
			{
				if (rule.isValueProducer())
				{
					Production.RHSElement ruleValue = rule.findValueProducer();
					if (ruleValue != null && getType(rule.lhs).equals(ruleValue.fieldType))
					{
						continue;
					}
				}
				else if (!rule.lhs.isOptionalListProducer())
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
	    else if (rule.lhs.isOptionalListProducer())
	    {
	    	if (rule.rhs.length != 0)
	    	{
	    		throw new IllegalStateException();
	    	}
		    out.print("return ");
    		out.print("new ");
    		out.print(returnType);
    		out.print("(");
    		out.print(")");
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
	    Production.RHSElement ruleValue;
	    String args = argsBuffer.toString();
		if (doNotWritePassThroughActions && args.length() == 0 && !rule.lhs.isOptionalListProducer())
		{
			out.print("null");
		}
		else if (doNotWritePassThroughActions && (ruleValue = rule.findValueProducer()) != null && getType(rule.lhs).equals(ruleValue.fieldType))
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
	
	private boolean writeAstTermStub()
	{
		try
		{
    		PrintWriter out = new PrintWriter(new FileWriter(new File(outputDir, "Term" + ".java")));
    		out.println("public class Term {");
    		out.print('\t');
    		out.println("String text;");
    		out.print('\t');
    		out.println("public Term(String text) {");
    		out.print("\t\t");
    		out.println("this.text = text;");
    		out.print('\t');
    		out.println("}");
    		out.println("}");
    		out.close();
		}
		catch (IOException e)
		{
			log.error("Failed writing AST Term source file: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	private boolean writeAstListStubs(Grammar grammar)
	{
		AstList[] lists = findAstLists(grammar);
		for (int i = 0; i < lists.length; i++)
        {
			try
			{
				writeListNode(lists[i]);
			}
			catch (IOException e)
			{
				log.error("Failed writing " + lists[i].listType + " source file: " + e.getMessage());
				return false;
			}
        }
		return true;
	}
	
	private void writeListNode(AstList node) throws IOException
	{
		PrintWriter out = new PrintWriter(new FileWriter(new File(outputDir, node.listType + ".java")));
		out.print("public class ");
		out.print(node.listType);
		out.println(" {");
		
		out.print('\t');
		out.print("public ");
		out.print(node.listType);
		out.println("() {");
		out.print('\t');
		out.println("}");
		
		out.print('\t');
		out.print("public ");
		out.print(node.listType);
		out.print('(');
		out.print(node.itemType);
		out.print(' ');
		out.print(node.itemName);
		out.print(')');
		out.println(" {");
		out.print('\t');
		out.println("}");
		
		out.print('\t');
		out.print("public ");
		out.print(node.listType);
		out.print(" add");
		out.print('(');
		out.print(node.itemType);
		out.print(' ');
		out.print(node.itemName);
		out.print(')');
		out.println(" {");
		out.print("\t\t");
		out.println("return this;");
		out.print('\t');
		out.println("}");
		
		out.println("}");
		out.close();
	}

	private boolean writeAstNodeStubs(Grammar grammar)
	{
		AstType[] types = findAstTypes(grammar);
		for (int i = 0; i < types.length; i++)
        {
			String src = types[i].parentType;
			try
			{
				if (src != null)
				{
					writeAbstractTypeStub(types[i]);
				}
				src = types[i].nodeType;
				writeAstTypeStub(types[i]);
			}
			catch (IOException e)
			{
				log.error("Failed writing " + src + " source file: " + e.getMessage());
				return false;
			}
        }
		return true;
	}

	private void writeAbstractTypeStub(AstType type) throws IOException
	{
		PrintWriter out = new PrintWriter(new FileWriter(new File(outputDir, type.parentType + ".java")));
		out.print("public abstract class ");
		out.print(type.parentType);
		out.println(" {");
		
		out.println("}");
		out.close();
	}

	private void writeAstTypeStub(AstType type) throws IOException
	{
		PrintWriter out = new PrintWriter(new FileWriter(new File(outputDir, type.nodeType + ".java")));
		out.print("public class ");
		out.print(type.nodeType);
		if (type.parentType != null)
		{
			out.print(" extends ");
			out.print(type.parentType);
		}
		out.println(" {");
		
		for (AstNodeField field = type.firstField; field != null; field = field.next)
        {
    		out.print('\t');
    		out.print(field.type);
    		out.print(' ');
    		out.print(field.name);
    		out.println(';');
        }

		for (AstNodeConstructor constructor = type.firstConstructor; constructor != null; constructor = constructor.next)
		{
    		out.print('\t');
    		out.print("public ");
    		out.print(type.nodeType);
    		out.print('(');
    		String sep = "";
    		for (AstNodeField arg = constructor.firstArg; arg != null; arg = arg.next)
    		{
        		out.print(sep);
        		out.print(arg.type);
        		out.print(' ');
        		out.print(arg.name);
        		sep = ", ";
    		}
        	out.print(')');
        	out.println(" {");
    		for (AstNodeField arg = constructor.firstArg; arg != null; arg = arg.next)
    		{
        		out.print("\t\t");
        		out.print("this.");
        		out.print(arg.name);
        		out.print(" = ");
        		out.print(arg.name);
        		out.println(';');
    		}
    		out.print('\t');
    		out.println("}");
		}
		out.println("}");
		out.close();
	}
	
	private boolean writeNodeVisitor(Grammar grammar)
	{
		try
		{
			PrintWriter out = new PrintWriter(new FileWriter(new File(outputDir, "NodeVisitor" + ".java")));
			out.print("public interface ");
			out.print("NodeVisitor");
			out.println(" {");
			for (int i = 0; i < grammar.nonterminals.length; i++)
            {
				Nonterminal nt = grammar.nonterminals[i];
	            if (nt.delegate == null)
	            {
	            	if (nt.isListProducer())
	            	{
	            		writeVisitorMethods(out, nt.name);
	            	}
	            }
            }
			Collection types = new HashSet();
			for (int i = 0; i < grammar.productions.length; i++)
            {
	            Production rule = grammar.productions[i];
	            if (rule.isValueProducer() && !rule.lhs.isListProducer() && !rule.lhs.isOptionalListProducer())
	            {
					Production.RHSElement ruleValue = rule.findValueProducer();
					if (ruleValue != null && getType(rule.lhs).equals(ruleValue.fieldType))
					{
						continue;
					}	            	
	            	String nodeType = rule.getFullName();
	            	if (types.add(nodeType))
	            	{
	            		writeVisitorMethods(out, nodeType);
	            	}
	            }
            }
			out.print('\t');
			out.print("void visit(");
			out.print("Term");
			out.println(" node);");
			
			out.println("}");
			out.close();
		}
		catch (IOException e)
		{
			log.error("Failed writing AST node visitor source file: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	private static void writeVisitorMethods(PrintWriter out, String nodeType)
	{
		out.print('\t');
		out.print("void enter(");
		out.print(nodeType);
		out.println(" node);");
		out.print('\t');
		out.print("void visit(");
		out.print(nodeType);
		out.println(" node);");
		out.print('\t');
		out.print("void leave(");
		out.print(nodeType);
		out.println(" node);");
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
	
	private static class AstList
	{
		String listType;
		String itemType;
		String itemName;
		
		AstList(Nonterminal nt)
		{
			listType = nt.name;
    		Production.RHSElement element = nt.rules[0].findValueProducer();
    		if (element == null)
    		{
    			element = nt.rules[1].findValueProducer();
    			if (element == null)
    			{
    				throw new IllegalStateException();
    			}
    		}
    		itemType = element.fieldType;
    		itemName = element.fieldName;
		}
	}
	
	private static class AstType
	{
		String             parentType;
		String             nodeType;
		AstNodeField       firstField;
		AstNodeConstructor firstConstructor;
		Collection         fieldNames;
		
		AstType(String parentType, String nodeType)
		{
			this.nodeType = nodeType;
			if (!nodeType.equals(parentType))
			{
				this.parentType = parentType;
			}
			this.fieldNames = new HashSet();
		}
		
		void add(AstNodeField field)
		{
			if (fieldNames.add(field.name))
			{
				if (firstField == null)
				{
					firstField = field;
				}
				else
				{
					AstNodeField lastField = firstField;
					while (lastField.next != null)
					{
						lastField = lastField.next;
					}
					lastField.next = field; 
				}
			}
		}
		
		void add(AstNodeConstructor constructor)
		{
			constructor.next = firstConstructor;
			firstConstructor = constructor;
		}
	}
	
	private static class AstNodeField
	{
		AstNodeField next;
		String       type;
		String       name;
		
		AstNodeField(Production.RHSElement rhs)
		{
			type = rhs.fieldType;
			name = rhs.fieldName;
		}
	}
	
	private static class AstNodeConstructor
	{
		AstNodeConstructor next;
		AstNodeField       firstArg;
		
		void add(AstNodeField arg)
		{
			if (firstArg == null)
			{
				firstArg = arg;
			}
			else
			{
				AstNodeField lastArg = firstArg;
				while (lastArg.next != null)
				{
					lastArg = lastArg.next;
				}
				lastArg.next = arg;
			}
		}
	}
	
	private static AstList[] findAstLists(Grammar grammar)
	{
		Collection lists = new ArrayList();
		for (int i = 0; i < grammar.nonterminals.length; i++)
        {
			Nonterminal nt = grammar.nonterminals[i];
            if (nt.delegate == null && nt.isListProducer())
            {
            	lists.add(new AstList(nt));
            }
        }
		return (AstList[]) lists.toArray(new AstList[lists.size()]);
	}
	
	private static AstType[] findAstTypes(Grammar grammar)
	{
		Collection types = new ArrayList();
		Map nameToType = new HashMap();

		for (int i = 0; i < grammar.productions.length; i++)
        {
            Production rule = grammar.productions[i];
    	    if (rule.lhs.isListProducer() || rule.lhs.isOptionalListProducer())
    	    {
    	    	continue;
    	    }
			Production.RHSElement ruleValue = rule.findValueProducer();
			if (ruleValue != null && getType(rule.lhs).equals(ruleValue.fieldType))
			{
    	    	continue;
			}
    	    
			String astTypeName = rule.getFullName();
			AstType type = (AstType) nameToType.get(astTypeName);
			if (type == null)
			{
				types.add(type = new AstType(getType(rule.lhs), astTypeName));
				nameToType.put(astTypeName, type);
			}
			
    	    for (int j = 0; j < rule.rhs.length; j++)
            {
    	    	Production.RHSElement arg = rule.rhs[j];
                if (arg.fieldType != null)
                {
                	type.add(new AstNodeField(arg));
                }
            }
    	    
    	    AstNodeConstructor constructor = new AstNodeConstructor(); 
    	    for (int j = 0; j < rule.rhs.length; j++)
            {
    	    	Production.RHSElement arg = rule.rhs[j];
                if (arg.fieldType != null)
                {
                	constructor.add(new AstNodeField(arg));
                }
            }
    	    type.add(constructor);
        }	
		return (AstType[]) types.toArray(new AstType[types.size()]);
	}
}
