package beaver.cc.spec;

public class PrecSymbolList {
	protected PrecSymbol first;
	protected PrecSymbol last;
	protected int size;

	protected PrecSymbolList() {
	}
	protected PrecSymbolList(PrecSymbol precSymbol) {
		first = last = precSymbol;
		size = 1;
	}
	protected PrecSymbolList add(PrecSymbol precSymbol) {
		last = last.next = precSymbol;
		++size;
		return this;
	}
	public int size() {
		return size;
	}

	public boolean equals(PrecSymbolList list) {
		if (this.size == list.size) {
			for (PrecSymbol this_precSymbol = this.first, list_precSymbol = list.first; this_precSymbol != null; this_precSymbol = this_precSymbol.next, list_precSymbol = list_precSymbol.next) {
				if (!this_precSymbol.equals(list_precSymbol)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
