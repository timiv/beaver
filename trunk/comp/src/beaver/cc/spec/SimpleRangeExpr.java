package beaver.cc.spec;

public class SimpleRangeExpr extends RangeExpr {
	public Term range;

	SimpleRangeExpr(Term range) {
		this.range = range;
	}
	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
