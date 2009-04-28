package beaver.cc.spec;

public abstract class CharExpr {
	public abstract boolean equals(CharExpr charExpr);
	abstract void dispatch(NodeVisitor visitor);
}
