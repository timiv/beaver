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
public class ItemSymbol extends Item
{
	public Term refName;
	public Term symName;
	public Term operator;
	
	public ItemSymbol(Term refName, Term symName, Term operator)
	{
		this.refName = refName;
		this.symName = symName;
		this.operator = operator;
	}

	public ItemSymbol(Term symName, Term operator)
	{
		this.symName = symName;
		this.operator = operator;
	}
	
	public ItemSymbol(Term symName)
	{
		this.symName = symName;
	}
	
	void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
