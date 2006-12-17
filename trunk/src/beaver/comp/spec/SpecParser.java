/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp.spec;

import java.io.DataInputStream;
import java.io.IOException;

import beaver.Parser;
import beaver.ParsingTables;
import beaver.Symbol;

/**
 * @author Alexander Demenchuk
 *
 */
public abstract class SpecParser extends Parser
{
	protected SpecParser()
	{
		super(tables);
	}
	
	protected abstract Spec onSpec (RuleList list);
	protected abstract Rule onRule (Term name, AltList alts);
	protected abstract Alt  onAlt  (ItemList rhs);
	protected abstract Alt  onAlt  (Term name, ItemList rhs);	
	protected abstract Item onItemString (Term text);
	protected abstract Item onItemSymbol (Term name, Term oper);
	protected abstract Item onItemSymbol (Term ref, Term name, Term oper);
	protected abstract Item onItemInline (Term ref, ItemList def, Term oper);

	protected abstract RuleList onRuleList (Rule item);
	protected abstract RuleList onRuleList (RuleList list, Rule item);
	protected abstract AltList  onAltList  (Alt item);
	protected abstract AltList  onAltList  (AltList list, Alt item);
	protected abstract ItemList onItemList (Item item);
	protected abstract ItemList onItemList (ItemList list, Item item);
	
	/** 
	 * @see beaver.Parser#reduce(beaver.Symbol[], int, int)
	 */
	protected Symbol reduce(Symbol[] symbols, int at, int rule)
	{
		switch (rule)
		{
			case  0: // Spec = "%rules" RuleList
			{
				RuleList list = (RuleList) symbols[at - 1].getValue();
				
				return symbol( onSpec(list) );
			}
			case  1: // Rule = NAME "=" AltList ";"
			{
				Term    name = (Term)    symbols[at].getValue();
				AltList alts = (AltList) symbols[at - 2].getValue();
				
				return symbol( onRule(name, alts) );
			}
			case  2: // AltList = Alt
			{
				Alt item = (Alt) symbols[at].getValue();
				
				return symbol( onAltList(item) );				
			}
			case  3: // AltList = AltList "|" Alt
			{
				AltList list = (AltList) symbols[at].getValue();
				Alt     item = (Alt)     symbols[at - 2].getValue();
				
				return symbol( onAltList(list, item) );				
			}
			case  4: // Alt = OptItemList
			{
				ItemList list = (ItemList) symbols[at].getValue();
				return symbol( onAlt(list) );				
			}
			case  5: // Alt = "{" NAME "}" OptItemList
			{
				Term    name = (Term)     symbols[at - 1].getValue();
				ItemList rhs = (ItemList) symbols[at - 3].getValue();
				
				return symbol( onAlt(name, rhs) );                				
			}
			case  6: // Item = STRING
			{
				Term text = (Term) symbols[at].getValue();
				
				return symbol( onItemString(text) );				
			}
			case  7: // Item = NAME OptOP
			{
				Term name = (Term) symbols[at].getValue();
				Term oper = (Term) symbols[at - 1].getValue();
				return symbol( onItemSymbol(name, oper) );
			}
			case  8: // Item = NAME ":" NAME OptOP
			{
				Term ref  = (Term) symbols[at].getValue();
				Term name = (Term) symbols[at - 2].getValue();
				Term oper = (Term) symbols[at - 3].getValue();
				
				return symbol( onItemSymbol(ref, name, oper) );
			}
			case  9: // Item = NAME ":" "(" ItemList ")" OP
			{
				Term     ref = (Term)     symbols[at].getValue();
				ItemList def = (ItemList) symbols[at - 3].getValue();
				Term    oper = (Term)     symbols[at - 5].getValue();
				
				return symbol( onItemInline(ref, def, oper) );				
			}
			case 10: // RuleList = Rule
			{
				Rule item = (Rule) symbols[at].getValue();
				
				return symbol( onRuleList(item) );
			}
			case 11: // RuleList = RuleList Rule
			{
				RuleList list = (RuleList) symbols[at].getValue();
				Rule     item = (Rule)     symbols[at - 1].getValue();

				return symbol(onRuleList(list, item));				
			}
			case 12: // ItemList = Item
			{
				Item item = (Item) symbols[at].getValue();
				
				return symbol( onItemList(item) );				
			}
			case 13: // ItemList = ItemList Item
			{
				ItemList list = (ItemList) symbols[at].getValue();
				Item     item = (Item)     symbols[at - 1].getValue();
				
				return symbol( onItemList(list, item) );				
			}
			case 14: // OptItemList =
			{
				return symbol( null );
			}
			case 15: // OptItemList = ItemList
			{
				return copy(symbols[at]);
			}
			case 16: // OptOP = 
			{
				return symbol( null );
			}
			case 17: // OptOP = OP
			{
				return copy(symbols[at]);
			}
		}
		throw new IllegalArgumentException("unknown production #" + rule);
	}

	private static final ParsingTables tables;
	
	static {
		try
		{
			tables = new ParsingTables(new DataInputStream(SpecParser.class.getResourceAsStream("SpecParser.tables")));
		}
		catch (IOException _)
		{
			throw new IllegalStateException("cannot load parsing tables");
		}
	}
}
