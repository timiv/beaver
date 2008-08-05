package beaver.parser;

import beaver.util.BitSet;

class Nonterminal extends Symbol
{
	/**
	 * Rules to derive this nonterminal, i.e. where this non-terminal is a LHS.
	 */
	Production[] rules;

	/**
	 * Non-terminal is nullable if any of its productions can match an empty string.
	 */
	boolean      isNullable;

	/**
	 * The set of all terminal symbols that could appear at the beginning of a string derived from
	 * this nonterminal.
	 */
	BitSet       firstSet;

	Nonterminal(String name)
	{
		super(name);
	}

	/**
	 * Nonterminal can match an empty string only if one of its derivation rules matches an empty
	 * string. This method returns what has been found (so far) by calculating nullability of this
	 * nonterminal.
	 * 
	 * @return true if the symbol is nullable
	 */
	boolean isNullable()
	{
		return isNullable;
	}
	
}
