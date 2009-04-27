package beaver.cc.spec;

public class MacroList {
	protected Macro first;
	protected Macro last;
	protected int size;

	protected MacroList() {
	}
	protected MacroList(Macro macro) {
		first = last = macro;
		size = 1;
	}
	protected MacroList add(Macro macro) {
		last = last.next = macro;
		++size;
		return this;
	}
	public int size() {
		return size;
	}
}
