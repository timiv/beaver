package beaver.cc.spec;

public class Precedence {
	Precedence next;
	public PrecSymbolList precSymbolList;
	public Term assoc;

	Precedence(PrecSymbolList precSymbolList, Term assoc) {
		this.precSymbolList = precSymbolList;
		this.assoc = assoc;
	}

	public boolean equals(Precedence precedence) {
		return precSymbolList.equals(precedence.precSymbolList) && assoc.equals(precedence.assoc);
	}
}
