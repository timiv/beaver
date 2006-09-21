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
	public RuleList()
	{
		super();
	}

	public RuleList(Rule item)
	{
		super(item);
	}

	public RuleList add(Rule rule)
	{
		return (RuleList) super.add(rule);
	}
	
	public Rule first()
	{
		return super.root.next != super.root ? (Rule) super.root.next : null;
	}
	
	public Rule next(Rule i)
	{
		return i.next != super.root ? (Rule) i.next : null;
	}
}
