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
public class ItemInline extends Item
{
	public Term     refName;
	public ItemList def;
	public Term     operator;
	
	public ItemInline(Term ref, ItemList def, Term operator)
	{
		this.refName = ref;
		this.def = def;
		this.operator = operator;
	}
	
	void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
