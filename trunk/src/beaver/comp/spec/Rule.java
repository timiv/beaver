/**
 * 
 */
package beaver.comp.spec;

/**
 * @author Alexander Demenchuk
 *
 */
public class Rule extends Node
{
	public Term name;
	public AltList alts;
	
	public Rule(Term name, AltList alts)
	{
		this.name = name;
		this.alts = alts;
	}
	
	void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
