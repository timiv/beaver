/**
 * Beaver: compiler front-end construction toolkit
 * Copyright (c) 2007 Alexander Demenchuk <alder@softanvil.com>
 * All rights reserved.
 *
 * See the file "LICENSE" for the terms and conditions for copying,
 * distribution and modification of Beaver.
 */
package beaver.comp.spec;

/**
 * @author <a href="http://beaver.sourceforge.net">Beaver</a> parser generator
 * @author Alexander Demenchuk
 */
public class ItemSymbol extends Item
{
	public Term ref;
	public Term oper;
	public Term name;

	public ItemSymbol(Term name, Term oper)
	{
		this.name = name;
		this.oper = oper;
	}

	public ItemSymbol(Term ref, Term name, Term oper)
	{
		this.ref  = ref;
		this.name = name;
		this.oper = oper;
	}
	
	public ItemSymbol(Term symName)
	{
		this.name = symName;
	}

	public void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
	
	public boolean equals(Item i)
	{
		if ( i instanceof ItemSymbol )
		{
			ItemSymbol s = (ItemSymbol) i;
			
			return name.equals(s.name)
			    && (oper == null && s.oper == null 
			    	|| 
			    	oper != null && s.oper != null && oper.equals(s.oper)
			       );
		}
		return false;
	}
}
