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
}
