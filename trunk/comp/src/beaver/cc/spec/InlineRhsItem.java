package beaver.cc.spec;

public class InlineRhsItem extends RhsItem {
	public Term ref;
	public Term prefix;
	public RhsSymbol rhsSymbol;
	public Term suffix;
	public Term quant;

	InlineRhsItem(Term prefix, RhsSymbol rhsSymbol, Term suffix, Term quant) {
		this.prefix = prefix;
		this.rhsSymbol = rhsSymbol;
		this.suffix = suffix;
		this.quant = quant;
	}
	InlineRhsItem(Term ref, Term prefix, RhsSymbol rhsSymbol, Term suffix, Term quant) {
		this.ref = ref;
		this.prefix = prefix;
		this.rhsSymbol = rhsSymbol;
		this.suffix = suffix;
		this.quant = quant;
	}
	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
