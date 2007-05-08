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
public class RuleList extends beaver.util.NodeList
{
	public RuleList()
	{
	}

	public RuleList add(Rule item)
	{
		return (RuleList) super.add(item);
	}

	public void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
	
	public Rule find(Alt alt)
	{
		for (Rule rule = (Rule) first(); rule != null; rule = (Rule) rule.next())
		{
			if ( rule.altList.length() == 1 && ((Alt) rule.altList.first()).equals(alt) )
			{
				return rule;
			}
		}
		return null;
	}
	
	public Rule find(String name)
	{
		for (Rule rule = (Rule) first(); rule != null; rule = (Rule) rule.next())
		{
			if ( rule.name.equals(name) )
			{
				return rule;
			}
		}
		return null;
	}
}
