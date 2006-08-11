/**
 * 
 */
package beaver.comp.spec;

/**
 * @author Alexander Demenchuk
 *
 */
public class RuleList extends NodeList
{
	RuleList(Rule item)
	{
		super(item);
	}
	
	public Rule first()
	{
		return (Rule) this.next;
	}
}
