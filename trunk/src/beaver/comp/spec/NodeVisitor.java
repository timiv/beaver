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
public abstract class NodeVisitor
{
	public abstract void visit(Spec node);
	public abstract void visit(Rule node);
	public abstract void visit(Alt  node);
	public abstract void visit(ItemInline node);
	public abstract void visit(ItemString node);
	public abstract void visit(ItemSymbol node);
}
