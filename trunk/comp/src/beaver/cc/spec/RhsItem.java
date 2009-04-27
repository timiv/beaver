package beaver.cc.spec;

public abstract class RhsItem {
	RhsItem next;

	abstract void dispatch(NodeVisitor visitor);
}
