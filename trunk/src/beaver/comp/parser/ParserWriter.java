/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */

package beaver.comp.parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Generates Java source for a parser's base implementation
 * 
 * @author Alexander Demenchuk
 * 
 */
public class ParserWriter
{
	String     beaverVersion;
	String[]   sourceFileComment;
	String     parserPackageName;
	String     astPackageName;
	String     parserName;

	boolean	   generateListBuilders;
	boolean    generateNodeBuilders;
	boolean	   generateAst;
	
	Grammar    grammar;
	Map        constTermNames;
	Map        symbolTypes;
	Collection termTypes;
	Callback[] callbacks;

	public ParserWriter(String parserName, Grammar grammar, String defaultTermType, Map types)
	{
		this.grammar = grammar;
		this.parserName = parserName;
		this.symbolTypes = initTypes(types, defaultTermType, grammar);
		Collection optSymbolNames = (Collection) symbolTypes.remove("...");
		this.termTypes = (Collection) symbolTypes.remove(":::");
		this.callbacks = createCalbacks(grammar, symbolTypes, optSymbolNames);
	}

	public void setConstTermNames(Map text2name)
	{
		constTermNames = text2name;
	}

	public void setParserPackageName(String name)
	{
		parserPackageName = name;
	}
	
	public void setAstPackageName(String name)
	{
		astPackageName = name;
	}

	public void setFileComment(String[] text)
	{
		sourceFileComment = text;
	}

	public void setCompilerVersion(String text)
	{
		beaverVersion = text;
	}

	public void setGenerateListBuilders(boolean opt)
    {
    	this.generateListBuilders = opt;
    }

	public void setGenerateNodeBuilders(boolean opt)
    {
    	this.generateNodeBuilders = opt;
    }
	
	public void setGenerateAst(boolean opt)
	{
		this.generateAst = opt;
	}

	public void writeParserSources(File dir) throws IOException
	{
		File of;
		if ( parserPackageName != null )
		{
			File od = new File(dir, parserPackageName.replace('.', '/'));
			if ( !od.exists() )
			{
				od.mkdirs();
			}
			of = new File(od, parserName + ".java");
		}
		else
		{
			of = new File(dir, parserName + ".java");
		}
		
		long lastModified = of.lastModified();
		
		if ( generateAst )
		{
			writeSemanticTypes(dir, lastModified);
		}
		
		PrintWriter out = new PrintWriter(new FileWriter(of));
		try
		{
			writeParserSource(out);
		}
		finally
		{
			out.close();
		}
	}

	private void writeParserSource(PrintWriter out)
	{
		writeFileComment(out);
		writeParserPackage(out);
		out.println("import java.io.DataInputStream;");
		out.println("import java.io.IOException;");
		out.println();
		out.println("import beaver.ParsingTables;");
		out.println("import beaver.Symbol;");
		out.println();
		if ( astPackageName != null && !astPackageName.equals(parserPackageName) )
		{
			out.print("import ");
			out.print(astPackageName);
			out.println(".*;");
			out.println();
		}
		writeClassComment(out);
		out.print("public abstract class ");
		out.print(parserName);
		out.println(" extends beaver.Parser");
		out.println("{");
		writeTerminals(out);
		out.println();
		writeCallbacks(out);
		out.println();
		writeRulesSwitch(out);
		out.println();
		writeParserConstructor(out);
		out.println();
		writeParsingTables(out);
		out.println("}");
	}

	private void writeFileComment(PrintWriter out)
	{
		if ( sourceFileComment != null )
		{
			for ( int i = 0; i < sourceFileComment.length; i++ )
            {
				out.println(sourceFileComment[i]);
            }
		}
	}

	private void writeParserPackage(PrintWriter out)
	{
		if ( parserPackageName != null )
		{
			out.print("package ");
			out.print(parserPackageName);
			out.println(";");
			out.println();
		}
	}

	private void writeClassComment(PrintWriter out)
	{
		out.println("/**");
		out.print(" * @author <a href=\"http://beaver.sourceforge.net\">Beaver</a>");
		if ( beaverVersion != null )
		{
			out.print(" (");
			out.print(beaverVersion);
			out.print(")");
		}
		out.println(" parser generator");
		out.println(" */");
	}

	private void writeTerminals(PrintWriter out)
	{
		int w = 0;
		for ( int i = 0; i < grammar.terminals.length; i++ )
		{
			if ( grammar.terminals[i] instanceof Terminal.Const )
			{
				String name, text = ((Terminal.Const) grammar.terminals[i]).text;
				if ( constTermNames != null && (name = (String) constTermNames.get(text)) != null )
				{
					w = Math.max(w, name.length());
				}
			}
			else
			{
				w = Math.max(w, grammar.terminals[i].name.length());
			}
		}

		for ( int i = 0; i < grammar.terminals.length; i++ )
		{
			String name = grammar.terminals[i].name, text = null;
			if ( grammar.terminals[i] instanceof Terminal.Const )
			{
				String tn;
				text = ((Terminal.Const) grammar.terminals[i]).text;
				if ( constTermNames != null && (tn = (String) constTermNames.get(text)) != null )
				{
					name = tn;
				}
			}				
			if ( name.charAt(0) != '#' )
			{
    			out.print("\tpublic static final char ");
    			write(name, out, w);
    			out.print(" = ");
    			writeId(grammar.terminals[i].id, out);
    			out.print(';');
    			
    			if ( text != null )
    			{
    				out.print(" // ");
    				out.print('"');
    				out.print(text);
    				out.print('"');
    			}
    			out.println();
			}
		}
	}

	private void writeCallbacks(PrintWriter out)
	{
		if ( !generateNodeBuilders || !generateListBuilders ) // then we need prototypes
		{
    		//
    		// Find max width of callback return type and name for formatting
    		//
    		int tw = 0, nw = 0;
    		for ( int i = 0; i < callbacks.length; i++ )
    		{
    			Callback cb = callbacks[i]; 
    			if ( cb.isNodeMaker() && !generateListBuilders || cb.isListMaker() && !generateListBuilders )
    			{
    				tw = Math.max(tw, cb.returnType.length());
    				nw = Math.max(nw, cb.name.length());
    			}
    		}
    		//
    		// Write prototypes
    		//
    		for ( int i = 0; i < callbacks.length; i++ )
    		{
    			Callback cb = callbacks[i]; 
    			if ( cb.isNodeMaker() && !generateListBuilders || cb.isListMaker() && !generateListBuilders )
    			{
    				writePrototype(cb, out, tw, nw);
    			}
    		}
		}
		
		if ( generateNodeBuilders )
		{
    		for ( int i = 0; i < callbacks.length; i++ )
    		{
    			if ( callbacks[i].isNodeMaker() )
    			{
    				out.println();
    				writeNodeBuilder(callbacks[i], out);
    			}
    		}
		}
		
		if ( generateListBuilders )
		{
    		//
    		// Write List builders
    		//
    		for ( int i = 0; i < callbacks.length; i++ )
    		{
    			if ( callbacks[i].isListStarter() )
    			{
    				out.println();
    				writeListStarter(callbacks[i], out);
    			}
    			else if ( callbacks[i].isListBuilder() )
    			{
    				out.println();
    				writeListBuilder(callbacks[i], out);
    			}
    		}
		}
	}

	private void writeRulesSwitch(PrintWriter out)
	{
		out.println("\tprotected Symbol reduce(Symbol[] symbols, int at, int ruleNo)");
		out.println("\t{");
		out.println("\t\tswitch (ruleNo)");
		out.println("\t\t{");

		int w = getWidth(grammar.productions.length - 1);
		for ( int i = 0; i < grammar.productions.length; i++ )
		{
			out.print("\t\t\tcase ");
			write(i, out, w);
			out.print(": // ");
			out.println(grammar.productions[i]);
			out.println("\t\t\t{");
			writeSwitchCase(i, out);
			out.println("\t\t\t}");
		}
		out.println("\t\t}");
		out.println("\t\tthrow new IndexOutOfBoundsException(\"unknown production #\" + ruleNo);");
		out.println("\t}");
	}

	private void writeSwitchCase(int n, PrintWriter out)
	{
		Callback cb = callbacks[n];

		if ( cb.isNull() )
		{
			out.println("\t\t\t\treturn symbol( null );");
		}
		else if ( cb.isCopying() )
		{
			out.print("\t\t\t\treturn copy( symbols[at");
			int i = cb.getFirstItemIndex();
			if ( i > 0 )
			{
				out.print(" - ");
				out.print(i);
			}
			out.println("] );");
		}
		else
		{
			int tw = 0, nw = 0;
			for ( int i = 0; i < cb.args.length; i++ )
			{
				Item arg = cb.args[i];
				if ( arg != null )
				{
					tw = Math.max(tw, arg.type.length());
					nw = Math.max(nw, arg.name.length());
				}
			}

			for ( int i = cb.getFirstItemIndex(); i >= 0; i = cb.getNextItemIndex(i) )
			{
				Item arg = cb.args[i];
				out.print("\t\t\t\t");
				write(arg.type, out, tw);
				out.print(' ');
				write(arg.name, out, nw);
				out.print(" = (");
				write(arg.type, out, tw);
				out.print(") symbols[at");
				if ( i > 0 )
				{
					out.print(" - ");
					out.print(i);
				}
				out.println("].getValue();");
			}
			out.println();
			out.print("\t\t\t\treturn symbol ( on");
			out.print(cb.name);
			out.print("(");

			int i = cb.getFirstItemIndex();
			if ( i >= 0 )
			{
				out.print(cb.args[i].name);

				while ( (i = cb.getNextItemIndex(i)) > 0 )
				{
					out.print(", ");
					out.print(cb.args[i].name);
				}
			}
			out.println(") );");
		}
	}

	private void writeParserConstructor(PrintWriter out)
	{
		out.print("\tprotected ");
		out.print(parserName);
		out.println("()");
		out.println("\t{");
		out.println("\t\tsuper(tables);");
		out.println("\t}");
	}

	private void writeParsingTables(PrintWriter out)
	{
		out.println("\tprivate static final ParsingTables tables;");
		out.println();
		out.println("\tstatic {");
		out.println("\t\ttry");
		out.println("\t\t{");
		out.print("\t\t\tDataInputStream input = new DataInputStream(");
		out.print(parserName);
		out.print(".class.getResourceAsStream(\"");
		out.print(parserName);
		out.println(".tables\"));");
		out.println("\t\t\ttry");
		out.println("\t\t\t{");
		out.println("\t\t\t\ttables = new ParsingTables(input);");
		out.println("\t\t\t}");
		out.println("\t\t\tfinally");
		out.println("\t\t\t{");
		out.println("\t\t\t\tinput.close();");
		out.println("\t\t\t}");
		out.println("\t\t}");
		out.println("\t\tcatch (IOException _)");
		out.println("\t\t{");
		out.println("\t\t\tthrow new IllegalStateException(\"cannot load parsing tables\");");
		out.println("\t\t}");
		out.println("\t}");
	}

	private void writeSemanticTypes(File dir, long timeLine) throws IOException
	{
		if ( astPackageName != null )
		{
			dir = new File(dir, astPackageName.replace('.', '/'));
			if ( !dir.exists() )
			{
				dir.mkdirs();
			}
		}
		
		Collection abstractTypes = new HashSet();
		Map listTypes = new HashMap();
		Map nodeTypes = new HashMap();
		initSemanticTypes(abstractTypes, listTypes, nodeTypes);

		long before = timeLine - 4000, after = timeLine;
		
		for ( Iterator i = abstractTypes.iterator(); i.hasNext(); )
		{
			String typeName = (String) i.next();
			File srcFile = new File(dir, typeName + ".java");
			long lastMod = srcFile.lastModified();
			if ( timeLine != 0 && (lastMod < before || after < lastMod) )
			{
				srcFile = new File(srcFile.getPath() + ".new");
			}
			PrintWriter out = new PrintWriter(new FileWriter(srcFile));
			try
			{
				writeAbstractType(typeName, out);
			}
			finally
			{
				out.close();
			}
		}

		for ( Iterator i = listTypes.values().iterator(); i.hasNext(); )
		{
			ListType type = (ListType) i.next();
			File srcFile = new File(dir, type.listType + ".java");
			long lastMod = srcFile.lastModified();
			if ( timeLine != 0 && (lastMod < before || after < lastMod) )
			{
				srcFile = new File(srcFile.getPath() + ".new");
			}
			PrintWriter out = new PrintWriter(new FileWriter(srcFile));
			try
			{
				write(type, out);
			}
			finally
			{
				out.close();
			}
		}

		for ( Iterator i = nodeTypes.values().iterator(); i.hasNext(); )
		{
			NodeType type = (NodeType) i.next();
			File srcFile = new File(dir, type.typeName + ".java");
			long lastMod = srcFile.lastModified();
			if ( timeLine != 0 && (lastMod < before || after < lastMod) )
			{
				srcFile = new File(srcFile.getPath() + ".new");
			}
			PrintWriter out = new PrintWriter(new FileWriter(srcFile));
			try
			{
				write(type, out);
			}
			finally
			{
				out.close();
			}
		}

		for ( Iterator i = termTypes.iterator(); i.hasNext(); )
		{
			String typeName = (String) i.next();

			File srcFile = new File(dir, typeName + ".java");
			long lastMod = srcFile.lastModified();
			if ( timeLine != 0 && (lastMod < before || after < lastMod) )
			{
				srcFile = new File(srcFile.getPath() + ".new");
			}
			PrintWriter out = new PrintWriter(new FileWriter(srcFile));
			try
			{
				writeTermType(typeName, out);
			}
			finally
			{
				out.close();
			}
		}

		writeNodeVisitor(nodeTypes.values(), listTypes.values(), dir, timeLine);
		writeTreeWalker(nodeTypes.values(), listTypes.values(), dir, timeLine);
	}

	private void initSemanticTypes(Collection abstractTypes, Map listTypes, Map nodeTypes)
	{
		Collection concreteTypes = new ArrayList();
		
		for ( int i = 0; i < callbacks.length; i++ )
		{
			Callback cb = callbacks[i];
			if ( cb.isNull() || cb.isCopying() )
				continue;

			if ( cb.isListStarter() )
				//
				// ignore it - we can get all we need from a list builder
				//
				continue;

			if ( cb.isListBuilder() )
			{
				listTypes.put(cb.returnType, new ListType(cb.returnType, cb.args[cb.getLastItemIndex()].type));
			}
			else
			{
				NodeType type = (NodeType) nodeTypes.get(cb.name);
				if ( type == null )
				{
					nodeTypes.put(cb.name, type = new NodeType(cb.name));
					
					if ( cb.name.equals(cb.returnType) )
					{
						concreteTypes.add(cb.name);
					}
					else
					{
						abstractTypes.add(type.superName = cb.returnType);
					}
				}
				type.add(cb);
			}
		}
		abstractTypes.removeAll(concreteTypes);
		
		for ( Iterator i = nodeTypes.values().iterator(); i.hasNext(); )
        {
	        NodeType type = (NodeType) i.next();
	        if ( type.superName != null && !abstractTypes.contains(type.superName) )
	        {
	        	type.superType = (NodeType) nodeTypes.get(type.superName);
	        }
        }
	}
	
	private void writeAstPackage(PrintWriter out)
	{
		if ( astPackageName != null )
		{
			out.print("package ");
			out.print(astPackageName);
			out.println(";");
			out.println();
		}
	}
	

	private void writeAbstractType(String typeName, PrintWriter out)
	{
		writeFileComment(out);
		writeAstPackage(out);
		writeClassComment(out);
		out.print("public abstract class ");
		out.print(typeName);
		out.print(" extends ");
		out.println("beaver.util.Node");
		out.println("{");
		out.println("\tpublic abstract void accept(NodeVisitor visitor);");
		out.println("}");
	}

	private void write(ListType type, PrintWriter out)
	{
		writeFileComment(out);
		writeAstPackage(out);
		writeClassComment(out);
		out.print("public class ");
		out.print(type.listType);
		out.print(" extends ");
		out.println("beaver.util.NodeList");
		out.println("{");
		out.print("\tpublic ");
		out.print(type.listType);
		out.println("()");
		out.println("\t{");
		out.println("\t}");
		out.println();
		out.print("\tpublic ");
		out.print(type.listType);
		out.print(" add(");
		out.print(type.itemType);
		out.println(" item)");
		out.println("\t{");
		out.print("\t\treturn (");
		out.print(type.listType);
		out.println(") super.add(item);");
		out.println("\t}");
		out.println();
		out.println("\tpublic void accept(NodeVisitor visitor)");
		out.println("\t{");
		out.println("\t\tvisitor.visit(this);");
		out.println("\t}");
		out.println("}");
	}

	private void write(NodeType type, PrintWriter out)
	{
		writeFileComment(out);
		writeAstPackage(out);
		writeClassComment(out);
		out.print("public class ");
		out.print(type.typeName);
		out.print(" extends ");
		out.println(type.superName != null ? type.superName : "beaver.util.Node");
		out.println("{");
		writeFields(type, out);
		out.println();
		writeConstructors(type, out);
		out.println("\tpublic void accept(NodeVisitor visitor)");
		out.println("\t{");
		out.println("\t\tvisitor.visit(this);");
		out.println("\t}");
		out.println("}");
	}

	private void writeTermType(String typeName, PrintWriter out)
	{
		writeFileComment(out);
		writeAstPackage(out);
		writeClassComment(out);
		out.print("public class ");
		out.print(typeName);
		out.print(" extends ");
		out.println("beaver.util.Node");
		out.println("{");
		out.println("\tpublic String text;");
		out.println();
		out.print("\tpublic ");
		out.print(typeName);
		out.println("(String text)");
		out.println("\t{");
		out.println("\t\tthis.text = text;");
		out.println("\t}");
		out.println();
		out.println("\tpublic String toString()");
		out.println("\t{");
		out.println("\t\treturn text;");
		out.println("\t}");
		out.println();
		out.println("\tpublic void accept(NodeVisitor visitor)");
		out.println("\t{");
		out.println("\t\tvisitor.visit(this);");
		out.println("\t}");
		out.println("}");
	}

	private void writeNodeVisitor(Collection nodeTypes, Collection listTypes, File dir, long timeLine) throws IOException
	{
		File srcFile = new File(dir, "NodeVisitor" + ".java");
		long lastMod = srcFile.lastModified();
		if ( timeLine != 0 && (lastMod < timeLine - 4000 || timeLine < lastMod) )
		{
			srcFile = new File(srcFile.getPath() + ".new");
		}
		PrintWriter out = new PrintWriter(new FileWriter(srcFile));
		try
		{
			writeNodeVisitor(nodeTypes, listTypes, out);
		}
		finally
		{
			out.close();
		}
	}

	private void writeNodeVisitor(Collection nodeTypes, Collection listTypes, PrintWriter out)
	{
		writeFileComment(out);
		writeAstPackage(out);
		writeClassComment(out);
		out.print("public interface ");
		out.println("NodeVisitor");
		out.println("{");
		int w = getMaxTypeNameLength(nodeTypes, listTypes, termTypes);

		List types = new ArrayList(nodeTypes.size() + listTypes.size() + termTypes.size());
		for ( Iterator i = nodeTypes.iterator(); i.hasNext(); )
		{
			types.add(((NodeType) i.next()).typeName);
		}
		for ( Iterator i = listTypes.iterator(); i.hasNext(); )
		{
			types.add(((ListType) i.next()).listType);
		}
		types.addAll(termTypes);
		Collections.sort(types);

		for ( Iterator i = types.iterator(); i.hasNext(); )
		{
			writeNodeVisitorMethod(i.next().toString(), w, out);
		}
		out.println("}");
	}

	private void writeTreeWalker(Collection nodeTypes, Collection listTypes, File dir, long timeLine) throws IOException
	{
		File srcFile = new File(dir, "TreeWalker" + ".java");
		long lastMod = srcFile.lastModified();
		if ( timeLine != 0 && (lastMod < timeLine - 4000 || timeLine < lastMod) )
		{
			srcFile = new File(srcFile.getPath() + ".new");
		}
		PrintWriter out = new PrintWriter(new FileWriter(srcFile));
		try
		{
			writeTreeWalker(nodeTypes, listTypes, out);
		}
		finally
		{
			out.close();
		}
	}

	private void writeTreeWalker(Collection nodeTypes, Collection listTypes, PrintWriter out)
	{
		writeFileComment(out);
		writeAstPackage(out);
		writeClassComment(out);
		out.print("public class ");
		out.print("TreeWalker");
		out.print(" implements ");
		out.println("NodeVisitor");
		out.println("{");
		out.println();

		for ( Iterator i = nodeTypes.iterator(); i.hasNext(); )
		{
			NodeType type = (NodeType) i.next();
			out.print("\tpublic void visit(");
			out.print(type.typeName);
			out.println(" node)");
			out.println("\t{");
			Collection fields = getFields(type.constructors);
			int w = getMaxItemNameLength(fields);
			for ( Iterator x = fields.iterator(); x.hasNext(); )
			{
				Item item = (Item) x.next();
				out.print("\t\t");
				out.print("if ( node.");
				write(item.name, out, w);
				out.print(" != null ) ");
				out.print("node.");
				write(item.name, out, w);
				out.println(" .accept(this);");
			}
			out.println("\t}");
			out.println();
		}
		for ( Iterator i = listTypes.iterator(); i.hasNext(); )
		{
			ListType list = (ListType) i.next();
			out.print("\tpublic void visit(");
			out.print(list.listType);
			out.println(" list)");
			out.println("\t{");
			out.print("\t\tfor ( ");
			out.print(list.itemType);
			out.print(" item = (");
			out.print(list.itemType);
			out.print(") list.first(); item != null; item = (");
			out.print(list.itemType);
			out.println(") item.next() )");
			out.println("\t\t{");
			out.println("\t\t\titem.accept(this);");
			out.println("\t\t}");
			out.println("\t}");
			out.println();
		}
		for ( Iterator i = termTypes.iterator(); i.hasNext(); )
		{
			String typeName = (String) i.next();

			out.print("\tpublic void visit(");
			out.print(typeName);
			out.println(" node)");
			out.println("\t{");
			out.println("\t\t// leaf node");
			out.println("\t}");
			out.println();
		}

		out.println("}");
	}
	
	private static int getMaxItemNameLength(Collection items)
	{
		int s = 0;
		for ( Iterator i = items.iterator(); i.hasNext(); )
        {
	        Item item = (Item) i.next();
	        s = Math.max(s, item.name.length());
        }
		return s;
	}

	private static int getMaxTypeNameLength(Collection nodeTypes, Collection listTypes, Collection termTypes)
	{
		int l = 0;
		for ( Iterator i = nodeTypes.iterator(); i.hasNext(); )
		{
			NodeType type = (NodeType) i.next();
			l = Math.max(l, type.typeName.length());
		}
		for ( Iterator i = listTypes.iterator(); i.hasNext(); )
		{
			ListType type = (ListType) i.next();
			l = Math.max(l, type.listType.length());
		}
		for ( Iterator i = termTypes.iterator(); i.hasNext(); )
		{
			String typeName = (String) i.next();
			l = Math.max(l, typeName.length());
		}
		return l;
	}

	private static void writeNodeVisitorMethod(String typeName, int maxTypeNameLength, PrintWriter out)
	{
		out.print("\tvoid visit(");
		write(typeName, out, maxTypeNameLength);
		out.println(" node);");
	}

	private static void writeConstructors(NodeType type, PrintWriter out)
	{
		for ( Iterator i = type.constructors.iterator(); i.hasNext(); )
		{
			Callback cb = (Callback) i.next();

			out.print("\tpublic ");
			out.print(type.typeName);
			out.print('(');
			String sep = "";
			for ( int x = 0; x < cb.args.length; x++ )
			{
				if ( cb.args[x] != null )
				{
					out.print(sep);
					write(cb.args[x], out);
					sep = ", ";
				}
			}
			out.println(')');
			out.println("\t{");
			int x = cb.getFirstItemIndex();
			if ( type.superType != null )
			{
				Callback ctr = type.superType.findSuperConstructor(cb);
				if ( ctr == null )
					throw new IllegalStateException("cannot find super-constructor");
				
				out.print("\t\tsuper(");
				out.print(cb.args[x].name);
				x = cb.getNextItemIndex(x);
				for ( int n = 1; n < ctr.argc; n++ )
				{
					out.print(", ");
					out.print(cb.args[x].name);
					x = cb.getNextItemIndex(x);
				}
				out.println(");");
			}
			
			int nw = 0;
			for ( int y = x; y >= 0; y = cb.getNextItemIndex(y) )
			{
				nw = Math.max(nw, cb.args[y].name.length());
			}
			while ( x >= 0 )
			{
				out.print("\t\tthis.");
				write(cb.args[x].name, out, nw);
				out.print(" = ");
				out.print(cb.args[x].name);
				out.println(';');
				x = cb.getNextItemIndex(x);
			}
			out.println("\t}");
			out.println();
		}
	}

	private static void writeFields(NodeType type, PrintWriter out)
	{
		int tw = 0;
		Collection fields = getFields(type.constructors);
		if ( type.superType != null )
		{
			Collection superFields = getFields(type.superType.constructors);
			fields.removeAll(superFields);
		}
		for ( Iterator i = fields.iterator(); i.hasNext(); )
		{
			Item field = (Item) i.next();
			tw = Math.max(tw, field.type.length());
		}
		for ( Iterator i = fields.iterator(); i.hasNext(); )
		{
			Item field = (Item) i.next();

			out.print("\tpublic ");
			write(field.type, out, tw);
			out.print(' ');
			out.print(field.name.toString());
			out.println(';');
		}
	}

	private static Collection getFields(Collection constructors)
	{
		List orderedConstructors = new ArrayList(constructors);
		Collections.sort(orderedConstructors, new Comparator()
		{
            public int compare(Object o1, Object o2)
            {
	            return ((Callback) o2).argc - ((Callback) o1).argc;
            }
		});
		Collection fields = new ArrayList();
		for ( Iterator i = orderedConstructors.iterator(); i.hasNext(); )
        {
	        Callback cb = (Callback) i.next();
			for ( int x = 0; x < cb.args.length; x++ )
			{
				if ( cb.args[x] != null && !fields.contains(cb.args[x]))
				{
					fields.add(cb.args[x]);
				}
			}
        }
		return fields;
	}

	private static void writePrototype(Callback cb, PrintWriter out, int tw, int nw)
	{
		out.print("\tprotected abstract ");
		write(cb.returnType, out, tw);
		out.print("  on");
		write(cb.name, out, nw);
		out.print(" (");
		writeArgs(cb, out);
		out.println(");");
	}

	private static void writeListStarter(Callback cb, PrintWriter out)
	{
		writeDecl(cb, out);
		out.println("\t{");

		out.print("\t\treturn new ");
		out.print(cb.returnType);
		out.print("().add(");
		out.print(cb.args[cb.getFirstItemIndex()].name);
		out.println(");");

		out.println("\t}");
	}

	private static void writeListBuilder(Callback cb, PrintWriter out)
	{
		writeDecl(cb, out);
		out.println("\t{");

		int i = cb.getFirstItemIndex();
		out.print("\t\treturn ");
		out.print(cb.args[i].name);
		out.print(".add(");
		out.print(cb.args[cb.getNextItemIndex(i)].name);
		out.println(");");

		out.println("\t}");
	}
	
	private static void writeNodeBuilder(Callback cb, PrintWriter out)
	{
		writeDecl(cb, out);
		out.println("\t{");
		out.print("\t\treturn new ");
		out.print(cb.name);
		out.print("(");
		int i = cb.getFirstItemIndex();
		if ( i >= 0 )
		{
			out.print(cb.args[i].name);

			while ( (i = cb.getNextItemIndex(i)) > 0 )
			{
				out.print(", ");
				out.print(cb.args[i].name);
			}
		}
		out.println(");");
		out.println("\t}");
	}

	private static void writeDecl(Callback cb, PrintWriter out)
	{
		out.print("\tprotected ");
		out.print(cb.returnType);
		out.print(" on");
		out.print(cb.name);
		out.print("(");
		writeArgs(cb, out);
		out.println(")");
	}

	private static void writeArgs(Callback cb, PrintWriter out)
	{
		int i = cb.getFirstItemIndex();
		if ( i >= 0 )
		{
			write(cb.args[i], out);

			while ( (i = cb.getNextItemIndex(i)) > 0 )
			{
				out.print(", ");
				write(cb.args[i], out);
			}
		}
	}

	private static void write(Item i, PrintWriter out)
	{
		out.print(i.type);
		out.print(' ');
		out.print(i.name);
	}

	private static int getWidth(int n)
	{
		return n < 10 ? 1 : n < 100 ? 2 : n < 1000 ? 3 : n < 10000 ? 4 : n < 100000 ? 5 : 10;
	}

	private static void write(int number, PrintWriter out, int width)
	{
		for ( int p = width - getWidth(number); p > 0; p-- )
		{
			out.print(' ');
		}
		out.print(number);
	}

	private static void write(String text, PrintWriter out, int width)
	{
		out.print(text);
		for ( int n = width - text.length(); n > 0; n-- )
		{
			out.print(' ');
		}
	}

	private static void writeHex(int n, PrintWriter out)
	{
		if ( n < 10 )
		{
			out.write('0' + n);
		}
		else
		{
			out.write('a' + n - 10);
		}
	}

	private static void writeId(char c, PrintWriter out)
	{
		out.print("'\\");
		if ( c <= 255 )
		{
			out.write('0' + ((c >> 6) & 0x7));
			out.write('0' + ((c >> 3) & 0x7));
			out.write('0' + (c & 0x7));
		}
		else
		{
			out.print('u');
			writeHex((c >> 12) & 0xf, out);
			writeHex((c >> 8) & 0xf, out);
			writeHex((c >> 4) & 0xf, out);
			writeHex(c & 0xf, out);
		}
		out.print('\'');
	}

	private static Map initTypes(Map explicitTypes, String defaultTermType, Grammar grammar)
	{
		Map symbolTypes = explicitTypes == null ? new HashMap() : new HashMap(explicitTypes);
		//
		// Add a "type" for a collection of optional symbol names
		//
		Collection optSymbols = new HashSet();
		symbolTypes.put("...", optSymbols);
		//
		// And a "type" for a map of Terminal types
		//
		Collection termTypes = new HashSet();
		symbolTypes.put(":::", termTypes);

		for ( int i = 0; i < grammar.terminals.length; i++ )
		{
			if ( grammar.terminals[i] instanceof Terminal.Const )
				continue;
			String termType = (String) symbolTypes.get(grammar.terminals[i].name);
			if ( termType == null )
			{
				symbolTypes.put(grammar.terminals[i].name, termType = defaultTermType);
			}
			termTypes.add(termType);
		}

		for ( int i = 0; i < grammar.nonterminals.length; i++ )
		{
			Symbol opt = getOptSymbol(grammar.nonterminals[i]);
			if ( opt != null )
			{
				optSymbols.add(grammar.nonterminals[i].name);

				String exp = (String) symbolTypes.get(grammar.nonterminals[i].name);
				if ( exp != null )
				{
					System.out.println("Warning: ignoring explicit type " + exp + " for " + grammar.nonterminals[i].name + " - a non-terminal for optional " + opt.name);
				}
				String type = (String) symbolTypes.get(opt.name);
				if ( type == null )
				{
					symbolTypes.put(opt.name, type = deriveTypeName(opt));
				}
				symbolTypes.put(grammar.nonterminals[i].name, type);
			}
			else if ( !symbolTypes.containsKey(grammar.nonterminals[i].name) )
			{
				symbolTypes.put(grammar.nonterminals[i].name, deriveTypeName(grammar.nonterminals[i]));
			}
		}
		return symbolTypes;
	}

	private static String deriveTypeName(Symbol sym)
	{
		String name = sym.name;
		char firstChar = name.charAt(0);
		return Character.isUpperCase(firstChar) ? name : Character.toUpperCase(firstChar) + name.substring(1);
	}

	private static Symbol getOptSymbol(NonTerminal nt)
	{
		Production[] rules = nt.derivationRules;

		if ( rules == null || rules.length != 2 )
			return null;

		int i = 0;
		if ( rules[i].rhs.length != 0 && rules[++i].rhs.length != 0 )
			return null;

		Production.RHSItem[] rhs = rules[i ^ 1].rhs;
		i = 0;
		while ( i < rhs.length && rhs[i].symbol instanceof Terminal.Const )
			i++;
		if ( i == rhs.length )
			return null;

		Symbol opt = rhs[i].symbol;

		while ( ++i < rhs.length && rhs[i].symbol instanceof Terminal.Const )
			continue;

		return i < rhs.length ? null : opt;
	}

	private static Callback[] createCalbacks(Grammar grammar, Map symbolTypes, Collection optSymbolNames)
	{
		Callback[] callbacks = new Callback[grammar.productions.length];

		for ( int i = 0; i < grammar.productions.length; i++ )
		{
			callbacks[i] = new Callback(grammar.productions[i], symbolTypes, optSymbolNames);
		}
		Callback.markCopying(callbacks);
		Callback.markListBuilders(callbacks, grammar.nonterminals);

		return callbacks;
	}

	static class Item
	{
		String  type;
		String  name;
		boolean isOptional;

		Item(String type, String name, boolean isOpt)
		{
			this.type = type;
			this.name = name;
			this.isOptional = isOpt;
		}
		
		public boolean equals(Object o)
		{
			if ( o instanceof Item )
			{
				Item i = (Item) o;
				
				return type.equals(i.type) && name.equals(i.name) && isOptional == i.isOptional;
			}
			return false;
		}
	}

	static class NodeType
	{
		String     typeName;
		String     superName;
		NodeType   superType;
		Collection constructors;

		NodeType(String name)
		{
			this.typeName = name;
			constructors = new ArrayList();
		}

		void add(Callback cb)
		{
			constructors.add(cb);
		}
		
		Callback findSuperConstructor(Callback cb)
		{
			Callback ctr = null;
			for ( Iterator i = constructors.iterator(); i.hasNext(); )
            {
	            Callback c = (Callback) i.next();
	            if ( c.argc <= cb.argc )
	            {
	            	int j = c.getFirstItemIndex(), k = cb.getFirstItemIndex();
	            	while ( j >= 0 && k >= 0 && c.args[j].equals(cb.args[k]) )
	            	{
	            		j = c.getNextItemIndex(j);
	            		k = cb.getNextItemIndex(k);
	            	}
	            	if ( j < 0 )
	            	{
	            		ctr = c;
	            	}
	            }
            }
			return ctr;
		}
	}

	static class ListType
	{
		String listType;
		String itemType;

		ListType(String name, String item)
		{
			this.listType = name;
			this.itemType = item;
		}
	}

	static class Callback
	{
		String returnType;
		String name;
		Item[] args;
		int    argc;
		int    marker;

		Callback(Production rule, Map symbolTypes, Collection optSymbolNames)
		{
			this.returnType = (String) symbolTypes.get(rule.lhs.name);
			this.name = rule.name;
			this.args = new Item[rule.rhs.length];
			for ( int i = 0; i < rule.rhs.length; i++ )
			{
				if ( rule.rhs[i].symbol instanceof Terminal.Const )
					continue;

				String argType = (String) symbolTypes.get(rule.rhs[i].symbol.name);
				String argName = getArgName(rule.rhs, i);
				this.args[i] = new Item(argType, argName, optSymbolNames.contains(rule.rhs[i].symbol.name));
				this.argc++;
			}
		}

		boolean isNull()
		{
			return argc == 0;
		}

		boolean isCopying()
		{
			return marker == COPYING;
		}

		boolean isListStarter()
		{
			return marker == LIST_START;
		}

		boolean isListBuilder()
		{
			return marker == LIST_BUILD;
		}

		boolean isSpecial()
		{
			return argc == 0 || marker != 0;
		}
		
		boolean isNodeMaker()
		{
			return argc > 0 && marker == 0;
		}
		
		boolean isListMaker()
		{
			return marker == LIST_START || marker == LIST_BUILD; 
		}

		int getNextItemIndex(int i)
		{
			while ( ++i < args.length && args[i] == null )
				continue;
			return i < args.length ? i : -1;
		}

		int getFirstItemIndex()
		{
			return getNextItemIndex(-1);
		}

		int getLastItemIndex()
		{
			for ( int i = args.length - 1; i >= 0; i-- )
			{
				if ( args[i] != null )
					return i;
			}
			return -1;
		}

		private String getArgName(Production.RHSItem[] rhs, int argIndex)
		{
			String argName = rhs[argIndex].name;
			if ( argName == null )
			{
				if ( rhs[argIndex].symbol instanceof Terminal )
				{
					argName = rhs[argIndex].symbol.name.toLowerCase();
				}
				else
				{
					Symbol sym = getOptionalSymbol((NonTerminal) rhs[argIndex].symbol);
					argName = makeArgName(sym != null ? sym.name : rhs[argIndex].symbol.name);
				}
			}
			//
			// check whether args, that are already defined, use the same name
			//
			String base = argName;
			int suffix = 2;
			for ( int i = 0; i < argIndex; i++ )
			{
				if ( args[i] != null && args[i].name.equals(name) )
				{
					//
					// change the name and keep searching for collisions with an
					// updated name
					//
					argName = base + suffix++;
				}
			}
			return argName;
		}

		private static int COPYING    = 1;
		private static int LIST_START = 2;
		private static int LIST_BUILD = 3;

		private static String makeArgName(String symName)
		{
			if ( Character.isLowerCase(symName.charAt(0)) )
			{
				return symName;
			}
			char[] name = symName.toCharArray();
			name[0] = Character.toLowerCase(name[0]);
			for ( int i = 1; i < name.length && Character.isUpperCase(name[i]); i++ )
			{
				name[i] = Character.toLowerCase(name[i]);
			}
			return new String(name);
		}

		private static Symbol getOptionalSymbol(NonTerminal nt)
		{
			if ( nt.derivationRules.length != 2 )
				return null;

			int i = 0;
			if ( nt.derivationRules[i].rhs.length != 0 && nt.derivationRules[++i].rhs.length != 0 )
				return null;

			Symbol sym = null;
			Production.RHSItem[] rhs = nt.derivationRules[i ^ 1].rhs;
			for ( i = 0; i < rhs.length; i++ )
			{
				if ( rhs[i].symbol instanceof Terminal.Const )
					continue;
				if ( sym != null )
					return null;
				sym = rhs[i].symbol;
			}
			return sym;
		}

		static void markListBuilders(Callback[] callbacks, NonTerminal[] nonterminals)
		{
			for ( int i = 0; i < nonterminals.length; i++ )
			{
				Production[] ntDerivationRules = nonterminals[i].derivationRules;
				if ( ntDerivationRules.length == 2 )
				{
					Callback startList = callbacks[ntDerivationRules[0].id];
					Callback buildList = callbacks[ntDerivationRules[1].id];

					if ( startList.argc == 2 && buildList.argc == 1 )
					{
						Callback cb = startList;
						startList = buildList;
						buildList = cb;
					}
					else if ( startList.argc != 1 || buildList.argc != 2 )
					{
						continue;
					}

					int x = buildList.getFirstItemIndex();
					if ( buildList.args[x].type.equals(buildList.returnType) )
					{
						x = buildList.getNextItemIndex(x);
						if ( buildList.args[x].type.equals(startList.args[startList.getFirstItemIndex()].type) )
						{
							startList.marker = LIST_START;
							buildList.marker = LIST_BUILD;
						}
					}
				}
			}
		}

		static void markCopying(Callback[] callbacks)
		{
			for ( int i = 0; i < callbacks.length; i++ )
			{
				Callback cb = callbacks[i];
				if ( cb.argc == 1 && cb.args[cb.getFirstItemIndex()].type.equals(cb.returnType) )
				{
					cb.marker = COPYING;
				}
			}
		}
	}
}
