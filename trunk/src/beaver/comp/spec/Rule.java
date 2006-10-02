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
public class Rule extends Node
{
	public Term name;
	public AltList alts;
	
	public Rule(Term name, AltList alts)
	{
		this.name = name;
		this.alts = alts;
	}
	
	void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
