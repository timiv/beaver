package beaver.cc.spec;

public class KeywordRhsItem extends RhsItem {
	public Term text;

	KeywordRhsItem(Term text) {
		this.text = text;
	}
	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
