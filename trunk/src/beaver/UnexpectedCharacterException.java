/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver;

/**
 * Exception that is used by a Scanner to signal abount an encounter of unexpected characters.
 * This exception is only meaningful if an application uses Scanner directly (without a Parser),
 * as Parsers intercept it, convert to a callback and continue calling parser hoping to get eventually
 * to recognizable tokens.
 * 
 * @author Alexander Demenchuk
 */
public class UnexpectedCharacterException extends Exception
{
	private static final long serialVersionUID = 3026616579354239715L;

	public UnexpectedCharacterException() { super(); }
}