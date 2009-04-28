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

	public boolean equals(RhsItem rhsItem) {
		return rhsItem instanceof InlineRhsItem && equals((InlineRhsItem) rhsItem);
	}

	public boolean equals(InlineRhsItem inlineRhsItem) {
		return (ref == null && inlineRhsItem.ref == null || ref != null && ref.equals(inlineRhsItem.ref)) && (prefix == null && inlineRhsItem.prefix == null || prefix != null && prefix.equals(inlineRhsItem.prefix)) && rhsSymbol.equals(inlineRhsItem.rhsSymbol) && (suffix == null && inlineRhsItem.suffix == null || suffix != null && suffix.equals(inlineRhsItem.suffix)) && quant.equals(inlineRhsItem.quant);
	}

	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
