package beaver.cc.spec;

public class TokenList {
	protected Token first;
	protected Token last;
	protected int size;

	protected TokenList() {
	}
	protected TokenList(Token token) {
		first = last = token;
		size = 1;
	}
	protected TokenList add(Token token) {
		last = last.next = token;
		++size;
		return this;
	}
	public int size() {
		return size;
	}
}
