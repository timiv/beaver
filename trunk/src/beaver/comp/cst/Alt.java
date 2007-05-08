/**
* Beaver: compiler front-end construction toolkit
* Copyright (c) 2007 Alexander Demenchuk <alder@softanvil.com>
* All rights reserved.
*
* See the file "LICENSE" for the terms and conditions for copying,
* distribution and modification of Beaver.
*/
package beaver.comp.cst;

/**
 * @author <a href="http://beaver.sourceforge.net">Beaver</a> parser generator
 * @author Alexander Demenchuk
 */
public class Alt extends beaver.util.Node
{
	public Term     name;
	public ItemList itemList;

	public Alt(ItemList itemList)
	{
		this.itemList = itemList;
	}

	public Alt(Term name, ItemList itemList)
	{
		this.name     = name;
		this.itemList = itemList;
	}

	public void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}

	public boolean equals(Alt alt)
	{
		return itemList.equals(alt.itemList);
	}
}
