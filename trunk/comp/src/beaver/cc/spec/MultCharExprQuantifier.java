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

	public boolean equals(CharExprQuantifier charExprQuantifier) {
		return charExprQuantifier instanceof MultCharExprQuantifier && equals((MultCharExprQuantifier) charExprQuantifier);
	}

	public boolean equals(MultCharExprQuantifier multCharExprQuantifier) {
		return min.equals(multCharExprQuantifier.min) && (max == null && multCharExprQuantifier.max == null || max != null && max.equals(multCharExprQuantifier.max));
	}

	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
