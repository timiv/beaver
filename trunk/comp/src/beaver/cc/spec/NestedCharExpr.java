package beaver.cc.spec;

public class NestedCharExpr extends CharExpr {
	public AltRegExprList altRegExprList;

	NestedCharExpr(AltRegExprList altRegExprList) {
		this.altRegExprList = altRegExprList;
	}

	public boolean equals(CharExpr charExpr) {
		return charExpr instanceof NestedCharExpr && equals((NestedCharExpr) charExpr);
	}

	public boolean equals(NestedCharExpr nestedCharExpr) {
		return altRegExprList.equals(nestedCharExpr.altRegExprList);
	}

	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
