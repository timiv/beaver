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
	 * Nonterminal is a value producer if at least one of its defining rules produces a value.
	 */
	boolean      isValueProducer;
	
	boolean      isListProducer;

	/**
	 * The set of all terminal symbols that could appear at the beginning of a string derived from
	 * this nonterminal.
	 */
	BitSet       firstSet;
	
	/**
	 * Some nonterminal definitions allow to substitute specified nonterminal with one of the symbols
	 * from its RHS. Such substitution is possible if a nonterminal is defined by a single production
	 * only and the RHS of that production has only one value bearing symbol (i.e. a non-keyword).
	 * Also, if a nonterminal definition includes a second production, which is empty, thus making
	 * the nonterminal optional.
	 * Delegate is a symbol that will be used instead of this nonterminal. 
	 */
	Symbol       delegate;

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
	
	/**
	 * Nonterminal is a value producer if at least one of its defining rules produces a value.
	 */
	boolean isValueProducer()
	{
		return isValueProducer;
	}
	
	boolean isListProducer()
	{
		return isListProducer;
	}
}
