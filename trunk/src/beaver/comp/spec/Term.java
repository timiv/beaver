/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp.spec;

/**
 * AST node that keeps values of recognized terminals.
 * 
 * @author Alexander Demenchuk
 *
 */
public class Term extends Node
{
	public String text;
	
	public Term(String value)
	{
		text = value;
	}
}
