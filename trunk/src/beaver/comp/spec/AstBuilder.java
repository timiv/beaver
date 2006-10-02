/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp.spec;


/**
 * @author Alexander Demenchuk
 *
 */
public class AstBuilder extends SpecParser
{
	protected Spec onSpec(RuleList rules)
	{
		return new Spec(rules);
	}
	
	protected Rule onRule(Term name, AltList alts)
	{
		return new Rule(name, alts);
	}
	
	protected Alt  onAlt(ItemList rhs)
	{
		return new Alt(rhs);
	}
	
	protected Alt  onAlt(Term name, ItemList rhs)
	{
		return new Alt(name, rhs);
	}
	
	protected Item onItemString(Term text)
	{
		return new ItemString(text);
	}
	
	protected Item onItemSymbol(Term name, Term operator)
	{
		return new ItemSymbol(name, operator);
	}
	
	protected Item onItemSymbol(Term ref, Term name, Term operator)
	{
		return new ItemSymbol(ref, name, operator);
	}
	
	protected Item onItemInline(Term ref, ItemList def, Term operator)
	{
		return new ItemInline(ref, def, operator);
	}
	
	protected RuleList onRuleList(Rule item)
	{
		return new RuleList(item);
	}
	
	protected RuleList onRuleList(RuleList list, Rule item)
	{
		return list.add(item);
	}

	protected AltList onAltList(Alt item)
	{
		return new AltList(item);
	}
	
	protected AltList onAltList(AltList list, Alt item)
	{
		return list.add(item);
	}

	protected ItemList onItemList(Item item)
	{
		return new ItemList(item);
	}
	
	protected ItemList onItemList(ItemList list, Item item)
	{
		return list.add(item);
	}

	protected Object makeTerm(Object value)
	{
		return new Term((String) value);
	}	
}
