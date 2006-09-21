/**
 * 
 */
package beaver.comp.spec;

/**
 * @author Alexander Demenchuk
 *
 */
public class ItemSymbol extends Item
{
	public Term refName;
	public Term symName;
	public Term operator;
	
	public ItemSymbol(Term refName, Term symName, Term operator)
	{
		this.refName = refName;
		this.symName = symName;
		this.operator = operator;
	}

	public ItemSymbol(Term symName, Term operator)
	{
		this.symName = symName;
		this.operator = operator;
	}
	
	void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
