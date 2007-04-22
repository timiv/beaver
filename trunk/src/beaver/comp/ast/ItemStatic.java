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
public class ItemStatic extends Item
{
	public Term text;

	public ItemStatic(Term text)
	{
		this.text = text;
	}

	public void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
	
	public boolean equals(Item i)
	{
		return i instanceof ItemStatic && ((ItemStatic) i).text.equals(text);
	}
	
	public Item makeClone()
	{
		return new ItemStatic(new Term(text.text));
	}
}
