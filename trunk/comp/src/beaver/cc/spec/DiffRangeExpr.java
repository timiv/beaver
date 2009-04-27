package beaver.cc.spec;

public class DiffRangeExpr extends RangeExpr {
	public RangeExpr min;
	public RangeExpr sub;

	DiffRangeExpr(RangeExpr min, RangeExpr sub) {
		this.min = min;
		this.sub = sub;
	}
	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
