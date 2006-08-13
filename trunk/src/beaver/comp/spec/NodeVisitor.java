/**
 * 
 */
package beaver.comp.spec;

/**
 * @author Alexander Demenchuk
 *
 */
public abstract class NodeVisitor
{
	public abstract void visit(Spec node);
	public abstract void visit(Rule node);
	public abstract void visit(Alt  node);
	public abstract void visit(ItemInline node);
	public abstract void visit(ItemString node);
	public abstract void visit(ItemSymbol node);
}
