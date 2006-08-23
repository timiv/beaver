/**
 * 
 */
package beaver.comp.spec;

/**
 * @author Alexander Demenchuk
 *
 */
public class AltList extends NodeList
{
	AltList(Alt item)
	{
		super(item);
	}
	
	AltList()
	{
		super();
	}
	
	public AltList add(Alt alt)
	{
		return (AltList) super.add(alt);
	}
	
	public Alt first()
	{
		return (Alt) super.root.next;
	}
	
	public Alt next(Alt i)
	{
		return i.next == super.root ? null : (Alt) i.next;
	}
}
