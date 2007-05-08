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
public class NumTerm extends beaver.util.Node
{
	public int value;

	public NumTerm(String text)
	{
		this.value = Integer.parseInt(text);
	}

	public String toString()
	{
		return String.valueOf(value);
	}

	public void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
