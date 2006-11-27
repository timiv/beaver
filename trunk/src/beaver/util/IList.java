/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */

package beaver.util;

/**
 * @author Alexander Demenchuk
 *
 */
public interface IList
{
	int     length ();
	void    add    (Element elem);
	void    remove (Element elem);
	Element first  ();
	
	public static class Element
    {
    	protected Element next;
    	protected Element prev;
    	
    	protected boolean end()
    	{
    		return false;
    	}
    	
    	public Element next()
    	{
    		return next.end() ? null : next;
    	}
    	
        public void replaceWith(Element elem)
        {
        	(elem.next = this.next).prev = (elem.prev = this.prev).next = elem;
        }
    }
}
