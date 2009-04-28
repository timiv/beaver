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

	public boolean equals(MacroList list) {
		if (this.size == list.size) {
			for (Macro this_macro = this.first, list_macro = list.first; this_macro != null; this_macro = this_macro.next, list_macro = list_macro.next) {
				if (!this_macro.equals(list_macro)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
