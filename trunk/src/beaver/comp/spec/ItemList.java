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
	
	Item first()
	{
		return (Item) this.next;
	}
}
