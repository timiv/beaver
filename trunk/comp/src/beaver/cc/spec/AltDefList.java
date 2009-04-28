package beaver.cc.spec;

public class AltDefList {
	protected AltDef first;
	protected AltDef last;
	protected int size;

	protected AltDefList() {
	}
	protected AltDefList(AltDef altDef) {
		first = last = altDef;
		size = 1;
	}
	protected AltDefList add(AltDef altDef) {
		last = last.next = altDef;
		++size;
		return this;
	}
	public int size() {
		return size;
	}

	public boolean equals(AltDefList list) {
		if (this.size == list.size) {
			for (AltDef this_altDef = this.first, list_altDef = list.first; this_altDef != null; this_altDef = this_altDef.next, list_altDef = list_altDef.next) {
				if (!this_altDef.equals(list_altDef)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
