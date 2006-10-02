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
 * @author Alexander Demenchuk
 *
 */
public class ItemString extends Item
{
	public Term text;
	
	public ItemString(Term text)
	{		
		this.text = text;
	}
	
	void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
