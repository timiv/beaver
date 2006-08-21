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
	Term refName;
	Term symName;
	Term operator;
	
	ItemSymbol(Term refName, Term symName, Term operator)
	{
		this.refName = refName;
		this.symName = symName;
		this.operator = operator;
	}

	ItemSymbol(Term symName, Term operator)
	{
		this.symName = symName;
		this.operator = operator;
	}
	
	void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
