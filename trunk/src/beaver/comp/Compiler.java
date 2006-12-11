/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import beaver.SyntaxErrorException;
import beaver.comp.parser.Action;
import beaver.comp.parser.Grammar;
import beaver.comp.parser.ParsingTables;
import beaver.comp.parser.State;
import beaver.comp.spec.AstBuilder;
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
	
	public Compiler(Log log)
	{
		this.log = log;
	}
	
	public void compile(File src) throws IOException, SyntaxErrorException, CompilationException
	{
		Grammar  grammar = compileGrammar(src);
		State firstState = compileAutomaton(grammar);
		
		ParsingTables tables = new ParsingTables(grammar, firstState);
		tables.writeTo(null);
	}

    Grammar compileGrammar(File src) throws IOException, SyntaxErrorException
    {
	    Spec spec = (Spec) new AstBuilder().parse(new SpecScanner(new FileReader(src)));
		spec.accept(new InlineRulesExtractor());
		spec.accept(new EbnfOperatorCompiler());
		spec.accept(new InlineStringExractor());
		
		NonTerminalCollector nontermCollector = new NonTerminalCollector(log);
		spec.accept(nontermCollector);
		Set nonterminals = nontermCollector.getNames();
		/*
		 * Inject the 'error' nonterminal without a definition
		 */
		nonterminals.add("error");
		
		UnreferencedNonTerminalFinder unrefNontermFinder = new UnreferencedNonTerminalFinder(nonterminals);
		spec.accept(unrefNontermFinder);
		Set unreferencedNames = unrefNontermFinder.getSymbolNames();
		
		if ( unreferencedNames.remove("error") ) // because there is no rule for it to remove
		{
			nonterminals.remove("error");
		}
		spec.accept(new UnusedRuleRemover(unreferencedNames, log));
		nonterminals.removeAll(unreferencedNames);
		
		TerminalCollector termCollector = new TerminalCollector(nonterminals);
		spec.accept(termCollector);
		Map constTokens = termCollector.getConstTokens();
		
		spec.accept(new InlineTokenReplacer(constTokens));
		
		GrammarBuilder grammarBuilder = new GrammarBuilder(constTokens, termCollector.getNamedTokens(), nonterminals);
		spec.accept(grammarBuilder);
		return grammarBuilder.getGrammar();
    }
    

	State compileAutomaton(Grammar grammar) throws CompilationException
    {
	    State firstState = new State.Builder().createStates(grammar);
		
		Action.ConflictResolver conflictResolver = new Action.ConflictResolver();
		if ( !conflictResolver.resolveConflicts(firstState) )
		{
			throw new CompilationException("there are conflicts");
		}
		BitSet unreducibleProductions = grammar.findUnreducibleProductions(firstState);
		if ( unreducibleProductions.size() != 0 )
		{
			throw new CompilationException("grammar has unreducible productions");
		}
		new Action.Compressor(grammar).compress(firstState);
	    return firstState;
    }
}
