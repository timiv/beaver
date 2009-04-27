package beaver.cc.spec;

public class MultCharExprQuantifier extends CharExprQuantifier {
	public Term min;
	public Term max;

	MultCharExprQuantifier(Term min, Term max) {
		this.min = min;
		this.max = max;
	}
	MultCharExprQuantifier(Term min) {
		this.min = min;
	}
	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
