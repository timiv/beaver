package beaver.cc.spec;

public class CatRegExprList {
	CatRegExprList next;
	protected CatRegExpr first;
	protected CatRegExpr last;
	protected int size;

	protected CatRegExprList() {
	}
	protected CatRegExprList(CatRegExpr catRegExpr) {
		first = last = catRegExpr;
		size = 1;
	}
	protected CatRegExprList add(CatRegExpr catRegExpr) {
		last = last.next = catRegExpr;
		++size;
		return this;
	}
	public int size() {
		return size;
	}
}
