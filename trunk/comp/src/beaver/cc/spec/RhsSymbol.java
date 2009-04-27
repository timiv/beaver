package beaver.cc.spec;

public class RhsSymbol {
	public Term id;
	public Term optQuant;

	RhsSymbol(Term id, Term optQuant) {
		this.id = id;
		this.optQuant = optQuant;
	}
}
