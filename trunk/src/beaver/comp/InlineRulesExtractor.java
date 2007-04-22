/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp;

import beaver.comp.ast.Alt;
import beaver.comp.ast.AltList;
import beaver.comp.ast.Item;
import beaver.comp.ast.ItemInline;
import beaver.comp.ast.ItemList;
import beaver.comp.ast.TreeWalker;


/**
 * @author Alexander Demenchuk
 *
 */
public class InlineRulesExtractor extends TreeWalker
{
	private AltList ruleAlternateProductions;
	private Alt currentProduction;
	
    public void visit(AltList list)
    {
    	ruleAlternateProductions = list;
	    super.visit(list);
    }

    public void visit(Alt node)
    {
    	currentProduction = node;
	    super.visit(node);
    }

	public void visit(ItemInline opt)
	{
		ItemList newList = new ItemList();
		
		Item item = (Item) currentProduction.itemList.first();
		for ( ; item != opt; item = (Item) item.next() )
		{
			newList.add(item.makeClone());
		}
		
		ItemList list = opt.itemList;
		for ( Item i = (Item) list.first(); i != null; i = (Item) list.first() ) 
		{
			list.remove(i);
			newList.add(i);
		}

		for ( item = (Item) item.next() ; item != null; item = (Item) item.next() )
		{
			newList.add(item.makeClone());
		}
		ruleAlternateProductions.add(new Alt(currentProduction.name, newList));
		
		currentProduction.itemList.remove(opt);
	}
}
