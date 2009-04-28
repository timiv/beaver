package beaver.cc.spec;

public class SymbolRhsItem extends RhsItem {
	public Term ref;
	public RhsSymbol rhsSymbol;

	SymbolRhsItem(RhsSymbol rhsSymbol) {
		this.rhsSymbol = rhsSymbol;
	}
	SymbolRhsItem(Term ref, RhsSymbol rhsSymbol) {
		this.ref = ref;
		this.rhsSymbol = rhsSymbol;
	}

	public boolean equals(RhsItem rhsItem) {
		return rhsItem instanceof SymbolRhsItem && equals((SymbolRhsItem) rhsItem);
	}

	public boolean equals(SymbolRhsItem symbolRhsItem) {
		return (ref == null && symbolRhsItem.ref == null || ref != null && ref.equals(symbolRhsItem.ref)) && rhsSymbol.equals(symbolRhsItem.rhsSymbol);
	}

	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
