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
}
