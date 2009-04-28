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

	public boolean equals(CatRegExprList list) {
		if (this.size == list.size) {
			for (CatRegExpr this_catRegExpr = this.first, list_catRegExpr = list.first; this_catRegExpr != null; this_catRegExpr = this_catRegExpr.next, list_catRegExpr = list_catRegExpr.next) {
				if (!this_catRegExpr.equals(list_catRegExpr)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
