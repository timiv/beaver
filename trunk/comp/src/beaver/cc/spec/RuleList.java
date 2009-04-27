package beaver.cc.spec;

public class RuleList {
	protected Rule first;
	protected Rule last;
	protected int size;

	protected RuleList() {
	}
	protected RuleList(Rule rule) {
		first = last = rule;
		size = 1;
	}
	protected RuleList add(Rule rule) {
		last = last.next = rule;
		++size;
		return this;
	}
	public int size() {
		return size;
	}
}
