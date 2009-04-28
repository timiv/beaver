package beaver.cc.spec;

public class CatRegExpr {
	CatRegExpr next;
	public CharExpr charExpr;
	public CharExprQuantifier optCharExprQuantifier;

	CatRegExpr(CharExpr charExpr, CharExprQuantifier optCharExprQuantifier) {
		this.charExpr = charExpr;
		this.optCharExprQuantifier = optCharExprQuantifier;
	}

	public boolean equals(CatRegExpr catRegExpr) {
		return charExpr.equals(catRegExpr.charExpr) && (optCharExprQuantifier == null && catRegExpr.optCharExprQuantifier == null || optCharExprQuantifier != null && optCharExprQuantifier.equals(catRegExpr.optCharExprQuantifier));
	}
}
