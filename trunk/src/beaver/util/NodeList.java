/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.util;

import beaver.util.DList;
import beaver.util.IList;

/**
 * List of nodes.
 *  
 * @author Alexander Demenchuk
 */
public class NodeList extends Node implements IList
{
	protected final DList elements;
	
	protected NodeList()
	{
		elements = new DList();
	}
	
	protected NodeList(Node node)
	{
		elements = new DList(node);
	}
	
	public NodeList add(Node node)
	{
		elements.add(node);
		return this;
	}
	
	public NodeList remove(Node node)
	{
		elements.remove(node);
		return this;
	}
	
	public void add(IList.Element elem)
	{
		elements.add(elem);
	}
	
	public void remove(IList.Element elem)
	{
		elements.remove(elem);
	}
	
	public IList.Element first()
	{
		return elements.first();
	}
	
	public int length()
	{
		return elements.length();
	}
}