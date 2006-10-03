/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
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
	 * Next definition of LHS non-terminal.
	 */
	public Production next;
	
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
	 * First element in the list of elements that define a LHS non-terminal. 
	 */
	RHSElement rhs;
	
	public Production(char id, String name, NonTerminal lhs, RHSElement rhs)
	{
		this.id   = id;
		this.lhs  = lhs;
		this.name = name;
		this.rhs  = rhs;
	}
	
	/**
	 * Represents an element on the RHS of a production.
	 * 
	 * @author Alexander Demenchuk
	 */
	public static class RHSElement
	{
		/**
		 * Next element in a singly linked list of RHS elements.
		 */
		public RHSElement next;
		
		/**
		 * RHS element name. This creates a reference in a semantic action callback.
		 */
		String name;
		
		/**
		 * Symbol
		 */
		Symbol sym;
		
		public RHSElement(String name, Symbol sym)
		{
			this.name = name;
			this.sym = sym;
		}
		
	}
	
}
