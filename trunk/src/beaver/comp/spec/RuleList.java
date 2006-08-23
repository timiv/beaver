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

	RuleList()
	{
		super();
	}
	
	public RuleList add(Rule rule)
	{
		return (RuleList) super.add(rule);
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
