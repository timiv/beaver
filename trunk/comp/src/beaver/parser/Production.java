package beaver.parser;

class Production
{
	/**
	 * Rule ID.
	 */
	int          id;

	/**
	 * Production precedence - either explicit or derived from its rightmost RHS terminal.
	 */
	char         precedence;

	/**
	 * Rule name. Full name is created by joining LHS non-terminal name and the rule name.
	 */
	String       name;

	/**
	 * Non-terminal that this production defines.
	 */
	Nonterminal  lhs;

	/**
	 * Sequence of symbols that defines LHS non-terminal.
	 */
	RHSElement[] rhs;

	Production(String name, Nonterminal lhs, RHSElement[] rhs)
	{
		this.name = name;
		this.lhs = lhs;
		this.rhs = rhs;
	}

	Production(Nonterminal lhs, RHSElement[] rhs)
	{
		this(null, lhs, rhs);
	}
	
	/**
	 * Production can match an empty string only if all its RHS symbols also match an empty string.
	 * 
	 * @return true if the production matches an empty string
	 */
	boolean isNullable()
	{
		for (int i = 0; i < rhs.length; i++)
		{
			if (!rhs[i].symbol.isNullable())
				return false;
		}
		return true;
	}
	
	String getFullName()
	{
		return name == null ? lhs.name : this.name + lhs.name; 
	}
	
	public String toString()
	{
		String repr = lhs + " =";
		if (name != null)
		{
			repr += " { " + name + " }"; 
		}
		for (int i = 0; i < rhs.length; i++)
		{
			repr += " " + rhs[i];
		}
		return repr;
	}

	static class RHSElement
	{
		String name;
		Symbol symbol;

		RHSElement(String name, Symbol symbol)
		{
			this.name = name;
			this.symbol = symbol;
		}

		RHSElement(Symbol symbol)
		{
			this.symbol = symbol;
		}
		
		public String toString()
		{
			return name != null ? name + ":" + symbol : symbol.toString();
		}
	}
}
