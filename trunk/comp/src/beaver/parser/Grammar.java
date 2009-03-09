package beaver.parser;

import java.io.DataOutput;
import java.io.IOException;

import beaver.util.BitSet;

class Grammar
{
	/**
	 * Symbols that are created by a scanner and represent input tokens for the parser.
	 */
	Terminal[]    terminals;

	/**
	 * Symbols that are created by the parser, when RHS of a production is reduced to LHS.
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
		// data discovered below are needed for parser source(s) generation
		markValueProducers(nonterminals);
		findDelegates(nonterminals);
		markListProducers(nonterminals);
		findSignatures(productions);

		this.productions = productions;
		this.nonterminals = nonterminals;
		this.terminals = terminals;
	}
	
	void writeTo(DataOutput out) throws IOException
	{
		out.writeChar(productions.length);
		for (int i = 0; i < productions.length; i++)
        {
			int ruleSize = productions[i].rhs.length;
			int lhsNtId  = productions[i].lhs.id;
	        out.writeInt((lhsNtId << 16) | ruleSize);
        }
		// find first non-keyword symbol
		int id = 0;
		for (int i = 1; i < terminals.length; i++)
        {
	        if (terminals[i].text == null)
	        {
	        	id = terminals[i].id;
	        	break;
	        }
        }
		if (id == 0)
		{
			id = nonterminals[0].id;
		}
		out.writeChar(id);
		// all symbols
		out.writeChar(terminals.length + nonterminals.length);
		for (int i = 0; i < terminals.length; i++)
		{
			out.writeUTF(terminals[i].toString());
		}
		for (int i = 0; i < nonterminals.length; i++)
		{
			out.writeUTF(nonterminals[i].toString());
		}
		out.writeByte(4);
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
	
	private static void findSignatures(Production[] productions)
	{
		for (int i = 0; i < productions.length; i++)
		{
			productions[i].findRhsValueProducers();
		}
	}
	
	private static void findDelegates(Nonterminal[] nonterminals)
	{
		for (boolean mutating = true; mutating;)
		{
			mutating = false;
			
			for (int i = 0; i < nonterminals.length; i++)
			{
				Nonterminal nt = nonterminals[i];
				if (nt.delegate == null)
				{
    				switch (nt.rules.length)
    				{
    					case 1:
    					{
    						Symbol sym = nt.rules[0].findSingleValue();
    						if (sym != null)
    						{
    							nt.delegate = sym;
    							mutating = true;
    						}
    						break;
    					}
    					case 2:
    					{
    						if (nt.rules[0].rhs.length == 0)
    						{
        						Symbol sym = nt.rules[1].findSingleValue();
        						if (sym != null)
        						{
        							nt.delegate = sym;
        							mutating = true;
        						}
    						}
    						else if (nt.rules[1].rhs.length == 0)
    						{
        						Symbol sym = nt.rules[0].findSingleValue();
        						if (sym != null)
        						{
        							nt.delegate = sym;
        							mutating = true;
        						}
    						}
    						break;
    					}
    				}
				}
				else if (nt.delegate instanceof Nonterminal)
				{
					Nonterminal delegate = (Nonterminal) nt.delegate;
					if (delegate.delegate != null)
					{
						nt.delegate = delegate.delegate;
						mutating = true;
					}
				}
			}			
		}		
	}

	private static void markValueProducers(Nonterminal[] nonterminals)
    {
		for (boolean marking = true; marking;)
		{
			marking = false;

			for (int i = 0; i < nonterminals.length; i++)
			{
				Nonterminal nt = nonterminals[i];

				if (!nt.isValueProducer)
				{
					for (int j = 0; j < nt.rules.length; j++)
					{
						if (nt.rules[j].isValueProducer())
						{
							nt.isValueProducer = true;
							marking = true;
							break;
						}
					}
				}
			}
		}
    }

	private static void markListProducers(Nonterminal[] nonterminals)
	{
		checkingNonterminals:
		for (int i = 0; i < nonterminals.length; i++)
		{
			Nonterminal nt = nonterminals[i];
			if (nt.delegate == null && nt.rules.length == 2)
			{
				int newListRule;
				Symbol element = nt.rules[newListRule = 0].findSingleValue();
				if (element == null)
				{
					element = nt.rules[newListRule = 1].findSingleValue();
					if (element == null)
					{
						continue checkingNonterminals;
					}
				}
				int extListRule = newListRule ^ 1;
				Production.RHSElement[] rhs = nt.rules[extListRule].rhs;
				int matching = 0;
				for (int j = 0; j < rhs.length; j++)
                {
					if (rhs[j].symbol.isValueProducer())
					{
						switch (matching)
						{
							case 0:
							{
								if (rhs[j].symbol != nt)
								{
									continue checkingNonterminals;
								}
								matching = 1;
								break;
							}
							case 1:
							{
								if (rhs[j].symbol != element)
								{
									continue checkingNonterminals;
								}
								matching = -1;
								break;
							}
							default:
							{
								continue checkingNonterminals;
							}
						}
					}
                }
				if (matching < 0)
				{
					nt.isListProducer = true;
				}
			}
		}		
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
