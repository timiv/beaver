package beaver.cc.spec;

public class RhsSymbol {
	public Term id;
	public Term optQuant;

	RhsSymbol(Term id, Term optQuant) {
		this.id = id;
		this.optQuant = optQuant;
	}

	public boolean equals(RhsSymbol rhsSymbol) {
		return id.equals(rhsSymbol.id) && (optQuant == null && rhsSymbol.optQuant == null || optQuant != null && optQuant.equals(rhsSymbol.optQuant));
	}
}
