/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import beaver.comp.parser.Grammar;
import beaver.comp.parser.NonTerminal;
import beaver.comp.parser.Production;
import beaver.comp.parser.Symbol;
import beaver.comp.parser.Terminal;
import beaver.comp.spec.Alt;
import beaver.comp.spec.ItemSymbol;
import beaver.comp.spec.Rule;
import beaver.comp.spec.TreeWalker;

/**
 * @author Alexander Demenchuk
 *
 */
public class GrammarBuilder extends TreeWalker
{
	private Map symbols = new HashMap();
	private int numTerms, numNonTerms;
	private NonTerminal lhs;
	private char nextProdId;
	private Collection  productions;
	private Collection  rhs;
	
	public GrammarBuilder(Map constTerms, Collection namedTerms, Collection nonterms)
	{
		symbols.put("EOF", Terminal.EOF);
		char id = Terminal.EOF.getId();
		for (Iterator i = constTerms.entrySet().iterator(); i.hasNext(); )
		{
			Map.Entry e = (Map.Entry) i.next();
			String text = (String) e.getKey();
			String name = (String) e.getValue();
			symbols.put(name, new Terminal.Const(id++, name, text));
		}
		for (Iterator i = namedTerms.iterator(); i.hasNext(); )
		{
			String name = (String) i.next();
			symbols.put(name, new Terminal(id++, name));
		}
		numTerms = id;
		
		boolean hasErrorSymbol = nonterms.remove("error");
		for (Iterator i = nonterms.iterator(); i.hasNext(); )
		{
			String name = (String) i.next();
			symbols.put(name, new NonTerminal(id++, name));
		}
		numNonTerms = id - numTerms;

		if (hasErrorSymbol)
		{
			symbols.put("error", new NonTerminal(id, "error"));
		}
		
		productions = new ArrayList();
		rhs = new ArrayList();
	}

	public void visit(Rule rule)
    {
		lhs = (NonTerminal) symbols.get(rule.name.text);
	    super.visit(rule);
    }

	public void visit(Alt alt)
    {
		rhs.clear();
		
	    super.visit(alt);

	    productions.add(
	    	new Production(
	    		nextProdId++,
	    		alt.name != null ? lhs.getName() + alt.name.text : lhs.getName(),
	    		lhs,
	    		(Production.RHSElement[]) rhs.toArray(new Production.RHSElement[rhs.size()])
	    	)
	    );
    }

	public void visit(ItemSymbol item)
    {
		String ref = item.refName != null ? item.refName.text : null;
		Symbol sym = (Symbol) symbols.get(item.symName.text);
		if (sym == null)
			throw new IllegalStateException("Cannot find '" + item.symName.text + "' in the symbol dictionary.");
		
		rhs.add( new Production.RHSElement(ref, sym) );
    }
	
	public Grammar getGrammar()
	{
		Terminal[]       terms = new Terminal[numTerms];
		NonTerminal[] nonterms = new NonTerminal[numNonTerms];
		
		NonTerminal err = (NonTerminal) symbols.remove("error");
		
		for (Iterator i = symbols.values().iterator(); i.hasNext(); )
		{
			Symbol sym = (Symbol) i.next();
			
			if (sym instanceof Terminal)
				terms[sym.getId()] = (Terminal) sym;
			else
				nonterms[sym.getId() - numTerms] = (NonTerminal) sym;
		}

		Production[] rules = (Production[]) productions.toArray(new Production[productions.size()]);
		
		return new Grammar(terms, nonterms, rules, err);
	}

}
