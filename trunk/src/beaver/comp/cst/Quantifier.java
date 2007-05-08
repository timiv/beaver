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
public class Quantifier extends beaver.util.Node
{
	public NumTerm min;
	public NumTerm max;

	public Quantifier(NumTerm min)
	{
		this.min = min;
		this.max = min;
	}

	public Quantifier(NumTerm min, NumTerm max)
	{
		this.min = min;
		this.max = max;
	}

	public void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
