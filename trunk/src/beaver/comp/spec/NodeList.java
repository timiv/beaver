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
 * List of nodes.
 *  
 * @author Alexander Demenchuk
 */
public class NodeList extends Node
{
	protected final ListElement root = new ListElement();
	private int length;
	
	protected NodeList()
	{
		root.next = root.prev = root;
	}
	
	protected NodeList(Node node)
	{
		root.next = root.prev = node;
		node.next = node.prev = root;
		
		length = 1;
	}
	
	public NodeList add(Node node)
	{
		root.prev = (node.prev = (node.next = root).prev).next = node;
		
		length++;
		
		return this;
	}
	
	public NodeList remove(Node node)
	{
		ListElement prevNode = node.prev;
		ListElement nextNode = node.next;
		
		prevNode.next = nextNode;
		nextNode.prev = prevNode;
		
		length--;
		
		return this;
	}
	
	public int length()
	{
		return length;
	}
}