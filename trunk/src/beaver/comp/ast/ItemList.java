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
 * @author Alexander Demenchuk
 */
public class ItemList extends beaver.util.NodeList
{
	public ItemList()
	{
	}

	public ItemList add(Item item)
	{
		return (ItemList) super.add(item);
	}

	public void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}

	public boolean equals(ItemList list)
	{
		if ( length() != list.length() )
			return false;
		
		for ( Item ia = (Item) list.first(), id = (Item) list.first(); ia != null && id != null; ia = (Item) ia.next(), id = (Item) id.next() )
		{
			if ( !ia.equals(id) )
				return false;
		}
		return true;
	}
}
