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
		Node end = node.rules.end();
		for (Node i = node.rules.first(); i != end; i = i.next)
		{
			((Rule) i).accept(this);
		}
	}

	public void visit(Rule node)
	{
		Node end = node.alts.end();
		for (Node i = node.alts.first(); i != end; i = i.next)
		{
			((Alt) i).accept(this);
		}
	}

	public void visit(Alt node)
	{
		Node end = node.def.end();
		for (Node i = node.def.first(); i != end; i = i.next)
		{
			((Item) i).accept(this);
		}
	}

	public void visit(ItemInline node)
	{
		Node end = node.def.end();
		for (Node i = node.def.first(); i != end; i = i.next)
		{
			((Item) i).accept(this);
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
