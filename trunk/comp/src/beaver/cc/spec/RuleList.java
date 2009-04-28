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

	public boolean equals(RuleList list) {
		if (this.size == list.size) {
			for (Rule this_rule = this.first, list_rule = list.first; this_rule != null; this_rule = this_rule.next, list_rule = list_rule.next) {
				if (!this_rule.equals(list_rule)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
