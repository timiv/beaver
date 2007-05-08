/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import beaver.comp.cst.Alt;
import beaver.comp.cst.ItemSymbol;
import beaver.comp.cst.PrecItemRule;
import beaver.comp.cst.PrecItemTerm;
import beaver.comp.cst.Precedence;
import beaver.comp.cst.Rule;
import beaver.comp.cst.TreeWalker;
import beaver.comp.parser.Grammar;
import beaver.comp.parser.NonTerminal;
import beaver.comp.parser.Production;
import beaver.comp.parser.Symbol;
import beaver.comp.parser.Terminal;

/**
 * @author Alexander Demenchuk
 *
 */
public class GrammarBuilder extends TreeWalker
{
	private Log log;
	private Map constTerms;
	private Map symbols = new HashMap();
	private int numTerms, numNonTerms;
	private NonTerminal lhs;
	private char nextProdId;
	private Collection  productions;
	private Collection  rhs;
	private Map productionsByName;
	private char precedence;
	private char assoc;
	
	public GrammarBuilder(Map constTerms, Collection namedTerms, String[] termOrder, Collection nonterms, Log log)
	{
		this.log = log;
		this.constTerms = constTerms;
		
		symbols.put("EOF", Terminal.EOF);
		char id = Terminal.EOF.getId();
		
		List constTermRepresentations = new ArrayList(constTerms.keySet());
		Collections.sort(constTermRepresentations);
		for (Iterator i = constTermRepresentations.iterator(); i.hasNext(); )
		{
			String text = (String) i.next();
			String name = (String) constTerms.get(text);
			symbols.put(name, new Terminal.Const(++id, name, text));
		}
		
		if ( termOrder != null )
		{
			for ( int i = 0; i < termOrder.length; i++ )
            {
				String name = termOrder[i];
				if ( !namedTerms.contains(name) )
				{
					log.warning("Terminal " + name + " is in preferred order list, but is not used in the grammar");
				}
				else
				{
					symbols.put(name, new Terminal(++id, name));
				}
            }
		}
		
		for (Iterator i = namedTerms.iterator(); i.hasNext(); )
		{
			String name = (String) i.next();
			if ( !symbols.containsKey(name) )
			{
				symbols.put(name, new Terminal(++id, name));
			}
		}
		numTerms = id + 1;
		
		nonterms.remove("error");
		
		for (Iterator i = nonterms.iterator(); i.hasNext(); )
		{
			String name = (String) i.next();
			symbols.put(name, new NonTerminal(++id, name));
		}
		numNonTerms = id + 1 - numTerms;

		symbols.put("error", new NonTerminal(++id, "error"));
		
		productions = new ArrayList();
		rhs = new ArrayList();
		
		productionsByName = new HashMap();
		
		precedence = '\uffff';
	}
	
	private void add(Production prod)
	{
	    productions.add(prod);
	    
	    String name = prod.getName();
	    
	    Object existing = productionsByName.get(name);
	    if ( existing == null )
	    {
	    	productionsByName.put(name, prod);
	    }
	    else if ( existing instanceof Production )
	    {
	    	Collection namesakeProductions = new ArrayList();
	    	namesakeProductions.add(existing);
	    	namesakeProductions.add(prod);
	    	productionsByName.put(name, namesakeProductions);
	    }
	    else if ( existing instanceof Collection )
	    {
	    	((Collection) existing).add(prod);
	    }
	    else
	    {
	    	throw new IllegalStateException("corrupted rule registry");
	    }
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

	    add(
	    	new Production(
	    		nextProdId++,
	    		alt.name != null ? lhs.getName() + alt.name.text : lhs.getName(),
	    		lhs,
	    		(Production.RHSItem[]) rhs.toArray(new Production.RHSItem[rhs.size()])
	    	)
	    );
    }

	public void visit(ItemSymbol item)
    {
		String ref = item.ref != null ? item.ref.text : null;
		Symbol sym = (Symbol) symbols.get(item.name.text);
		if (sym == null)
			throw new IllegalStateException("Cannot find '" + item.name.text + "' in the symbol dictionary.");
		
		rhs.add( new Production.RHSItem(ref, sym) );
    }
	
	public void visit(Precedence prec)
	{
		this.precedence--;
		this.assoc = Character.toUpperCase(prec.assoc.text.charAt(0));
		
		super.visit(prec);
	}
	
	public void visit(PrecItemRule item)
	{
		String name = item.name.text;

		Object obj = productionsByName.get(name);
		if ( obj instanceof Production )
		{
			((Production) obj).setPrecedence(precedence);
		}
		else if ( obj instanceof Collection )
		{
			for (Iterator i = ((Collection) obj).iterator(); i.hasNext();)
            {
	            ((Production) i.next()).setPrecedence(precedence);
            }
		}
		else
		{
			obj = symbols.get(name);
			if ( obj instanceof Terminal )
			{
				((Terminal) obj).setPrecedence(precedence, assoc);
			}
			else
			{
				log.error(item, "There is no terminal or rule named " + name);
			}
		}
	}

	public void visit(PrecItemTerm item)
	{
		String repr = item.text.text;
		String name = (String) constTerms.get(repr);
		if ( name == null )
		{
			log.error(item, "Token " + repr + " was not used in the grammar");
		}
		else
		{
			Terminal term = (Terminal) symbols.get(name);
			if ( term == null )
				throw new IllegalStateException("Missing terminal for token " + repr);
			term.setPrecedence(precedence, assoc);
		}
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
