/**
 * 
 */
package beaver.comp.spec;

/**
 * @author Alexander Demenchuk
 *
 */
public class ItemList extends NodeList
{
	ItemList(Item item)
	{
		super(item);
	}
	
	public Item first()
	{
		return (Item) super.root.next;
	}
	
	public Item next(Item i)
	{
		return i.next == super.root ? null : (Item) i.next;
	}
}
