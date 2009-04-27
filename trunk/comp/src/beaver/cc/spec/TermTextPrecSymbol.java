package beaver.cc.spec;

public class TermTextPrecSymbol extends PrecSymbol {
	public Term text;

	TermTextPrecSymbol(Term text) {
		this.text = text;
	}
	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
