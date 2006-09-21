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
	public AltList()
	{
		super();
	}

	public AltList(Alt item)
	{
		super(item);
	}
	
	public AltList add(Alt alt)
	{
		return (AltList) super.add(alt);
	}
	
	public Alt first()
	{
		return super.root.next != super.root ? (Alt) super.root.next : null;
	}
	
	public Alt next(Alt i)
	{
		return i.next != super.root ? (Alt) i.next : null;
	}
}
