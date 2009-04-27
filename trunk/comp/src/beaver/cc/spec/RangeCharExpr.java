package beaver.cc.spec;

public class RangeCharExpr extends CharExpr {
	public RangeExpr rangeExpr;

	RangeCharExpr(RangeExpr rangeExpr) {
		this.rangeExpr = rangeExpr;
	}
	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
