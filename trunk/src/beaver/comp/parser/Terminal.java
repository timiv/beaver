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
 * Terminals 
 * 
 * @author Alexander Demenchuk
 */
public class Terminal extends Symbol
{
	/**
	 * Precedence: 0 - undefined (lowest), 0xffff - highest
	 */
	char precedence;
	
	/**
	 * 'L' - left, 'R' - right, 'N' - none
	 */
	char associativity;

	public Terminal(char id, String name)
	{
		super(id, name);
	}
	
	public void setPrecedence(char prec, char assoc)
	{
		precedence = prec;
		associativity = assoc;
	}

	/**
	 * Terminal cannot match an empty string.
	 * 
	 *  @return false
	 */
	boolean matchesEmptyString()
	{
		return false;
	}
	
	public static final Terminal EOF = new Terminal((char) 0, "EOF");
	public static final Terminal EOL = new Terminal((char) 1, "EOL");
	
	public static class Const extends Terminal
	{
		String text;
		
		public Const(char id, String name, String text)
		{
			super(id, name);
			this.text = text;
		}
		
		public String getRepresentation()
		{
			return '"' + text + '"';
		}
		
		public String toString()
		{
			return text;
		}
	}
}
