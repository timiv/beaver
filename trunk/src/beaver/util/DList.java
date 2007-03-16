/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
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
public class DList implements IList
{
	private final Root root;
	private int length;
	
	public DList()
	{
		root = new Root();
		root.next = root.prev = root;
	}
	
	public int length()
	{
		return length;
	}
	
	public void add(IList.Element elem)
	{
		root.prev = (elem.prev = (elem.next = root).prev).next = elem;
		length++;
	}
	
	public void remove(IList.Element elem)
	{
		elem.prev.next = elem.next;
		elem.next.prev = elem.prev;
		length--;
	}
	
	public IList.Element first()
	{
		return length > 0 ? root.next : null; 
	}
	
	private static class Root extends IList.Element
	{
		protected boolean end()
		{
			return true;
		}
	}
}
