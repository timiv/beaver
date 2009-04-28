package beaver.cc.spec;

public class TermTextPrecSymbol extends PrecSymbol {
	public Term text;

	TermTextPrecSymbol(Term text) {
		this.text = text;
	}

	public boolean equals(PrecSymbol precSymbol) {
		return precSymbol instanceof TermTextPrecSymbol && equals((TermTextPrecSymbol) precSymbol);
	}

	public boolean equals(TermTextPrecSymbol termTextPrecSymbol) {
		return text.equals(termTextPrecSymbol.text);
	}

	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
