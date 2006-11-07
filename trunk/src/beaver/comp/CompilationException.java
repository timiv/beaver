/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */

package beaver.comp;

public class CompilationException extends java.lang.Exception
{
	CompilationException(String message)
	{
		super(message);
	}
	
    private static final long serialVersionUID = 6692714053366863634L;
}