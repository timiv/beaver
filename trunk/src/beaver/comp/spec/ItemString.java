/**
 * 
 */
package beaver.comp.spec;

/**
 * @author Alexander Demenchuk
 *
 */
public class ItemString extends Item
{
	public Term text;
	
	public ItemString(Term text)
	{		
		this.text = text;
	}
	
	void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
