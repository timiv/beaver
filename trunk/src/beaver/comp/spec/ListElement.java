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
 * @author Alexander Demenchuk
 *
 */
public class ListElement
{
	/**
     * Next node in the doubly linked list of nodes
     */
    protected ListElement next;
	/**
     * Previous node in the list
     */
    protected ListElement prev;
    
	/**
     * Replaces this node in the list with the specified one.
     *
     */
    public void replaceWith(ListElement node)
    {
    	(node.next = this.next).prev = (node.prev = this.prev).next = node;
    }
}
