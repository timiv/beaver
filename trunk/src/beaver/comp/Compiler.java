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
import java.util.Map;
import java.util.Set;

import beaver.SyntaxErrorException;
import beaver.comp.parser.Action;
import beaver.comp.parser.Grammar;
import beaver.comp.parser.ParserWriter;
import beaver.comp.parser.ParsingTables;
import beaver.comp.parser.State;
import beaver.comp.spec.AstBuilder;
import beaver.comp.spec.ParserSpec;
import beaver.comp.spec.Spec;
import beaver.comp.spec.SpecScanner;
import beaver.util.BitSet;

/**
 * @author Alexander Demenchuk
 *
 */
public class Compiler
{
	private Log log;
	private String[] termOrder;
	
	public Compiler(Log log)
	{
		this.log = log;
	}
	
	public void setTerminalsOrder(String[] terms)
	{
		termOrder = terms;
	}
	
	public void compile(File src, File dstDir) throws IOException, SyntaxErrorException, CompilationException
	{
		Grammar  grammar = compileGrammar(src);
		if ( grammar != null )
		{
    		State firstState = compileAutomaton(grammar);
    		
    		generateParsingTables(grammar, firstState, dstDir);
    		generateParserSource(grammar, null, null, dstDir);
		}
	}

    Grammar compileGrammar(File src) throws IOException, SyntaxErrorException
    {
	    Spec spec = (Spec) new AstBuilder().parse(new SpecScanner(new FileReader(src)));
	    
	    ParserSpec parserSpec;
	    if ( (parserSpec = spec.parserSpec) == null )
	    {
	    	return null;
	    }
	    
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
		
		GrammarBuilder grammarBuilder = new GrammarBuilder(constTokens, termCollector.getNamedTokens(), termOrder, nonterminals, log);
		parserSpec.accept(grammarBuilder);
		return grammarBuilder.getGrammar();
    }
    

	State compileAutomaton(Grammar grammar) throws CompilationException
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

	void generateParsingTables(Grammar grammar, State firstState, File dstDir) throws IOException
    {
	    ParsingTables tables = new ParsingTables(grammar, firstState);
		DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(dstDir, "SpecParser.tables")));
		try
		{
			tables.writeTo(out);
		}
		finally
		{
			out.close();
		}
    }
	
	void generateParserSource(Grammar grammar, Map semanticValueTypes, Map ctermNames, File dstDir) throws IOException
	{
		ParserWriter sourceWriter = new ParserWriter("SpecParser", grammar, "Term", semanticValueTypes);
		sourceWriter.setParserPackageName("beaver.comp.spec");
		sourceWriter.setSemanticTypesPackageName("beaver.comp.spec");
		sourceWriter.setFileComment("/**\n * Beaver: compiler front-end construction toolkit\n * Copyright (c) 2007 Alexander Demenchuk <alder@softanvil.com>\n * All rights reserved.\n *\n * See the file \"LICENSE\" for the terms and conditions for copying,\n * distribution and modification of Beaver.\n */");
		sourceWriter.setConstTermNames(ctermNames);
		sourceWriter.writeParserSource(dstDir);
		sourceWriter.writeSemanticTypes(dstDir);
	}
}
