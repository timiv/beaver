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
import java.util.Set;

import beaver.SyntaxErrorException;
import beaver.comp.spec.AstBuilder;
import beaver.comp.spec.Spec;
import beaver.comp.spec.SpecScanner;

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
	
	public void compile(File src) throws IOException, SyntaxErrorException
	{
		Spec spec = (Spec) new AstBuilder().parse(new SpecScanner(new FileReader(src)));
		spec.accept(new InlineRulesExtractor());
		spec.accept(new EbnfOperatorCompiler());
		spec.accept(new InlineStringExractor());
		
		NonTerminalSymbolNamesCollector nontermCollector = new NonTerminalSymbolNamesCollector();
		spec.accept(nontermCollector);
		Set nonterminals =nontermCollector.getNames(); 
		
		UnreferencedNonTerminalFinder unrefNontermFinder = new UnreferencedNonTerminalFinder(nonterminals);
		spec.accept(unrefNontermFinder);
		Set unreferencedNames = unrefNontermFinder.getSymbolNames();
		
		UnusedRuleRemover ruleRemover = new UnusedRuleRemover(unreferencedNames, log);
		spec.accept(ruleRemover);
		
		nonterminals.removeAll(unreferencedNames);	
		
		TerminalSymbolNamesCollector termCollector = new TerminalSymbolNamesCollector(nonterminals);
		spec.accept(termCollector);
	}
	
}
