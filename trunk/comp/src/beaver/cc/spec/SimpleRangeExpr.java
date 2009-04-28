package beaver.cc.spec;

public class SimpleRangeExpr extends RangeExpr {
	public Term range;

	SimpleRangeExpr(Term range) {
		this.range = range;
	}

	public boolean equals(RangeExpr rangeExpr) {
		return rangeExpr instanceof SimpleRangeExpr && equals((SimpleRangeExpr) rangeExpr);
	}

	public boolean equals(SimpleRangeExpr simpleRangeExpr) {
		return range.equals(simpleRangeExpr.range);
	}

	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
