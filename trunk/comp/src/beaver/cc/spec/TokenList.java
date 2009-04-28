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

	public boolean equals(TokenList list) {
		if (this.size == list.size) {
			for (Token this_token = this.first, list_token = list.first; this_token != null; this_token = this_token.next, list_token = list_token.next) {
				if (!this_token.equals(list_token)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
