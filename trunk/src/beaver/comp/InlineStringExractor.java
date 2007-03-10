/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp;

import beaver.comp.spec.ItemStatic;
import beaver.comp.spec.TreeWalker;

/**
 * @author Alexander Demenchuk
 *
 */
public class InlineStringExractor extends TreeWalker
{
	public void visit(ItemStatic node)
	{
		// remove double-quotes around the value returned by the scanner
		node.text.text = node.text.text.substring(1, node.text.text.length() - 1);
	}
}
