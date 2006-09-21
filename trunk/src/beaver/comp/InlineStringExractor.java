/**
 * 
 */
package beaver.comp;

import beaver.comp.spec.ItemString;
import beaver.comp.spec.TreeWalker;

/**
 * @author Alexander Demenchuk
 *
 */
public class InlineStringExractor extends TreeWalker
{
	public void visit(ItemString node)
	{
		// remove double-quotes around the value returned by the scanner
		node.text.text = node.text.text.substring(1, node.text.text.length() - 1);
	}
}
