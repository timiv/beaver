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
public abstract class Item extends beaver.util.Node
{
	public abstract void accept(NodeVisitor visitor);
	public abstract Item makeClone();

	public boolean equals(Item i)
	{
		return false;
	}
}
