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

	public boolean equals(RhsItemList list) {
		if (this.size == list.size) {
			for (RhsItem this_rhsItem = this.first, list_rhsItem = list.first; this_rhsItem != null; this_rhsItem = this_rhsItem.next, list_rhsItem = list_rhsItem.next) {
				if (!this_rhsItem.equals(list_rhsItem)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
