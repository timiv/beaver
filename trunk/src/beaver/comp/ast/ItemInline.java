/**
* Beaver: compiler front-end construction toolkit
* Copyright (c) 2007 Alexander Demenchuk <alder@softanvil.com>
* All rights reserved.
*
* See the file "LICENSE" for the terms and conditions for copying,
* distribution and modification of Beaver.
*/
package beaver.comp.ast;

/**
 * @author <a href="http://beaver.sourceforge.net">Beaver</a> parser generator
 */
public class ItemInline extends Item
{
	public ItemList itemList;

	public ItemInline(ItemList itemList)
	{
		this.itemList = itemList;
	}

	public void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
	
	public Item makeClone()
	{
		ItemList newList = new ItemList();
		for ( Item item = (Item) itemList.first(); item != null; item = (Item) item.next() )
		{
			newList.add(item.makeClone());
		}
		return new ItemInline(newList);
	}
}
