/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */

package beaver.comp;

import java.util.Map;

import beaver.comp.spec.ItemString;
import beaver.comp.spec.ItemSymbol;
import beaver.comp.spec.Term;
import beaver.comp.spec.TreeWalker;

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

	public void visit(ItemString item)
    {
		String newSymbolName = (String) constTokens.get(item.text.text);
		if ( newSymbolName == null )
			throw new IllegalStateException("Text token '" + item.text.text + "' cannot not found in the text tokens dictionary.");
		Term nameTerm = new Term(newSymbolName);
		item.text.copyLocation(nameTerm);
		item.replaceWith(new ItemSymbol(nameTerm));
    }
	
}
