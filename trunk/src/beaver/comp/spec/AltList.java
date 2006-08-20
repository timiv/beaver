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
	
	public Alt first()
	{
		return (Alt) super.root.next;
	}
	
	public Alt next(Alt i)
	{
		return i.next == super.root ? null : (Alt) i.next;
	}
}
