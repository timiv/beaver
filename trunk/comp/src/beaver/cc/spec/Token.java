package beaver.cc.spec;

public class Token {
	Token next;
	public Term id;
	public AltRegExprList altRegExprList;
	public CatRegExpr optContext;
	public Term optEvent;

	Token(Term id, AltRegExprList altRegExprList, CatRegExpr optContext, Term optEvent) {
		this.id = id;
		this.altRegExprList = altRegExprList;
		this.optContext = optContext;
		this.optEvent = optEvent;
	}
}
