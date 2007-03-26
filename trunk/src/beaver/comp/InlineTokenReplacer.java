/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */

package beaver.comp;

import java.util.Map;

import beaver.comp.ast.ItemStatic;
import beaver.comp.ast.ItemSymbol;
import beaver.comp.ast.Term;
import beaver.comp.ast.TreeWalker;

/**
 * @author Alexander Demenchuk
 *
 */
public class InlineTokenReplacer extends TreeWalker
{
	private Map constTokens;
	
	public InlineTokenReplacer(Map constTokens)
	{
		this.constTokens = constTokens;
	}

	public void visit(ItemStatic item)
    {
		String newSymbolName = (String) constTokens.get(item.text.text);
		if ( newSymbolName == null )
			throw new IllegalStateException("Text token '" + item.text.text + "' cannot not found in the text tokens dictionary.");
		Term nameTerm = new Term(newSymbolName);
		item.text.copyLocation(nameTerm);
		item.replaceWith(new ItemSymbol(nameTerm));
    }
	
}
