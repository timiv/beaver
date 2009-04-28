package beaver.cc.spec;

public class PrecedenceList {
	protected Precedence first;
	protected Precedence last;
	protected int size;

	protected PrecedenceList() {
	}
	protected PrecedenceList(Precedence precedence) {
		first = last = precedence;
		size = 1;
	}
	protected PrecedenceList add(Precedence precedence) {
		last = last.next = precedence;
		++size;
		return this;
	}
	public int size() {
		return size;
	}

	public boolean equals(PrecedenceList list) {
		if (this.size == list.size) {
			for (Precedence this_precedence = this.first, list_precedence = list.first; this_precedence != null; this_precedence = this_precedence.next, list_precedence = list_precedence.next) {
				if (!this_precedence.equals(list_precedence)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
