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
}
