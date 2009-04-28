package beaver.cc.spec;

public abstract class RhsItem {
	RhsItem next;

	public abstract boolean equals(RhsItem rhsItem);
	abstract void dispatch(NodeVisitor visitor);
}
