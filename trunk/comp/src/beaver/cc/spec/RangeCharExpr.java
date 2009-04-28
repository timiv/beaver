package beaver.cc.spec;

public class RangeCharExpr extends CharExpr {
	public RangeExpr rangeExpr;

	RangeCharExpr(RangeExpr rangeExpr) {
		this.rangeExpr = rangeExpr;
	}

	public boolean equals(CharExpr charExpr) {
		return charExpr instanceof RangeCharExpr && equals((RangeCharExpr) charExpr);
	}

	public boolean equals(RangeCharExpr rangeCharExpr) {
		return rangeExpr.equals(rangeCharExpr.rangeExpr);
	}

	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
