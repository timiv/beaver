package beaver.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Builds a Grammar
 */
public class GrammarFactory
{
	Map         symbols       = new HashMap();
	Map         keywords      = new HashMap();
	List        productions   = new ArrayList();
	Collection  nonterminals  = new ArrayList();
	Collection  terminals     = new ArrayList();
	Nonterminal lhs;
	String      ruleName;
	Collection  rhs           = new ArrayList();
	char        precedence    = '\uffff';
	char        associativity = 'N'; 

	public GrammarFactory(String[] terminalNames)
	{
		for (int i = 0; i < terminalNames.length; i++)
		{
			String name = terminalNames[i];
			Terminal term = (Terminal) symbols.get(name);
			if (term == null)
			{
				symbols.put(name, term = new Terminal(name));
				terminals.add(term);
			}
		}
	}

	public GrammarFactory def(String symbolName, String ruleName)
	{
		if (lhs != null)
		{
			throw new IllegalStateException(lhs.name + " build is in progress");
		}
		lhs = (Nonterminal) symbols.get(symbolName);
		if (lhs == null)
		{
			symbols.put(symbolName, lhs = new Nonterminal(symbolName));
			nonterminals.add(lhs);
		}
		this.ruleName = ruleName;
		return this;
	}

	public GrammarFactory def(String symbolName)
	{
		return def(symbolName, null);
	}

	public GrammarFactory sym(String symbolName, String referenceName)
	{
		Symbol symbol = (Symbol) symbols.get(symbolName);
		if (symbol == null)
		{
			symbols.put(symbolName, symbol = new Nonterminal(symbolName));
			nonterminals.add(symbol);
		}
		rhs.add(new Production.RHSElement(referenceName, symbol));
		return this;
	}

	public GrammarFactory sym(String symbolName)
	{
		return sym(symbolName, null);
	}

	public GrammarFactory txt(String text)
	{
		Symbol symbol = (Symbol) keywords.get(text);
		if (symbol == null)
		{
			keywords.put(text, symbol = new Terminal("$" + keywords.size(), text));
			symbols.put(symbol.name, symbol);
		}
		rhs.add(new Production.RHSElement(symbol));
		return this;
	}

	public GrammarFactory end()
	{
		productions.add(
				new Production(
						ruleName, 
						lhs, 
						(Production.RHSElement[]) rhs.toArray(new Production.RHSElement[rhs.size()])
				)
		);
		rhs.clear();
		lhs = null;
		return this;
	}
	
	public GrammarFactory left()
	{
		precedence--;
		associativity = 'L';
		return this;	
	}
	
	public GrammarFactory right()
	{
		precedence--;
		associativity = 'R';
		return this;	
	}
	
	public GrammarFactory none()
	{
		precedence--;
		associativity = 'N';
		return this;	
	}
	
	public GrammarFactory prec(String sym)
	{
		Symbol symbol = (Symbol) symbols.get(sym);
		if (symbol == null)
		{
			symbol = (Symbol) keywords.get(sym);
		}
		if (!(symbol instanceof Terminal))
		{
			throw new IllegalArgumentException(sym + " is not a terminal");
		}
		Terminal t = (Terminal) symbol;
		if (t.precedence > '\0')
		{
			throw new IllegalStateException(sym + " has been already assigned a precedence");
		}
		((Terminal) symbol).precedence = precedence;
		((Terminal) symbol).associativity = associativity;
		
		return this;
	}

	public GrammarFactory prec(String sym, String name)
	{
		Symbol symbol = (Symbol) symbols.get(sym);
		if (!(symbol instanceof Nonterminal))
		{
			throw new IllegalArgumentException(sym + " is not a nonterminal");
		}
		Production prod = null;
		for (Iterator i = productions.iterator(); i.hasNext();)
        {
			Production p = (Production) i.next();
	        if (p.lhs.name.equals(sym) && p.name != null && p.name.equals(name))
	        {
	        	prod = p;
	        	break;
	        }
        }
		if (prod == null)
		{
			throw new IllegalArgumentException("there is no production " + sym + " { " + name + " }");
		}
		prod.precedence = precedence;
		
		return this;
	}

	public Grammar getGrammar()
	{
		ensureGrammarHasRules();
		/*
		 * Ensure that the goal of the grammar (LHS nonterminal of the first production) does not have alternate definitions
		 */
		ensureValidGoalExists();
		/*
		 * Extract productions and nonterminals into arrays - that's how the Grammar wants to see them.
		 */
		Production[] grammarProductions = (Production[]) productions.toArray(new Production[productions.size()]);
		Nonterminal[] grammarNonterminals = (Nonterminal[]) nonterminals.toArray(new Nonterminal[nonterminals.size()]);
		/*
		 * Check that all nonterminals have productions rules for them.
		 * Also, while we are looking at nonterminals and productions, collect production
		 * rules for the nonterminals.
		 */
		ensureAllNonterminalsHaveDefiningRules(grammarProductions, grammarNonterminals);
		/*
		 * Check rule-based terminals. Throw away unused ones.
		 */
		discardUnusedTerminals(grammarProductions);
		
		return new Grammar(grammarProductions, grammarNonterminals, collectTerminals());
	}

	private void ensureGrammarHasRules()
    {
	    if (lhs != null)
		{
			throw new IllegalStateException(lhs.name + " build is in progress");
		}
		if (productions.size() == 0)
		{
			throw new IllegalStateException("grammar is undefined");
		}
    }

	private Terminal[] collectTerminals()
    {
	    Terminal[] txtTerminals = (Terminal[]) keywords.values().toArray(new Terminal[keywords.size()]);
		Arrays.sort(txtTerminals, new Comparator()
		{
			public int compare(Object o1, Object o2)
            {
	            return ((Terminal) o1).name.compareTo(((Terminal) o2).name);
            }
		});
		
		Collection allTerminals = new ArrayList(1 + keywords.size() + this.terminals.size());
		allTerminals.add(new Terminal("EOF"));
		for (int i = 0; i < txtTerminals.length; i++)
        {
	        allTerminals.add(txtTerminals[i]);
        }
		allTerminals.addAll(terminals);
		Terminal[] allTerminalsArray = (Terminal[]) allTerminals.toArray(new Terminal[allTerminals.size()]);
	    return allTerminalsArray;
    }

	private void discardUnusedTerminals(Production[] grammarProductions)
    {
	    for (Iterator i = terminals.iterator(); i.hasNext();)
        {
	        Terminal term = (Terminal) i.next();
	        if (!isSymbolUsed(term, grammarProductions))
	        {
	        	// TODO: report discarded terminal
	        	i.remove();
	        }
        }
    }

	private void ensureAllNonterminalsHaveDefiningRules(Production[] grammarProductions, Nonterminal[] grammarNonterminals)
    {
	    Collection rules = new ArrayList();
		
		for (int i = 0; i < grammarNonterminals.length; i++)
        {
			Nonterminal nt = grammarNonterminals[i];
			for (int j = 0; j < grammarProductions.length; j++)
            {
	            Production production = grammarProductions[j];
	            if (production.lhs == nt)
	            {
	            	rules.add(production);
	            }
            }
			if (rules.size() != 0)
			{
				nt.rules = (Production[]) rules.toArray(new Production[rules.size()]);
				rules.clear();
			}
			else if ("error".equals(nt.name))
			{
				nt.rules = new Production[] {};
			}
			else
			{
				// TODO: report _all_ undefined nonterminals
				throw new IllegalStateException(nt.name + " is undefined");
			}
        }
    }

	private void ensureValidGoalExists()
    {
	    Nonterminal goal = null;
		for (Iterator i = productions.iterator(); i.hasNext();)
        {
	        Production production = (Production) i.next();
	        if (goal == null)
	        {
	        	goal = production.lhs;
	        }
	        else if (production.lhs == goal)
	        {
	        	/*
	        	 * Need to augment the grammar with a synthetic goal production
	        	 */
	        	Nonterminal newGoal = new Nonterminal("Goal");
	        	nonterminals.add(newGoal);
	        	/*
	        	 * Insert the definition of the new goal as the very first production
	        	 */
	        	productions.add(
	        			0,
	        			new Production(
	    	        			newGoal,
	    	        			new Production.RHSElement[] {
	    	        					new Production.RHSElement(goal)
	    	        			}
	    	        	)
	        	);
	        	break;
	        }
        }
    }
	
	private static boolean isSymbolUsed(Symbol symbol, Production[] productions)
	{
		for (int i = 0; i < productions.length; i++)
        {
            Production.RHSElement[] rhs = productions[i].rhs;
	        for (int j = 0; j < rhs.length; j++)
            {
	            if (rhs[j].symbol == symbol)
	            	return true;
            }
        }
		return false;
	}
}