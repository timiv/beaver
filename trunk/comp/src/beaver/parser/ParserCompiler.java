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
	boolean dumpParserStates;
	String  packageName;

	public ParserCompiler(Log log, String parserName, String packageName, File outputDir)
	{
		this.log = log;
		this.parserName = parserName;
		this.packageName = packageName;
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
	
	public void setDumpParserStates(boolean flag)
	{
		dumpParserStates = flag;
	}

	public boolean compile(Grammar grammar)
	{
		AstList[] astListTypes;
		AstType[] astNodeTypes;
		ParserState firstState = new ParserStatesBuilder().buildParserStates(grammar);
		return resolveConflicts(firstState)
			&& writeParsingTables(firstState, grammar)
			&& writeParserSource(grammar)
			&& (!generateAstStubs 
				|| writeAstListStubs(astListTypes = findAstLists(grammar)) 
				&& writeAstNodeStubs(astNodeTypes = findAstTypes(grammar), astListTypes)
				&& writeAstTermStub(astListTypes)
				&& writeNodeVisitor(astNodeTypes, astListTypes) 
				&& writeTreeWalker(astNodeTypes, astListTypes)
				)
			&& (!dumpParserStates 
				|| writeParserStates(firstState, grammar))
		;
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
	
	private boolean writeParserStates(ParserState firstState, Grammar grammar)
	{
		try
		{
			PrintWriter out = new PrintWriter(new File(outputDir, parserName + "-states.lst"));
			// symbols
			for (int i = 0; i < grammar.terminals.length; i++)
			{
				Symbol symbol = grammar.terminals[i];
				if (symbol.id < 100)
				{
					out.print(' ');
					if (symbol.id < 10)
					{
						out.print(' ');
						
					}
					out.print(symbol.id);
					out.print(": ");
					out.println(symbol);
				}
			}
			for (int i = 0; i < grammar.nonterminals.length; i++)
			{
				Symbol symbol = grammar.nonterminals[i];
				if (symbol.id < 100)
				{
					out.print(' ');
					if (symbol.id < 10)
					{
						out.print(' ');
						
					}
					out.print(symbol.id);
					out.print(": ");
					out.println(symbol);
				}
			}
			out.println();
			// productions
			for (int i = 0; i < grammar.productions.length; i++)
			{
				Production rule = grammar.productions[i];
				if (rule.id < 100)
				{
					out.print(' ');
					if (rule.id < 10)
					{
						out.print(' ');
						
					}
					out.print(~rule.id);
					out.print(": ");
					out.println(rule);
				}
			}
			out.println();
			// states
			for (ParserState state = firstState; state != null; state = state.next)
			{
	    		if (state.id < 100)
	    		{
	    			out.print(' ');
	    			if (state.id < 10)
	    			{
	    				out.print(' ');
	    			}
	    		}
				out.println(state);
			}		
			out.close();
		}
		catch (IOException e)
		{
			log.error("Failed writing file with a list of parser states: " + e.getMessage());
			return false;
		}
		return true;
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
	    out.print("package ");
	    out.print(packageName);
	    out.println(';');
	    out.println();
	    out.print("public ");
	    if (!generateAstStubs)
	    {
	    	out.print("abstract ");
	    }
	    out.print("class ");
	    out.print(parserName);
	    out.print(" extends ");
	    out.print("beaver.Parser");
	    out.println(" {");
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
		out.println("() throws java.io.IOException {");
		out.print("\t\t");
		out.print("super(");
	    out.print(parserName);
		out.print(".class.getResourceAsStream(\"");
	    out.print(parserName);
		out.println(".bpt\"));");
		out.print('\t');
		out.println("}");
	}
	
	private void writeTermMaker(PrintWriter out)
	{
		out.print('\t');
	    out.print("protected ");
	    out.print("Object");
	    out.print(" make");
	    out.print("Term");
		out.print("(Object text, int line, int column)");
		out.println(" {");
		out.print("\t\t");
	    out.print("return ");
		out.print("new ");
	    out.print("Term");
		out.print('(');
	    out.print("(String) ");
	    out.print("text");
		out.print(')');
	    out.println(';');
		out.print('\t');
		out.println("}");
	}
	
	private void writeAbstractTermMaker(PrintWriter out)
	{
		out.print('\t');
	    out.print("protected ");
	    out.print("abstract ");
	    out.print("Object");
	    out.print(" make");
	    out.print("Term");
		out.print("(Object text, int line, int column)");
		out.println(';');
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
	    if (generateAstStubs)
	    {
	    	writeTermMaker(out);
	    }
	    else
	    {
	    	writeAbstractTermMaker(out);
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
		out.print("protected ");
	    out.println("Object reduce(Object[] stack, int top, int rule) {");
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
    	int lastRhsItem = rule.rhs.length - 1; 
	    int ruleValueIndex;
	    if (doNotWritePassThroughActions && (ruleValueIndex = rule.findValueProducerIndex()) >= 0 && getType(rule.lhs).equals(rule.rhs[ruleValueIndex].fieldType))
	    {
		    out.println();
			out.print("\t\t\t\t");
		    out.print("return ");
			out.print("stack[top");
			int stackOffset = lastRhsItem - ruleValueIndex;
			if (stackOffset > 0)
			{
				out.print(" + ");
				out.print(stackOffset);
			}
			out.println("];");
	    }
	    else
	    {
			StringBuffer argsBuffer = new StringBuffer();
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
					out.print(") ");
					out.print("stack[top");
					int stackOffset = lastRhsItem - i;
					if (stackOffset > 0)
					{
						out.print(" + ");
						out.print(stackOffset);
					}
					out.println("];");
					
					argsBuffer.append(sep).append(arg.fieldName);
		            sep = ", ";
	            }
	        }
		    out.println();
			out.print("\t\t\t\t");
		    out.print("return ");
		    String args = argsBuffer.toString();
			if (doNotWritePassThroughActions && args.length() == 0 && !rule.lhs.isOptionalListProducer())
			{
				out.print("null");
			}
			else
			{
			    out.print("make");
			    out.print(rule.getFullName());
			    out.print("(");
			    out.print(args);
			    out.print(")");
			}
		    out.println(";");    
	    }
	}
	
	private boolean writeAstTermStub(AstList[] lists)
	{
		try
		{
    		PrintWriter out = new PrintWriter(new FileWriter(new File(outputDir, "Term" + ".java")));
    	    out.print("package ");
    	    out.print(packageName);
    	    out.println(';');
    	    out.println();
    		out.print("public ");
    		out.print("class ");
    		out.print("Term");
    		out.println(" {");
    		if (isListElementType("Term", lists))
    		{
        		out.print('\t');
        		out.print("Term");
        		out.println(" next;");
    		}
    		out.print('\t');
    		out.print("protected ");
    		out.println("String text;");
    		out.println();
    		out.print('\t');
    		out.print("protected ");
    		out.print("Term");
    		out.println("(String text) {");
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
	
	private boolean writeAstListStubs(AstList[] lists)
	{
		for (int i = 0; i < lists.length; i++)
        {
			try
			{
				writeListNode(lists[i], lists);
			}
			catch (IOException e)
			{
				log.error("Failed writing " + lists[i].listType + " source file: " + e.getMessage());
				return false;
			}
        }
		return true;
	}
	
	private void writeListNode(AstList node, AstList[] lists) throws IOException
	{
		PrintWriter out = new PrintWriter(new FileWriter(new File(outputDir, node.listType + ".java")));
	    out.print("package ");
	    out.print(packageName);
	    out.println(';');
	    out.println();
		out.print("public ");
		out.print("class ");
		out.print(node.listType);
		out.println(" {");
		if (isListElementType(node.listType, lists))
		{
			out.print('\t');
			out.print(node.listType);
			out.println(" next;");
		}
		out.print('\t');
		out.print("protected ");
		out.print(node.itemType);
		out.println(" first;");
		out.print('\t');
		out.print("protected ");
		out.print(node.itemType);
		out.println(" last;");
		out.print('\t');
		out.print("protected ");
		out.print("int");
		out.println(" size;");
		out.println();
		
		out.print('\t');
		out.print("protected ");
		out.print(node.listType);
		out.println("() {");
		out.print('\t');
		out.println("}");
		
		out.print('\t');
		out.print("protected ");
		out.print(node.listType);
		out.print('(');
		out.print(node.itemType);
		out.print(' ');
		out.print(node.itemName);
		out.print(')');
		out.println(" {");
		out.print("\t\t");
		out.print("first = last = ");
		out.print(node.itemName);
		out.println(";");
		out.print("\t\t");
		out.println("size = 1;");
		out.print('\t');
		out.println("}");
		
		out.print('\t');
		out.print("protected ");
		out.print(node.listType);
		out.print(" add");
		out.print('(');
		out.print(node.itemType);
		out.print(' ');
		out.print(node.itemName);
		out.print(')');
		out.println(" {");
		out.print("\t\t");
		out.print("last = last.next = ");
		out.print(node.itemName);
		out.println(";");
		out.print("\t\t");
		out.println("++size;");
		out.print("\t\t");
		out.println("return this;");
		out.print('\t');
		out.println("}");

		out.print('\t');
		out.print("public ");
		out.print("int");
		out.println(" size() {");
		out.print("\t\t");
		out.println("return size;");
		out.print('\t');
		out.println("}");

		out.println("}");
		out.close();
	}

	private boolean writeAstNodeStubs(AstType[] types, AstList[] lists)
	{
		for (int i = 0; i < types.length; i++)
        {
			String src = types[i].parentType;
			try
			{
				if (src != null)
				{
					writeAbstractTypeStub(types[i], lists);
				}
				src = types[i].nodeType;
				writeAstTypeStub(types[i], lists);
			}
			catch (IOException e)
			{
				log.error("Failed writing " + src + " source file: " + e.getMessage());
				return false;
			}
        }
		return true;
	}

	private void writeAbstractTypeStub(AstType type, AstList[] lists) throws IOException
	{
		PrintWriter out = new PrintWriter(new FileWriter(new File(outputDir, type.parentType + ".java")));
	    out.print("package ");
	    out.print(packageName);
	    out.println(';');
	    out.println();
		out.print("public ");
		out.print("abstract ");
		out.print("class ");
		out.print(type.parentType);
		out.println(" {");
		if (isListElementType(type.parentType, lists))
		{
			out.print('\t');
    		out.print("protected ");
			out.print(type.parentType);
			out.println(" next;");
    		out.println();
		}
		out.print('\t');
		out.print("abstract ");
		out.print("void dispatch(NodeVisitor visitor)");
		out.println(';');
		out.println("}");
		out.close();
	}

	private void writeAstTypeStub(AstType type, AstList[] lists) throws IOException
	{
		PrintWriter out = new PrintWriter(new FileWriter(new File(outputDir, type.nodeType + ".java")));
	    out.print("package ");
	    out.print(packageName);
	    out.println(';');
	    out.println();
		out.print("public ");
		out.print("class ");
		out.print(type.nodeType);
		if (type.parentType != null)
		{
			out.print(" extends ");
			out.print(type.parentType);
		}
		out.println(" {");
		if (isListElementType(type.nodeType, lists))
		{
			out.print('\t');
    		out.print("protected ");
			out.print(type.nodeType);
			out.println(" next;");
		}
		
		for (AstNodeField field = type.firstField; field != null; field = field.next)
        {
    		out.print('\t');
    		out.print("protected ");
    		out.print(field.type);
    		out.print(' ');
    		out.print(field.name);
    		out.println(';');
        }
		out.println();

		for (AstNodeConstructor constructor = type.firstConstructor; constructor != null; constructor = constructor.next)
		{
    		out.print('\t');
    		out.print("protected ");
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
		
		if (type.parentType != null)
		{
    		out.print('\t');
    		out.print("void dispatch(NodeVisitor visitor)");
    		out.println(" {");
    		out.print("\t\t");
    		out.println("visitor.visit(this);");
    		out.print('\t');
    		out.println("}");
		}		
		out.println("}");
		out.close();
	}
	
	private boolean writeNodeVisitor(AstType[] astTypes, AstList[] astLists)
	{
		try
		{
			PrintWriter out = new PrintWriter(new FileWriter(new File(outputDir, "NodeVisitor" + ".java")));
		    out.print("package ");
		    out.print(packageName);
		    out.println(';');
		    out.println();
			out.print("public ");
			out.print("interface ");
			out.print("NodeVisitor");
			out.println(" {");
			for (int i = 0; i < astLists.length; i++)
            {
        		writeVisitorMethods(out, astLists[i].listType);
            }
			for (int i = 0; i < astTypes.length; i++)
            {
        		writeVisitorMethods(out, astTypes[i].nodeType);
            }
			out.print('\t');
			out.print("void visit(");
			out.print("Term");
			out.print(" term)");
			out.println(';');
			
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
	
	private boolean writeTreeWalker(AstType[] astTypes, AstList[] astLists)
	{
		try
		{
			PrintWriter out = new PrintWriter(new FileWriter(new File(outputDir, "AstTreeWalker" + ".java")));
		    out.print("package ");
		    out.print(packageName);
		    out.println(';');
		    out.println();
			out.print("public class ");
			out.print("AstTreeWalker");
			out.print(" implements ");
			out.print("NodeVisitor");
			out.println(" {");
			
			Collection allTypes = new HashSet();
			for (int i = 0; i < astLists.length; i++)
			{
				allTypes.add(astLists[i].listType);
			}
			for (int i = 0; i < astTypes.length; i++)
			{
				allTypes.add(astTypes[i].nodeType);
			}
			allTypes.add("Term");
			
			for (int i = 0; i < astLists.length; i++)
            {
        		writeWalkerMethods(out, astLists[i], allTypes);
            }
			for (int i = 0; i < astTypes.length; i++)
            {
        		writeWalkerMethods(out, astTypes[i], allTypes);
            }
			out.print('\t');
			out.print("public ");
			out.print("void visit(");
			out.print("Term");
			out.print(" node)");
			out.println(" {");
			out.print('\t');
			out.println("}");
			
			out.println("}");
			out.close();
		}
		catch (IOException e)
		{
			log.error("Failed writing AST tree walker source file: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	private static void writeVisitorMethods(PrintWriter out, String nodeType)
	{
		String argName = typeToName(nodeType);
		out.print('\t');
		out.print("void enter(");
		out.print(nodeType);
		out.print(' ');
		out.print(argName);
		out.println(");");
		out.print('\t');
		out.print("void visit(");
		out.print(nodeType);
		out.print(' ');
		out.print(argName);
		out.println(");");
		out.print('\t');
		out.print("void leave(");
		out.print(nodeType);
		out.print(' ');
		out.print(argName);
		out.println(");");
	}

	private static void writeWalkerMethods(PrintWriter out, AstList list, Collection allTypes)
	{
		String argName = typeToName(list.listType);
		
		out.print('\t');
		out.print("public ");
		out.print("void enter(");
		out.print(list.listType);
		out.print(' ');
		out.print(argName);
		out.print(')');
		out.println(" {");
		out.print('\t');
		out.println("}");
		
		out.print('\t');
		out.print("public ");
		out.print("void visit(");
		out.print(list.listType);
		out.print(' ');
		out.print(argName);
		out.print(')');
		out.println(" {");

		out.print("\t\t");
		out.print("enter(");	
		out.print(argName);
		out.println(");");
		
		out.print("\t\t");
		out.print("for (");
		out.print(list.itemType);
		out.print(' ');
		out.print(list.itemName);
		out.print(" = ");
		out.print(argName);
		out.print(".first; ");
		out.print(list.itemName);
		out.print(" != null; ");
		out.print(list.itemName);
		out.print(" = ");
		out.print(list.itemName);
		out.print(".next)");
		out.println(" {");
		
		out.print("\t\t\t");
		if (allTypes.contains(list.itemType))
		{
			out.print("visit(");
			out.print(list.itemName);
			out.println(");");
		}
		else // abstract AST type
		{
			out.print(list.itemName);
			out.println(".dispatch(this);");
		}
		out.print("\t\t");
		out.println("}");

		out.print("\t\t");
		out.print("leave(");
		out.print(argName);
		out.println(");");
		
		out.print('\t');
		out.println("}");
		
		out.print('\t');
		out.print("public ");
		out.print("void leave(");
		out.print(list.listType);
		out.print(' ');
		out.print(argName);
		out.print(')');
		out.println(" {");
		out.print('\t');
		out.println("}");
	}
	
	private static void writeWalkerMethods(PrintWriter out, AstType node, Collection allTypes)
	{
		String argName = typeToName(node.nodeType);
		
		out.print('\t');
		out.print("public ");
		out.print("void enter(");
		out.print(node.nodeType);
		out.print(' ');
		out.print(argName);
		out.print(')');
		out.println(" {");
		out.print('\t');
		out.println("}");
		
		out.print('\t');
		out.print("public ");
		out.print("void visit(");
		out.print(node.nodeType);
		out.print(' ');
		out.print(argName);
		out.print(')');
		out.println(" {");

		out.print("\t\t");
		out.print("enter(");
		out.print(argName);
		out.println(");");

		for (AstNodeField field = node.firstField; field != null; field = field.next)
        {
			out.print("\t\t");
			if (field.canBeNull)
			{
				out.print("if (");
				out.print(argName);
				out.print('.');
				out.print(field.name);
				out.println(" != null) { ");
				out.print("\t\t\t");
			}
			if (allTypes.contains(field.type))
			{
				out.print("visit(");
				out.print(argName);
				out.print('.');
				out.print(field.name);
				out.println(");");
			}
			else // abstract AST type
			{
				out.print(argName);
				out.print('.');
				out.print(field.name);
				out.println(".dispatch(this);");
			}
			if (field.canBeNull)
			{
				out.print("\t\t");
				out.println("}");
			}
        }
		
		out.print("\t\t");
		out.print("leave(");
		out.print(argName);
		out.println(");");
		
		out.print('\t');
		out.println("}");
		
		out.print('\t');
		out.print("public ");
		out.print("void leave(");
		out.print(node.nodeType);
		out.print(' ');
		out.print(argName);
		out.print(')');
		out.println(" {");
		out.print('\t');
		out.println("}");
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
	
	private static String typeToName(String type)
	{
		String name = Character.toLowerCase(type.charAt(0)) + type.substring(1);
		if (name.equals(type))
		{
			name = "_" + name;
		}
		return name;
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
		boolean      canBeNull;
		
		AstNodeField(Production.RHSElement rhs)
		{				
			type = rhs.fieldType;
			name = rhs.fieldName;
			canBeNull = rhs.symbol instanceof Nonterminal && ((Nonterminal) rhs.symbol).isOptional() && !((Nonterminal) rhs.symbol).isOptionalListProducer(); 
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
    	    if (rule.lhs.isOptional())
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

	private static boolean isListElementType(String typeName, AstList[] lists)
	{
		for (int i = 0; i < lists.length; i++)
        {
	        if (typeName.equals(lists[i].itemType))
	        	return true;
        }
		return false;
	}
}
