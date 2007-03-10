/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp.parser;

/**
 * Productions
 * 
 * @author Alexander Demenchuk
 */
public class Production
{
	/**
	 * Rule ID.
	 */
	char id;
	
	/**
	 * Production precedence - either explicit or derived from its rightmost RHS terminal.
	 */
	char precedence;
	
	/**
	 * Rule name. Full name is created by joining LHS non-terminal name and the rule name.
	 */
	String name;
	
	/**
	 * Non-terminal that this production defines.
	 */
	NonTerminal lhs;
	
	/**
	 * Sequence of symbols that defines lhs non-terminal. 
	 */
	RHSItem[] rhs;
	
	public Production(char id, String name, NonTerminal lhs, RHSItem[] rhs)
	{
		this.id   = id;
		this.lhs  = lhs;
		this.name = name;
		this.rhs  = rhs;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setPrecedence(char prec)
	{
		precedence = prec;
	}
	
	/**
	 * Production can match an empty string only if all its RHS symbols also
	 * match an empty string. 
	 * 
	 * @return true if the production matches an empty string
	 */
	boolean matchesEmptyString()
	{
		for (int i = 0; i < rhs.length; i++)
        {
			if ( !rhs[i].symbol.matchesEmptyString() )
				return false;
        }
		return true;
	}

	public String toString()
	{
		StringBuffer text = new StringBuffer();
		text.append(lhs.name)
			.append(" =");
		for (int i = 0; i < rhs.length; i++)
        {
			text.append(' ').append(rhs[i].symbol.getRepresentation());
        }
		return text.toString();
	}
	
	public static class RHSItem
	{
		String name;
		Symbol symbol;
		
		public RHSItem(String name, Symbol symbol)
		{
			this.name = name;
			this.symbol = symbol;
		}
		
		public RHSItem(Symbol symbol)
		{
			this.symbol = symbol;
		}
	}
}
