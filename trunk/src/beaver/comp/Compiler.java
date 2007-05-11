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
import beaver.comp.cst.ParserSpec;
import beaver.comp.cst.ScannerState;
import beaver.comp.cst.Spec;
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
	private Terminal[] constTerms;
	private Terminal[] regexTerms;
	private Map        cstTypes;
	private String     cstTermType;
	private String     cstPackageName;

	public Compiler(Log log)
	{
		this.log = log;
	}

	File configure(String[] args)
	{
		String specFileName = null;
		String propFileName = null;
		for ( int i = 0; i < args.length; i++ )
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
								if ( ++i == args.length )
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
								if ( ++i == args.length )
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
						if ( ++i == args.length )
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
			catch ( IOException e )
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
		if ( !lang.equals("Java") )
			throw new IllegalArgumentException("unsupported language -- " + lang);

		parserClassName = p.getProperty("parser.class");
		scannerClassName = p.getProperty("scanner.class");

		Collection terms = new ArrayList();
		terms.add(Terminal.EOF);

		if ( scannerClassName == null )
		{
			String pv = p.getProperty("terminal.precedence");
			if ( pv != null )
			{
				terminalsPrecedence = split(pv, ',');
			}
		}

		for ( Iterator i = p.keySet().iterator(); i.hasNext(); )
		{
			String property = (String) i.next();
			if ( property.startsWith("terminal.const.") )
			{
				String name = property.substring(15);
				String text = p.getProperty(property).trim();

				constTermNames.put(text, name);
				terms.add(new Terminal.Const((char) terms.size(), name, text));
			}
			else if ( property.startsWith("cst.type.") )
			{
				String name = property.substring(9);
				String text = p.getProperty(property).trim();

				if ( cstTypes == null )
					cstTypes = new HashMap();
				cstTypes.put(name, text);
			}
		}
		constTerms = (Terminal[]) terms.toArray(new Terminal[terms.size()]);

		if ( terminalsPrecedence != null )
		{
			terms.clear();

			for ( int i = 0; i < terminalsPrecedence.length; i++ )
			{
				terms.add(new Terminal((char) terms.size(), terminalsPrecedence[i]));
			}

			regexTerms = (Terminal[]) terms.toArray(new Terminal[terms.size()]);
		}

		String pv = p.getProperty("srcFileComment");
		if ( pv != null )
		{
			srcFileCommentLines = split(pv, '$');
		}
		cstTermType = p.getProperty("cst.term.type");
		cstPackageName = p.getProperty("cst.package");
	}

	public void compile(File specFile) throws IOException, SyntaxErrorException, CompilationException
	{
		Spec spec = (Spec) new CSTBuilder().parse(new SpecScanner(new FileReader(specFile)));

		if ( spec.scannerSpec != null )
		{
			TokenNamesCollector nc = new TokenNamesCollector();
			spec.scannerSpec.accept(nc);
			terminalsPrecedence = nc.getNames();
		}

		if ( spec.parserSpec != null )
		{
			Grammar grammar = compileGrammar(spec.parserSpec);
			State firstState = compileAutomaton(grammar);

			generateParsingTables(grammar, firstState);

			int lastDot = parserClassName.lastIndexOf('.');
			String className = lastDot < 0 ? parserClassName : parserClassName.substring(lastDot + 1);

			ParserWriter sourceWriter = new ParserWriter(className, grammar, cstTermType, cstTypes);
			if ( lastDot > 0 )
			{
				sourceWriter.setParserPackageName(parserClassName.substring(0, lastDot));
			}
			sourceWriter.setAstPackageName(cstPackageName);
			sourceWriter.setFileComment(srcFileCommentLines);
			sourceWriter.setConstTermNames(constTermNames);
			sourceWriter.setGenerateListBuilders(makeCallbacks);
			sourceWriter.setGenerateNodeBuilders(makeCallbacks);
			sourceWriter.setGenerateCST(makeTypes);
			sourceWriter.writeParserSources(srcDir);

			Terminal[] terminals = grammar.getTerminals();
			int n = 0;
			while ( ++n < terminals.length && terminals[n] instanceof Terminal.Const )
			{
				continue;
			}
			constTerms = new Terminal[n - 1];
			System.arraycopy(terminals, 1, constTerms, 0, constTerms.length);
			regexTerms = new Terminal[terminals.length - n];
			System.arraycopy(terminals, n, regexTerms, 0, regexTerms.length);
		}

		if ( spec.scannerSpec != null )
		{
			Map exclStateRules = null;
			Map inclStateRules = null;
			Map constTermNames = new HashMap();

			Map macros = new MacroCompiler().compile(spec.scannerSpec);

			TokenCompiler tc = new TokenCompiler(macros, constTermNames, log);
			Map mainStateRules = tc.compile(spec.scannerSpec.terminals, "MAIN");
			Collection names = tc.getNames();

			if ( spec.scannerSpec.states != null )
			{
				for ( ScannerState st = (ScannerState) spec.scannerSpec.states.first(); st != null; st = (ScannerState) st.next() )
				{
					tc = new TokenCompiler(macros, constTermNames, log);
					Map stateRules;
					switch ( st.selector.text.charAt(0) )
					{
						case '@':
						{
							if ( inclStateRules == null )
							{
								inclStateRules = new HashMap();
							}
							stateRules = inclStateRules;
							break;
						}
						case '&':
						{
							if ( exclStateRules == null )
							{
								exclStateRules = new HashMap();
							}
							stateRules = exclStateRules;
							break;
						}
						default:
						{
							throw new IllegalStateException("Unknown state type selector " + st.selector.text);
						}
					}
					stateRules.put(st.name.text, tc.compile(st.terminals, st.name.text));
					names.addAll(tc.getNames());
				}
			}

			DFA mainDFA = compileMainStateDFA(mainStateRules, constTermNames);
			DFA[] incDFAs = null;
			String[] incStateNames = null;
			if ( inclStateRules != null )
			{
				incDFAs = new DFA[inclStateRules.size()];
				incStateNames = new String[incDFAs.length];
				int n = 0;
				for ( Iterator i = inclStateRules.entrySet().iterator(); i.hasNext(); n++ )
				{
					Map.Entry e = (Map.Entry) i.next();
					String stateName = (String) e.getKey();
					Map stateRules = (Map) e.getValue();

					incStateNames[n] = stateName;
					incDFAs[n] = compileInclusiveStateDFA(stateRules, constTermNames);

				}
			}
			DFA[] excDFAs = null;
			String[] excStateNames = null;
			if ( exclStateRules != null )
			{
				excDFAs = new DFA[exclStateRules.size()];
				excStateNames = new String[excDFAs.length];
				int n = 0;
				for ( Iterator i = exclStateRules.entrySet().iterator(); i.hasNext(); n++ )
				{
					Map.Entry e = (Map.Entry) i.next();
					String stateName = (String) e.getKey();
					Map stateRules = (Map) e.getValue();

					excStateNames[n] = stateName;
					excDFAs[n] = compileExclusiveStateDFA(stateRules, constTermNames);

				}
			}

			new CharScannerClassWriter().write(scannerClassName.replace('.', '/'), binDir, mainDFA, incDFAs, excDFAs);

			if ( !mainStateRules.isEmpty() )
			{
				String unusedTokens = "";
				String sep = "";
				for ( Iterator i = mainStateRules.keySet().iterator(); i.hasNext(); sep = ", " )
				{
					unusedTokens = unusedTokens + sep + i.next();
				}
				log.warning("The following terminals are not required for the parser: " + unusedTokens + ". They are ignored.");
			}
		}
	}

	private DFA compileMainStateDFA(Map tokenRules, Map constTermNames) throws IOException
	{
		Collection rules = new ArrayList();
		for ( int i = 0; i < constTerms.length; i++ )
		{
			Terminal t = constTerms[i];

			if ( !addConstTermRule(rules, t, tokenRules, constTermNames) )
			{
				rules.add(ScannerBuilder.makeMatchStringRule(t.getId(), t.toString()));
			}
		}
		for ( int i = 0; i < regexTerms.length; i++ )
		{
			Terminal t = regexTerms[i];

			addRegexpTermRule(rules, t, tokenRules);
		}
		rules.add(ScannerBuilder.makeEndOfLineRule());
		rules.add(ScannerBuilder.makeEndOfFileRule());
		rules.add(ScannerBuilder.makeRule(-3, new RegExp.CloseOp(ScannerBuilder.rangeToRegExp("[ \t]"))));

		return ScannerBuilder.compile(rules);
	}

	private DFA compileExclusiveStateDFA(Map tokenRules, Map constTermNames) throws IOException
	{
		Collection rules = compileStateRules(tokenRules, constTermNames);

		rules.add(ScannerBuilder.makeEndOfLineRule());
		rules.add(ScannerBuilder.makeEndOfFileRule());
		rules.add(ScannerBuilder.makeRule(-3, new RegExp.CloseOp(ScannerBuilder.rangeToRegExp("[ \t]"))));

		return ScannerBuilder.compile(rules);
	}

	private DFA compileInclusiveStateDFA(Map tokenRules, Map constTermNames) throws IOException
	{
		Collection rules = compileStateRules(tokenRules, constTermNames);

		return ScannerBuilder.compile(rules);
	}

	private Collection compileStateRules(Map tokenRules, Map constTermNames)
	{
		Collection rules = new ArrayList();
		for ( int i = 0; i < constTerms.length; i++ )
		{
			Terminal t = constTerms[i];

			addConstTermRule(rules, t, tokenRules, constTermNames);
		}
		for ( int i = 0; i < regexTerms.length; i++ )
		{
			Terminal t = regexTerms[i];

			addRegexpTermRule(rules, t, tokenRules);
		}
		return rules;
	}

	private void addRegexpTermRule(Collection rules, Terminal t, Map tokenRules)
	{
		RegExp.RuleOp rule = (RegExp.RuleOp) tokenRules.remove(t.toString());
		if ( rule != null )
		{
			rule.setId(t.getId());
			rules.add(rule);
		}
	}

	private boolean addConstTermRule(Collection rules, Terminal t, Map tokenRules, Map constTermNames)
	{
		String text = t.toString();
		String name = (String) constTermNames.get(text);
		if ( name != null )
		{
			RegExp.RuleOp rule = (RegExp.RuleOp) tokenRules.remove(name);
			if ( rule != null )
			{
				rule.setId(t.getId());
				rules.add(rule);

				return true;
			}
		}
		return false;
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

		if ( unreferencedNames.remove("error") ) // because there is no rule
													// for it to remove
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

	private void generateParsingTables(Grammar grammar, State firstState) throws IOException
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

	private static String[] split(String str, char c)
	{
		Collection parts = new ArrayList();
		int b = 0;
		for ( int e = str.indexOf(c); e > 0; b = e + 1, e = str.indexOf(c, b) )
		{
			parts.add(str.substring(b, e).trim());
		}
		parts.add(str.substring(b).trim());

		return (String[]) parts.toArray(new String[parts.size()]);
	}

	private static void printUsageInstructions()
	{
		System.err.println("Usage: java -jar beaver.jar [options] language_specification_file.name");
		System.err.println("where options include:");
		System.err.println("\t-ds dir  destination directory for generated source file");
		System.err.println("\t-db dir  destination directory for binary/class files");
		System.err.println("\t-t       generate implementations of semantic types");
		System.err.println("\t-m       generate implementations of CST building methods");
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
		catch ( IllegalArgumentException e )
		{
			System.err.println("Error: " + e.getMessage());
			printUsageInstructions();
		}
		catch ( Exception e )
		{
			//System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	static class StdoutLog implements Log
	{
		static final PrintStream out = System.out;
		static final Where       loc = new Where();

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
