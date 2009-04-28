package beaver.cc.spec;

public abstract class RangeExpr {
	public abstract boolean equals(RangeExpr rangeExpr);
	abstract void dispatch(NodeVisitor visitor);
}
