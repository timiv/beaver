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

	public boolean equals(AltRegExprList list) {
		if (this.size == list.size) {
			for (CatRegExprList this_catRegExprList = this.first, list_catRegExprList = list.first; this_catRegExprList != null; this_catRegExprList = this_catRegExprList.next, list_catRegExprList = list_catRegExprList.next) {
				if (!this_catRegExprList.equals(list_catRegExprList)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
