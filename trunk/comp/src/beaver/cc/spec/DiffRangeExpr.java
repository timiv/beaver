package beaver.cc.spec;

public class DiffRangeExpr extends RangeExpr {
	public RangeExpr min;
	public RangeExpr sub;

	DiffRangeExpr(RangeExpr min, RangeExpr sub) {
		this.min = min;
		this.sub = sub;
	}

	public boolean equals(RangeExpr rangeExpr) {
		return rangeExpr instanceof DiffRangeExpr && equals((DiffRangeExpr) rangeExpr);
	}

	public boolean equals(DiffRangeExpr diffRangeExpr) {
		return min.equals(diffRangeExpr.min) && sub.equals(diffRangeExpr.sub);
	}

	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
