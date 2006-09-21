/**
 * 
 */
package beaver.comp.spec;

/**
 * @author Alexander Demenchuk
 *
 */
public class TreeWalker extends NodeVisitor
{

	public void visit(Spec node)
	{
		RuleList list = node.rules;
		if (list != null)
		{
			for (Rule i = list.first(); i != null; i = list.next(i))
			{
				i.accept(this);
			}
		}
	}

	public void visit(Rule node)
	{
		AltList list = node.alts;
		if (list != null)
		{
			for (Alt i = list.first(); i != null; i = list.next(i))
			{
				i.accept(this);
			}
		}
	}

	public void visit(Alt node)
	{
		ItemList list = node.def;
		if (list != null)
		{
			for (Item i = list.first(); i != null; i = list.next(i))
			{
				i.accept(this);
			}
		}
	}

	public void visit(ItemInline node)
	{
		ItemList list = node.def;
		if (list != null)
		{
			for (Item i = list.first(); i != null; i = list.next(i))
			{
				i.accept(this);
			}
		}
	}

	public void visit(ItemString node)
	{
		// leaf
	}

	public void visit(ItemSymbol node)
	{
		// leaf
	}

}
