package beaver.cc.spec;

public class Term {
	public Object value;
	public int id;
	public int line;
	public int column;

	Term(int id, Object value, int line, int column) {
		this.id = id;
		this.value = value;
		this.line = line;
		this.column = column;
	}

	public boolean equals(Term term) {
		return this.id == term.id && this.value.equals(term.value);
	}
}
