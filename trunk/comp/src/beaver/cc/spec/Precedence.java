package beaver.cc.spec;

public class Precedence {
	Precedence next;
	public PrecSymbolList precSymbolList;
	public Term assoc;

	Precedence(PrecSymbolList precSymbolList, Term assoc) {
		this.precSymbolList = precSymbolList;
		this.assoc = assoc;
	}
}
