/**
 * 
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
	
	protected Item onItemSymbol(Term name, Term close)
	{
		return new ItemSymbol(name, close);
	}
	
	protected Item onItemSymbol(Term ref, Term name, Term close)
	{
		return new ItemSymbol(ref, name, close);
	}
	
	protected Item onItemInline(Term ref, ItemList def, Term close)
	{
		return new ItemInline(ref, def, close);
	}
}
