package beaver.cc.spec;

public class MacroRangeExpr extends RangeExpr {
	public Term id;

	MacroRangeExpr(Term id) {
		this.id = id;
	}
	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
