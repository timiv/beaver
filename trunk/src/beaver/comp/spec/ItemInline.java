/**
 * 
 */
package beaver.comp.spec;

/**
 * @author Alexander Demenchuk
 *
 */
public class ItemInline extends Item
{
	Term ref;
	ItemList def;
	Term close;
	
	ItemInline(Term ref, ItemList def, Term close)
	{
		this.ref = ref;
		this.def = def;
		this.close = close;
	}
}
