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
	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
