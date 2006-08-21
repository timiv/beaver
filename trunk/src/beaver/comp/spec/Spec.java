/**
 * 
 */
package beaver.comp.spec;

/**
 * @author Alexander Demenchuk
 *
 */
public class Spec extends Node
{
	RuleList rules;
	
	Spec(RuleList list)
	{
		rules = list;
	}
	
	public void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
