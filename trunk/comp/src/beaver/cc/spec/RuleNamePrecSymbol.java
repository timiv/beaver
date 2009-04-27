package beaver.cc.spec;

public class RuleNamePrecSymbol extends PrecSymbol {
	public Term id;

	RuleNamePrecSymbol(Term id) {
		this.id = id;
	}
	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
