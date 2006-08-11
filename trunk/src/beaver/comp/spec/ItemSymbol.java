/**
 * 
 */
package beaver.comp.spec;

/**
 * @author Alexander Demenchuk
 *
 */
public class ItemSymbol extends Item
{
	Term ref;
	Term name;
	Term close;
	
	ItemSymbol(Term ref, Term name, Term close)
	{
		this.ref = ref;
		this.name = name;
		this.close = close;
	}

	ItemSymbol(Term name, Term close)
	{
		this.name = name;
		this.close = close;
	}
}
