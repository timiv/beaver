package beaver.cc.spec;

public class RuleNamePrecSymbol extends PrecSymbol {
	public Term id;

	RuleNamePrecSymbol(Term id) {
		this.id = id;
	}

	public boolean equals(PrecSymbol precSymbol) {
		return precSymbol instanceof RuleNamePrecSymbol && equals((RuleNamePrecSymbol) precSymbol);
	}

	public boolean equals(RuleNamePrecSymbol ruleNamePrecSymbol) {
		return id.equals(ruleNamePrecSymbol.id);
	}

	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
