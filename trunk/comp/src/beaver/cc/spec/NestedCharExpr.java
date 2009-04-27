package beaver.cc.spec;

public class NestedCharExpr extends CharExpr {
	public AltRegExprList altRegExprList;

	NestedCharExpr(AltRegExprList altRegExprList) {
		this.altRegExprList = altRegExprList;
	}
	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
