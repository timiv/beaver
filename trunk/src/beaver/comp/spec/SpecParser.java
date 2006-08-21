/**
 * 
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

	protected RuleList onRuleList(Rule item)
	{
		return new RuleList(item);
	}
	
	protected RuleList onRuleList(RuleList list, Rule item)
	{
		list.add(item);
		return list;
	}

	protected AltList onAltList(Alt item)
	{
		return new AltList(item);
	}
	
	protected AltList onAltList(AltList list, Alt item)
	{
		list.add(item);
		return list;
	}
	
	protected ItemList onItemList(Item item)
	{
		return new ItemList(item);
	}
	
	protected ItemList onItemList(ItemList list, Item item)
	{
		list.add(item);
		return list;
	}
	
	protected Object makeTerm(Object value)
	{
		return new Term((String) value);
	}
	
	/** 
	 * @see beaver.Parser#reduce(beaver.Symbol[], int, int)
	 */
	protected Symbol reduce(Symbol[] symbols, int at, int rule)
	{
		switch (rule)
		{
			case  0: // lst$Rule = Rule
			{
				Rule item = (Rule) symbols[at].getValue();
				
				return symbol( onRuleList(item) );
			}
			case  1: // lst$Rule = lst$Rule Rule
			{
				RuleList list = (RuleList) symbols[at].getValue();
				Rule     item = (Rule)     symbols[at - 1].getValue();

				return symbol(onRuleList(list, item));				
			}
			case  2: // Spec = RULES lst$Rule
			{
				RuleList list = (RuleList) symbols[at - 1].getValue();
				
				return symbol( onSpec(list) );
			}
			case  3: // Rule = NAME EQ AltList SEMI
			{
				Term name = (Term) symbols[at].getValue();
				AltList alts = (AltList) symbols[at - 2].getValue();
				
				return symbol( onRule(name, alts) );
			}
			case  4: // AltList = Alt
			{
				Alt item = (Alt) symbols[at].getValue();
				
				return symbol( onAltList(item) );				
			}
			case  5: // AltList = AltList BAR Alt
			{
				AltList list = (AltList) symbols[at].getValue();
				Alt     item = (Alt) symbols[at - 2].getValue();
				
				return symbol( onAltList(list, item) );				
			}
			case  6: // lst$Item = Item
			{
				Item item = (Item) symbols[at].getValue();
				
				return symbol( onItemList(item) );				
			}
			case  7: // lst$Item = lst$Item Item
			{
				ItemList list = (ItemList) symbols[at].getValue();
				Item     item = (Item) symbols[at - 1].getValue();
				
				return symbol( onItemList(list, item) );				
			}
			case  8: // opt$lst$Item =
			{
				return symbol(null);
			}
			case  9: // opt$lst$Item = lst$Item
			{
				return copy(symbols[at]);
			}
			case 10: // Alt = opt$lst$Item
			{
				ItemList list = (ItemList) symbols[at].getValue();
				return symbol( onAlt(list) );				
			}
			case 11: // Alt = LCURL NAME RCURL opt$lst$Item
			{
				Term name = (Term) symbols[at - 1].getValue();
				ItemList rhs = (ItemList) symbols[at - 3].getValue();
				
				return symbol( onAlt(name, rhs) );                				
			}
			case 12: // Item = STRING
			{
				Term text = (Term) symbols[at].getValue();
				
				return symbol( onItemString(text) );				
			}
			case 13: // opt$OP = 
			{
				return symbol(null);
			}
			case 14: // opt$OP = OP
			{
				return copy(symbols[at]);
			}
			case 15: // Item = NAME opt$OP
			{
				Term name = (Term) symbols[at].getValue();
				Term oper = (Term) symbols[at - 1].getValue();
				return symbol( onItemSymbol(name, oper) );
			}
			case 16: // Item = NAME COLON NAME opt$OP
			{
				Term ref = (Term) symbols[at].getValue();
				Term name = (Term) symbols[at - 2].getValue();
				Term oper = (Term) symbols[at - 3].getValue();
				
				return symbol( onItemSymbol(ref, name, oper) );
			}
			case 17: // Item = NAME COLON LPAREN lst$Item RPAREN OP
			{
				Term ref = (Term) symbols[at].getValue();
				ItemList def = (ItemList) symbols[at - 3].getValue();
				Term oper = (Term) symbols[at - 5].getValue();
				
				return symbol( onItemInline(ref, def, oper) );				
			}
		}
		throw new IllegalArgumentException("unknown production #" + rule);
	}

	private static final ParsingTables tables;
	
	static {
		try
		{
			tables = ParsingTables.from(new DataInputStream(SpecParser.class.getResourceAsStream("SpecParser.tables")));
		}
		catch (IOException _)
		{
			throw new IllegalStateException("cannot load parsing tables");
		}
	}
}
