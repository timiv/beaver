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
 * Exception that parser throws when it cannot recover from the syntax error.
 * 
 * @author Alexander Demenchuk
 */
public class SyntaxErrorException extends Exception
{
	SyntaxErrorException()
	{
		super("unexpected token");
	}
	
	SyntaxErrorException(String msg)
	{
		super(msg);
	}
	
	private static final long serialVersionUID = 2431065657267491348L;
}
