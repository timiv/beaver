/**
 * 
 */
package beaver.comp.spec;

/**
 * @author Alexander Demenchuk
 *
 */
public abstract class Item extends Node
{
	abstract void accept(NodeVisitor visitor);
}
