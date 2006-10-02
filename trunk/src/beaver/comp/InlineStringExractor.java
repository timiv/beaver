/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp;

import beaver.comp.spec.ItemString;
import beaver.comp.spec.TreeWalker;

/**
 * @author Alexander Demenchuk
 *
 */
public class InlineStringExractor extends TreeWalker
{
	public void visit(ItemString node)
	{
		// remove double-quotes around the value returned by the scanner
		node.text.text = node.text.text.substring(1, node.text.text.length() - 1);
	}
}
