package beaver.cc.spec;

public class MacroRangeExpr extends RangeExpr {
	public Term id;

	MacroRangeExpr(Term id) {
		this.id = id;
	}

	public boolean equals(RangeExpr rangeExpr) {
		return rangeExpr instanceof MacroRangeExpr && equals((MacroRangeExpr) rangeExpr);
	}

	public boolean equals(MacroRangeExpr macroRangeExpr) {
		return id.equals(macroRangeExpr.id);
	}

	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
