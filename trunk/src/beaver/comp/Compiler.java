/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import beaver.Location;
import beaver.SyntaxErrorException;
import beaver.comp.ast.ParserSpec;
import beaver.comp.ast.Spec;
import beaver.comp.lexer.CharScannerClassWriter;
import beaver.comp.lexer.DFA;
import beaver.comp.lexer.RegExp;
import beaver.comp.lexer.ScannerBuilder;
import beaver.comp.parser.Action;
import beaver.comp.parser.Grammar;
import beaver.comp.parser.ParserWriter;
import beaver.comp.parser.ParsingTables;
import beaver.comp.parser.State;
import beaver.comp.parser.Terminal;
import beaver.comp.SpecScanner;
import beaver.util.BitSet;

/**
 * @author Alexander Demenchuk
 *
 */
public class Compiler
{
	private Log        log;
	private File       srcDir;
	private File       binDir;

	private boolean    makeTypes;
	private boolean    makeCallbacks;

	private String     parserClassName;
	private String     scannerClassName;
	private String[]   srcFileCommentLines;

	private String[]   terminalsPrecedence;
	private Map        constTermNames;
	private Terminal[] terminals;
	private Map        astTypes;
	private String     astTermType;
	private String     astPackageName;
	
	public Compiler(Log log)
	{
		this.log = log;
	}
	
	File configure(String[] args)
	{
		String specFileName = null;
		String propFileName = null;
		for (int i = 0; i < args.length; i++ )
		{
			String arg = args[i];
			if ( arg.length() >= 2 && arg.charAt(0) == '-' )
			{
				switch ( arg.charAt(1) )
				{
					case 'd':
					{
						if ( arg.length() != 3 )
						{
    						throw new IllegalArgumentException("unknown option " + arg);
						}
						switch ( arg.charAt(2) )
						{
							case 's':
							{
								if ( ++i == args.length)
								{
									throw new IllegalArgumentException(arg + " requires a directory name");
								}
								srcDir = new File(args[i]);
								if ( !srcDir.exists() && !srcDir.mkdirs() )
								{
									throw new IllegalStateException(args[i] + " cannot be created");
								}
								break;
							}
							case 'b':
							{
								if ( ++i == args.length)
								{
									throw new IllegalArgumentException(arg + " requires a directory name");
								}
								binDir = new File(args[i]);
								if ( !binDir.exists() && !binDir.mkdirs() )
								{
									throw new IllegalStateException(args[i] + " cannot be created");
								}
								break;
							}
	    					default:
	    					{
	    						throw new IllegalArgumentException("unknown option " + arg);
	    					}
						}
						break;
					}
					case 't':
					{
						makeTypes = true;
						break;
					}
					case 'm':
					{
						makeCallbacks = true;
						break;
					}
					case 'p':
					{
						if ( ++i == args.length)
						{
							throw new IllegalArgumentException(arg + " requires a directory name");
						}
						propFileName = args[i];
						break;
					}
					default:
					{
						throw new IllegalArgumentException("unknown option " + arg);
					}
				}
			}
			else
			{
				if ( specFileName != null )
					throw new IllegalStateException("specification file name is already configured as " + specFileName);
				specFileName = arg;
			}
		}
		
		if ( specFileName == null )
		{
			throw new IllegalStateException("specification file name is not provided");
		}
		
		File pf;
		if ( propFileName != null )
		{
			pf = new File(propFileName);
			if ( !pf.canRead() )
			{
				throw new IllegalArgumentException("properties file - " + propFileName + " - is unreadable");
			}
		}
		else
		{
			int dot = specFileName.lastIndexOf('.');
			propFileName = (dot < 0 ? specFileName : specFileName.substring(0, dot)) + ".properties";  
			pf = new File(propFileName);
		}
		
		if ( pf.canRead() )
		{
			Properties pl = new Properties();
			try
			{
				pl.load(new FileReader(pf));
			}
			catch (IOException e)
			{
				throw new IllegalArgumentException("failed to read properties from " + propFileName + " -- " + e.getMessage());
			}
			this.configure(pl);
		}
		
		File srcFile = new File(specFileName);
		if ( !srcFile.canRead() )
		{
			throw new IllegalStateException("specification file - " + specFileName + " - is unreadable");
		}
		return srcFile;
	}
	
	void configure(Properties p)
	{
		String lang = p.getProperty("language");
		if ( ! lang.equals("Java") )
			throw new IllegalArgumentException("unsupported language -- " + lang);
		
		parserClassName = p.getProperty("parser.class");
		scannerClassName = p.getProperty("scanner.class");
		
		Collection terms = new ArrayList();
		terms.add(Terminal.EOF);
		
		if ( scannerClassName == null )
		{
			String pv = p.getProperty("terminal.expr");
			if ( pv != null )
			{
				terminalsPrecedence = split(pv, ',');
			}
		}
		String pv = p.getProperty("terminal.const");
		if ( pv != null )
		{
			String[] cterms = split(pv, ',');
			constTermNames = new HashMap();
			for ( int i = 0; i < cterms.length; i++ )
            {
				String name = cterms[i];
				pv = p.getProperty("terminal.const." + name);
				if ( pv != null )
				{
					String text = pv.trim();
	                constTermNames.put(text, name);
	                terms.add(new Terminal.Const((char) terms.size(), name, text));
				}
            }
			if ( terminalsPrecedence != null )
			{
    			for ( int i = 0; i < terminalsPrecedence.length; i++ )
                {
                    terms.add(new Terminal((char) terms.size(), terminalsPrecedence[i]));
                }
			}
		}
		terminals = (Terminal[]) terms.toArray(new Terminal[terms.size()]);
		
		pv = p.getProperty("srcFileComment");
		if ( pv != null )
		{
			srcFileCommentLines = split(pv, '$');
		}
		
		pv = p.getProperty("ast.types");
		if ( pv != null )
		{
			String[] astTypeNames = split(pv, ',');
			astTypes = new HashMap();
			for ( int i = 0; i < astTypeNames.length; i++ )
            {
				String name = astTypeNames[i];
				pv = p.getProperty("ast.types." + name);
				if ( pv != null )
				{
	                astTypes.put(name, pv.trim());
				}
            }
		}
		astTermType = p.getProperty("ast.term.type");
		astPackageName = p.getProperty("ast.package");
	}
	
	public void compile(File specFile) throws IOException, SyntaxErrorException, CompilationException
	{
	    Spec spec = (Spec) new AstBuilder().parse(new SpecScanner(new FileReader(specFile)));

	    Map tokenRules = null;
	    if ( spec.scannerSpec != null )
	    {
	    	Map macros = new MacroCompiler().compile(spec.scannerSpec);
	    	TokenCompiler tc = new TokenCompiler(macros);
	    	tokenRules = tc.compile(spec.scannerSpec);
	    	terminalsPrecedence = tc.getTerminalNames();
	    }
	    if ( spec.parserSpec != null )
	    {
	    	Grammar grammar = compileGrammar(spec.parserSpec);
	    	terminals = grammar.getTerminals();
	    	State firstState = compileAutomaton(grammar);
	    	
    		generateParsingTables(grammar, firstState);

    		int lastDot = parserClassName.lastIndexOf('.');
    		String className = lastDot < 0 ? parserClassName : parserClassName.substring(lastDot + 1); 

    		
    		ParserWriter sourceWriter = new ParserWriter(className, grammar, astTermType, astTypes);
    		if ( lastDot > 0 )
    		{    		
    			sourceWriter.setParserPackageName(parserClassName.substring(0, lastDot));
    		}
			sourceWriter.setAstPackageName(astPackageName);
    		sourceWriter.setFileComment(srcFileCommentLines);
    		sourceWriter.setConstTermNames(constTermNames);
    		sourceWriter.setGenerateListBuilders(makeCallbacks);
    		sourceWriter.setGenerateNodeBuilders(makeCallbacks);
    		sourceWriter.writeParserSource(srcDir);
    		if ( makeTypes )
    		{
    			sourceWriter.writeSemanticTypes(srcDir);
    		}
	    }
	    if ( tokenRules != null )
	    {
	    	String className = scannerClassName.replace('.', '/');
	    	DFA[] automata = compileScanner(tokenRules);
			byte[] bc = CharScannerClassWriter.compile(automata, className);
			ScannerBuilder.saveClass(binDir, className, bc);
	    	
	    	if ( !tokenRules.isEmpty() )
	    	{
	    		String unusedTokens = "";
    			String sep = "";
    			for ( Iterator i = tokenRules.keySet().iterator(); i.hasNext(); sep = ", " )
                {
	                unusedTokens = unusedTokens + sep + i.next();
                }
	    		log.warning("The following terminals are not required for the parser: " + unusedTokens + ". They are ignored.");
	    	}
	    }
	}

	void generateParsingTables(Grammar grammar, State firstState) throws IOException
    {
	    File outFile = new File(binDir, parserClassName.replace('.', '/') + ".tables");
	    File destDir = outFile.getParentFile();
	    if ( destDir != null && !destDir.exists() )
	    {
	    	destDir.mkdirs();
	    }
		DataOutputStream out = new DataOutputStream(new FileOutputStream(outFile));
		try
		{
			new ParsingTables(grammar, firstState).writeTo(out);
		}
		finally
		{
			out.close();
		}
    }
	
	DFA[] compileScanner(Map tokens) throws IOException
	{
		Collection rules = new ArrayList(terminals.length + 3);
		Terminal t;
		int i = 0;
		while ( ++i < terminals.length && (t = terminals[i]) instanceof Terminal.Const )
        {
			rules.add( ScannerBuilder.makeMatchStringRule(t.getId(), t.toString()) ); 
        }
		while ( i < terminals.length )
		{
			t = terminals[i++];
			
			RegExp.RuleOp rule = (RegExp.RuleOp) tokens.remove(t.toString());
			if ( rule == null )
			{
				log.error("Terminal " + t + " is undefined.");
			}
			else
			{
				rule.setId(t.getId());
				rules.add( rule );
			}
		}
		rules.add( ScannerBuilder.makeEndOfLineRule() );
		rules.add( ScannerBuilder.makeEndOfFileRule() );
		rules.add( ScannerBuilder.makeRule(-3, new RegExp.CloseOp(ScannerBuilder.rangeToRegExp("[ \t]"))) );
		
		DFA dfa = ScannerBuilder.compile(rules);
		
		return new DFA[] { dfa };
	}
	
    private Grammar compileGrammar(ParserSpec parserSpec) throws IOException, SyntaxErrorException
    {
		parserSpec.accept(new InlineRulesExtractor());
		parserSpec.accept(new EbnfOperatorCompiler());
		parserSpec.accept(new InlineStringExractor());
		
		NonTerminalCollector nontermCollector = new NonTerminalCollector(log);
		parserSpec.accept(nontermCollector);
		Set nonterminals = nontermCollector.getNames();
		/*
		 * Inject the 'error' nonterminal without a definition
		 */
		nonterminals.add("error");
		
		UnreferencedNonTerminalFinder unrefNontermFinder = new UnreferencedNonTerminalFinder(nonterminals);
		parserSpec.accept(unrefNontermFinder);
		Set unreferencedNames = unrefNontermFinder.getSymbolNames();
		
		if ( unreferencedNames.remove("error") ) // because there is no rule for it to remove
		{
			nonterminals.remove("error");
		}
		parserSpec.accept(new UnusedRuleRemover(unreferencedNames, log));
		nonterminals.removeAll(unreferencedNames);
		
		TerminalCollector termCollector = new TerminalCollector(nonterminals);
		parserSpec.accept(termCollector);
		Map constTokens = termCollector.getConstTokens();
		
		parserSpec.accept(new InlineTokenReplacer(constTokens));
		
		GrammarBuilder grammarBuilder = new GrammarBuilder(constTokens, termCollector.getNamedTokens(), terminalsPrecedence, nonterminals, log);
		parserSpec.accept(grammarBuilder);
		return grammarBuilder.getGrammar();
    }

	private State compileAutomaton(Grammar grammar) throws CompilationException
    {
	    State firstState = new State.Builder().createStates(grammar);
		
		Action.ConflictResolver conflictResolver = new Action.ConflictResolver(log);
		if ( !conflictResolver.resolveConflicts(firstState) )
		{
			throw new CompilationException("conflicts");
		}
		BitSet unreducibleProductions = grammar.findUnreducibleProductions(firstState);
		if ( unreducibleProductions.size() != 0 )
		{
			throw new CompilationException("grammar has unreducible productions");
		}
		new Action.Compressor(grammar).compress(firstState);
	    return firstState;
    }

	private static String[] split(String str, char c)
	{
		Collection parts = new ArrayList();		
		int b = 0;
		for ( int e = str.indexOf(c); e > 0; b = e + 1, e = str.indexOf(c, b) ) 
		{
			parts.add( str.substring(b, e).trim() );
		}
		parts.add( str.substring(b).trim() );
		
		return (String[]) parts.toArray(new String[parts.size()]);
	}
	
	private static void printUsageInstructions()
	{
		System.err.println("Usage: java -jar beaver.jar [options] language_specification_file.name");
		System.err.println("where options include:");
		System.err.println("\t-ds dir  destination directory for generated source file");
		System.err.println("\t-db dir  destination directory for binary/class files");  
		System.err.println("\t-t       generate implementations of semantic types");
		System.err.println("\t-m       generate implementations of AST building methods");
		System.err.println("\t-p file  code generation properties");
	}
	
	public static void main(String[] args)
	{	
		Compiler comp = new Compiler(new StdoutLog());
		//
		// Parse the command line:
		//
		try
		{
			File src = comp.configure(args);
			comp.compile(src);
		}
		catch (IllegalArgumentException e)
		{
			System.err.println("Error: " + e.getMessage());
			printUsageInstructions();
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	static class StdoutLog implements Log
	{
		static final PrintStream out = System.out; 
		static final Where loc = new Where();
		
		public void error(Location where, String descr)
        {
	        where.copyLocation(loc);

	        out.print("Error: ");
	        loc.writeTo(out);
	        out.print(' ');
	        out.println(descr);
        }

		public void error(String descr)
        {
	        out.print("Error: ");
	        out.println(descr);
        }

		public void information(String descr)
        {
	        out.println(descr);
        }

		public void warning(Location where, String descr)
        {
	        where.copyLocation(loc);

	        out.print("Warning: ");
	        loc.writeTo(out);
	        out.print(' ');
	        out.println(descr);
        }

		public void warning(String descr)
        {
	        out.print("Warning: ");
	        out.println(descr);
        }
		
		static class Where implements Location
		{
			int slin, elin, scol, ecol;

			public void copyLocation(Location dest)
            {
				dest.setLocation(slin, scol, elin, ecol);
            }

			public void setLocation(int line, int column, int endLine, int endColumn)
            {
				slin = line;
				scol = column;
				elin = endLine;
				ecol = endColumn;
            }
			
			void writeTo(PrintStream out)
			{
				out.print('[');
				out.print(slin);
				out.print(',');
				out.print(scol);
				out.print('-');
				out.print(elin);
				out.print(',');
				out.print(ecol);
				out.print(']');
			}
		}
	}
}
