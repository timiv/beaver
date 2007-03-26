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
public class PrecItemList extends beaver.util.NodeList
{
	public PrecItemList()
	{
	}

	public PrecItemList add(PrecItem item)
	{
		return (PrecItemList) super.add(item);
	}

	public void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
