/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */

package beaver.comp.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;




/**
 * @author Alexander Demenchuk
 *
 */
class ItemSet
{
	private Item firstItem;
	private int  coreSize;
	private int  coreHash;
	
	ItemSet(Item[] coreItems)
	{
		coreSize = coreItems.length;
        Item item = firstItem = coreItems[0];
        for (int n = coreSize; n > 0; n--, item = item.next)
        {
        	coreHash = coreHash * 571 + item.hashCode();
        }
	}
	
	Item getFirstItem()
	{
		return firstItem;
	}
	
	void buildClosure(ItemSet.Builder itemBuilder)
	{
		for ( Item item = firstItem; item != null; item = item.next )
		{
			if ( item.isDotAfterLastSymbol() )
				continue;
			
			Symbol sym = item.getSymbolAfterDot();
			if ( sym instanceof NonTerminal )
			{
				NonTerminal nt = (NonTerminal) sym;
				for (int i = 0; i < nt.derivationRules.length; i++)
                {
	                Item newItem = itemBuilder.getItem(nt.derivationRules[i], 0);
	                if (newItem.addLookaheads(item))
	                {
	                	item.addAcceptor(newItem);
	                }
                }
			}
		}
	}
	
	void copyEmitters(ItemSet other)
	{
		if (other.coreSize != coreSize)
			throw new IllegalArgumentException("unequal cores");

        Item myItem = firstItem, cpItem = other.firstItem;
        for (int n = coreSize; n > 0; n--, myItem = myItem.next, cpItem = cpItem.next)
        {
        	myItem.copyEmitters(cpItem);
        }
	}
	
	void reverseEmitters()
	{
		for ( Item item = firstItem; item != null; item = item.next )
        {
	        item.reverseEmitters();
        }
	}
	
	void resetContributions()
	{
		for ( Item item = firstItem; item != null; item = item.next )
        {
	        item.hasContributed = false;
        }
	}
	
	boolean findLookaheads()
	{
		boolean found = false;
		for ( Item item = firstItem; item != null; item = item.next )
        {
	        if ( !item.hasContributed )
	        {
	        	if ( item.findLookaheads() )
	        	{
	        		found = true;
	        	}
	        	item.hasContributed = true;
	        }
        }
		return found;
	}
	
	public int hashCode()
	{
		return coreHash;
	}
	
	public boolean equals(Object o)
	{
		return o == this || o instanceof ItemSet && this.equals((ItemSet) o);
	}
	
	boolean equals(ItemSet other)
	{
		if (coreSize != other.coreSize)
			return false;
		
        Item myItem = firstItem, cpItem = other.firstItem;
        for (int n = coreSize; n > 0; n--, myItem = myItem.next, cpItem = cpItem.next)
        {
        	if ( !myItem.equals(cpItem) )
        		return false;
        }
        return true;
	}
	
	public String toString()
	{
		StringBuffer text = new StringBuffer();
        Item item = firstItem;
        for (int n = coreSize; n > 0; n--, item = item.next)
        {
			text.append('\t')
				.append(item)
				.append('\n');
        }
		text.append(" >> ");
		for ( ; item != null; item = item.next)
		{
			text.append('\t')
				.append(item)
				.append('\n');
		}
		return text.toString();
	}
	
	static class Builder
    {
    	private Map  items = new HashMap();
    	private Item probe = new Item(null, 0);
    	private Item last;

    	Item getItem(Production rule, int dot)
    	{
    		Item item = (Item) items.get(probe.as(rule, dot));
    		if (item == null)
    		{
    			add(item = new Item(rule, dot));
    		}
    		return item;
    	}
    	
    	Item getItem(Item proto)
    	{
    		Item item = (Item) items.get(proto);
    		if (item == null)
    		{
    			add(item = proto);
    		}
    		return item;
    	}
    	
    	private void add(Item item)
    	{
			items.put(item, item);
			if (last == null)
				last = item;
			else
				last = last.next = item;
    	}
    	
    	Item[] getCore()
    	{
            Item[] core = new Item[items.size()];
            int i = 0;
            for ( Iterator iter = items.values().iterator(); iter.hasNext(); )
            {
    	        core[i++] = (Item) iter.next();
            }
            Arrays.sort(core);
            
            last = core[0];
            for ( i = 1; i < core.length; i++ )
            {
            	last = last.next = core[i];
            }
            last.next = null;
            
    		return core;
    	}
    	
    	Item getLastItem()
    	{
    		return last;
    	}
    	
    	void reset()
    	{
    		items.clear();
    		last = null;
    	}

    }

}
