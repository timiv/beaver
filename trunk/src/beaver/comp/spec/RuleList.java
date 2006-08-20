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
		return (Rule) super.root.next;
	}
	
	public Rule next(Rule i)
	{
		return i.next == super.root ? null : (Rule) i.next;
	}
}
