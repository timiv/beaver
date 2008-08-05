package beaver.parser;

import beaver.util.BitSet;

class Grammar
{
	/**
	 * Symbols that are created by a scanner and represent input tokens for the parser.
	 */
	Terminal[]    terminals;

	/**
	 * Symbols that are created by the parser, when a RHS of a production is reduced to a LHS.
	 */
	Nonterminal[] nonterminals;

	/**
	 * Rules to derive nonterminal symbols.
	 */
	Production[]  productions;

	Grammar(Production[] productions, Nonterminal[] nonterminals, Terminal[] terminals)
	{
		markNullable(nonterminals);
		buildFirstSets(productions, nonterminals, terminals);
		assignIDs(productions, nonterminals, terminals);
		assignPrecedences(productions);

		this.productions = productions;
		this.nonterminals = nonterminals;
		this.terminals = terminals;
	}
	
	Nonterminal getGoal()
	{
		return productions[0].lhs;
	}

	Terminal getEOF()
	{
		return terminals[0];
	}
	
	int getAcceptActionId()
	{
		return ~productions.length;
	}
	
	BitSet findUnreducibleProductions(ParserState firstState)
	{
		BitSet set = new BitSet(productions.length);
		set.add(0, productions.length);
		for (ParserState state = firstState; state != null; state = state.next)
		{
			for (ParserAction.Reduce reduce = (ParserAction.Reduce) state.reduce; reduce != null; reduce = (ParserAction.Reduce) reduce.next)
			{
				set.erase(reduce.production.id);
			}
		}
		return set;
	}

	private static void markNullable(Nonterminal[] nonterminals)
    {
		for (boolean marking = true; marking;)
		{
			marking = false;

			for (int i = 0; i < nonterminals.length; i++)
			{
				Nonterminal nt = nonterminals[i];

				if (!nt.isNullable)
				{
					for (int j = 0; j < nt.rules.length; j++)
					{
						if (nt.rules[j].isNullable())
						{
							nt.isNullable = true;
							marking = true;
							break;
						}
					}
				}
			}
		}
    }

	private static void buildFirstSets(Production[] productions, Nonterminal[] nonterminals, Terminal[] terminals)
    {
	    /*
		 * Setup
		 */
		for (int i = 0; i < nonterminals.length; i++)
		{
			nonterminals[i].firstSet = new BitSet(terminals.length);
		}
		/*
		 * Build First Sets
		 */
		addFirstGenerationTerminalsToFirstSets(productions);
		addRemainingTerminalsToFirstSets(productions);
    }

	private static void addFirstGenerationTerminalsToFirstSets(Production[] productions)
    {
	    /*
		 * Create first generation of first set terminals
		 */
		for (int i = 0; i < productions.length; i++)
		{
			Nonterminal lhs = productions[i].lhs;
			Production.RHSElement[] rhs = productions[i].rhs;

			for (int j = 0; j < rhs.length; j++)
			{
				Symbol symbol = rhs[j].symbol;

				if (symbol instanceof Terminal)
				{
					lhs.firstSet.add(symbol.id);
				}
				else
				{
					Nonterminal nt = (Nonterminal) symbol;
					if (nt != lhs)
					{
						lhs.firstSet.add(nt.firstSet);
					}
				}

				if (!symbol.isNullable())
				{
					break;
				}
			}
		}
    }

	private static void addRemainingTerminalsToFirstSets(Production[] productions)
    {
	    /*
		 * Build first sets - step 3: Keep adding terminals from leading nonterminals
		 */
		for (boolean adding = true; adding;)
		{
			adding = false;

			for (int i = 0; i < productions.length; i++)
			{
				Nonterminal lhs = productions[i].lhs;
				Production.RHSElement[] rhs = productions[i].rhs;

				for (int j = 0; j < rhs.length; j++)
				{
					Symbol symbol = rhs[j].symbol;

					if (symbol instanceof Nonterminal)
					{
						Nonterminal nt = (Nonterminal) symbol;
						if (nt != lhs)
						{
							if (lhs.firstSet.add(nt.firstSet))
							{
								adding = true;
							}
						}
					}

					if (!symbol.isNullable())
					{
						break;
					}
				}
			}
		}
    }

	private static void assignIDs(Production[] productions, Nonterminal[] nonterminals, Terminal[] terminals)
    {
		int sid = 0;
		for (int i = 0; i < terminals.length; i++)
        {
	        terminals[i].id = sid++;
        }
		for (int i = 0; i < nonterminals.length; i++)
        {
	        nonterminals[i].id = sid++;
        }
		for (int i = 0; i < productions.length; i++)
        {
	        productions[i].id = i;
        }
    }
	
	private static void assignPrecedences(Production[] productions)
	{
		/*
		 * The default precedence of the production is the precedence of the rightmost terminal.
		 */
		for (int i = 0; i < productions.length; i++)
		{
			if (productions[i].precedence == '\0')
			{
				Terminal t = null;
				for (int j = 0; j < productions[i].rhs.length; j++)
				{
					if (productions[i].rhs[j].symbol instanceof Terminal)
					{
						t = (Terminal) productions[i].rhs[j].symbol;
					}
				}
				if (t != null)
				{
					productions[i].precedence = t.precedence;
				}
			}
		}
	}
}
