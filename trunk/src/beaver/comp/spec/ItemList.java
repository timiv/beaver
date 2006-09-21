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
	public ItemList()
	{
		super();
	}

	public ItemList(Item item)
	{
		super(item);
	}

	public ItemList add(Item item)
	{
		return (ItemList) super.add(item);
	}
	
	public Item first()
	{
		return super.root.next != super.root ? (Item) super.root.next : null;
	}
	
	public Item next(Item i)
	{
		return i.next != super.root ? (Item) i.next : null;
	}
}
