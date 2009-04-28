package beaver.cc.spec;

public abstract class CharExprQuantifier {
	public abstract boolean equals(CharExprQuantifier charExprQuantifier);
	abstract void dispatch(NodeVisitor visitor);
}
