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
	public Term     name;
	public ItemList def;
	
	public Alt(Term name, ItemList def)
	{
		this.name = name;
		this.def = def;
	}
	
	public Alt(ItemList def)
	{
		this.def = def;
	}
	
	void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
