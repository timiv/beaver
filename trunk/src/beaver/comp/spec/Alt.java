/**
 * 
 */
package beaver.comp.spec;

/**
 * @author Alexander Demenchuk
 *
 */
public class Alt extends Node
{
	Term name;
	ItemList def;
	
	Alt(Term name, ItemList def)
	{
		this.name = name;
		this.def = def;
	}
	
	Alt(ItemList def)
	{
		this.def = def;
	}
	
	void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
