package beaver.cc.spec;

public class TextCharExpr extends CharExpr {
	public Term text;

	TextCharExpr(Term text) {
		this.text = text;
	}

	public boolean equals(CharExpr charExpr) {
		return charExpr instanceof TextCharExpr && equals((TextCharExpr) charExpr);
	}

	public boolean equals(TextCharExpr textCharExpr) {
		return text.equals(textCharExpr.text);
	}

	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
