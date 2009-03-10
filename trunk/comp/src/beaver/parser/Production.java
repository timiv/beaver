package beaver.parser;

import java.util.Collection;
import java.util.HashSet;

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
	
	/**
	 * Production is a value producer if it matches at least one symbol that is a value
	 * producer.
	 */
	boolean      isValueProducer;

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
	
	/**
	 * Production is a value producer if its RHS has at least one symbol that returns a value (i.e.
	 * it's not a keyword symbol)
	 */
	boolean isValueProducer()
	{
		return isValueProducer;
	}
	
	boolean markValueProducer()
	{
		for (int i = 0; i < rhs.length; i++)
		{
			if (rhs[i].symbol.isValueProducer())
			{
				return (isValueProducer = true);
			}
		}
		return false;
	}
	
	/**
	 * If this rule produces a value and that value is derived from the single RHS symbol, this
	 * methods will return that symbol.
	 * 
	 * @return RHS symbol that carries this rule value or null
	 */
	Symbol findSingleValue()
	{
		Symbol sym = null;
		for (int i = 0; i < rhs.length; i++)
		{
			if (rhs[i].symbol.isValueProducer())
			{
				if (sym != null)
				{
					// this rule create new symbol that represents a record of multiple values
					return null;
				}
				sym = rhs[i].symbol;
			}
		}
		return sym;
	}
	
	void findRhsValueProducers()
	{
        Collection names = new HashSet();
	    for (int i = 0; i < rhs.length; i++)
        {
            RHSElement arg = rhs[i];
            if (arg.symbol.isValueProducer())
            {
                String argType, argName;
                if (arg.symbol instanceof Terminal)
                {
                	argType = "Term";
                	argName = arg.name != null ? arg.name : arg.symbol.name.toLowerCase();  
                }
                else
                {
                	Nonterminal ntArg = (Nonterminal) arg.symbol;
                	if (ntArg.delegate == null)
                	{
                		argType = ntArg.name;
                	}
                	else if (ntArg.delegate instanceof Terminal)
                	{
                		argType = "Term";
                	}
                	else
                	{
                		argType = ntArg.delegate.name;
                	}
                	argName = arg.name != null ? arg.name : Character.toLowerCase(ntArg.name.charAt(0)) + ntArg.name.substring(1); 
                }
                String nameProbe = argName;
                int argNameCount = 1;
                while (names.contains(nameProbe))
                {
                	nameProbe = argName + Integer.toString(++argNameCount); 
                }
                names.add(argName = nameProbe);
    
                arg.fieldType = argType;
                arg.fieldName = argName;
            }
        }
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
		String fieldType;
		String fieldName;

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
