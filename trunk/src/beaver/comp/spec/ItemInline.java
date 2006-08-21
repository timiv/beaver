/**
 * 
 */
package beaver.comp.spec;

/**
 * @author Alexander Demenchuk
 *
 */
public class ItemInline extends Item
{
	Term refName;
	ItemList def;
	Term operator;
	
	ItemInline(Term ref, ItemList def, Term operator)
	{
		this.refName = ref;
		this.def = def;
		this.operator = operator;
	}
	
	void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
