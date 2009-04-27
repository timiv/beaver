package beaver.cc.spec;

public class AltRegExprList {
	protected CatRegExprList first;
	protected CatRegExprList last;
	protected int size;

	protected AltRegExprList() {
	}
	protected AltRegExprList(CatRegExprList catRegExprList) {
		first = last = catRegExprList;
		size = 1;
	}
	protected AltRegExprList add(CatRegExprList catRegExprList) {
		last = last.next = catRegExprList;
		++size;
		return this;
	}
	public int size() {
		return size;
	}
}
