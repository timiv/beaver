package beaver.cc.spec;

public class RhsItemList {
	protected RhsItem first;
	protected RhsItem last;
	protected int size;

	protected RhsItemList() {
	}
	protected RhsItemList(RhsItem rhsItem) {
		first = last = rhsItem;
		size = 1;
	}
	protected RhsItemList add(RhsItem rhsItem) {
		last = last.next = rhsItem;
		++size;
		return this;
	}
	public int size() {
		return size;
	}
}
