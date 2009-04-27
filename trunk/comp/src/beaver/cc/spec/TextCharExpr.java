package beaver.cc.spec;

public class TextCharExpr extends CharExpr {
	public Term text;

	TextCharExpr(Term text) {
		this.text = text;
	}
	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
