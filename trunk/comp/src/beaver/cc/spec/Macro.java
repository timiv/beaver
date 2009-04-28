package beaver.cc.spec;

public class Macro {
	Macro next;
	public Term id;
	public AltRegExprList altRegExprList;

	Macro(Term id, AltRegExprList altRegExprList) {
		this.id = id;
		this.altRegExprList = altRegExprList;
	}

	public boolean equals(Macro macro) {
		return id.equals(macro.id) && altRegExprList.equals(macro.altRegExprList);
	}
}
