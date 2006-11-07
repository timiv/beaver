/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */

package beaver.comp.parser;


/**
 * @author Alexander Demenchuk
 *
 */
class DLList
{
	protected final Element root;
	private int length;
	
	protected DLList()
	{
		root = new Element();
		root.next = root.prev = root;
	}
	
	void add(Element elem)
	{
		root.prev = (elem.prev = (elem.next = root).prev).next = elem;
		length++;
	}
	
	void remove(Element elem)
	{
		Element prevElem = elem.prev;
		Element nextElem = elem.next;
		
		prevElem.next = nextElem;
		nextElem.prev = prevElem;
		
		length--;
	}
	
	Element getFirstElement()
	{
		return root.next;
	}
	
	Element getNextElement(Element current)
	{
		return current.next == root ? null : current.next; 
	}

	static class Element
	{
		Element next;
		Element prev;
	}
}
