package beaver.cc.spec;

public class KeywordRhsItem extends RhsItem {
	public Term text;

	KeywordRhsItem(Term text) {
		this.text = text;
	}

	public boolean equals(RhsItem rhsItem) {
		return rhsItem instanceof KeywordRhsItem && equals((KeywordRhsItem) rhsItem);
	}

	public boolean equals(KeywordRhsItem keywordRhsItem) {
		return text.equals(keywordRhsItem.text);
	}

	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
