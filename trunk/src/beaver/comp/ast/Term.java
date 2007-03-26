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
public class Term extends beaver.util.Node
{
	public String text;

	public Term(String text)
	{
		this.text = text;
	}

	public String toString()
	{
		return text;
	}

	public void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
	
	public boolean equals(Term t)
	{
		return text.equals(t.text);
	}
	
	public boolean equals(String s)
	{
		return text.equals(s);
	}
}
