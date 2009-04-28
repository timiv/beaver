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

	public boolean equals(Token token) {
		return id.equals(token.id) && altRegExprList.equals(token.altRegExprList) && (optContext == null && token.optContext == null || optContext != null && optContext.equals(token.optContext)) && (optEvent == null && token.optEvent == null || optEvent != null && optEvent.equals(token.optEvent));
	}
}
