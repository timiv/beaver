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
 */
public class PrecItemRule extends PrecItem
{
	public Term name;

	public PrecItemRule(Term name)
	{
		this.name = name;
	}

	public void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
