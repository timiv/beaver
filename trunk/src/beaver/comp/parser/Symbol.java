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
 * Represents symbols of a grammar.
 * 
 * @author Alexander Demenchuk
 */
public abstract class Symbol
{
	/**
	 * Symbol's ID
	 */
	char id;
	
	/**
	 * String that is used to reference this symbol in the specification.
	 */
	String name;
	
	protected Symbol(char id, String name)
	{
		this.id   = id;
		this.name = name;
	}
	
	public char getId()
	{
		return id;
	}
	
	public String getName()
	{
		return name;
	}

	/**
	 * Text representing this symbol in some output.
	 * This text is stored in parsing tables and available to
	 * parsers for error reporting.
	 */
	public String getRepresentation()
	{
		return name;
	}
	
	/**
	 * Informs the caller if this symbols can match an empty string.
	 * This method always return false for terminals. For non-terminals
	 * result depends on whether on of their derivation rules can match
	 * an empty string.
	 * 
	 * @return true if symbol can match an empty string
	 */
	abstract boolean matchesEmptyString();

}
