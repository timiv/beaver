package beaver.cc.spec;

public class OperCharExprQuantifier extends CharExprQuantifier {
	public Term quant;

	OperCharExprQuantifier(Term quant) {
		this.quant = quant;
	}

	public boolean equals(CharExprQuantifier charExprQuantifier) {
		return charExprQuantifier instanceof OperCharExprQuantifier && equals((OperCharExprQuantifier) charExprQuantifier);
	}

	public boolean equals(OperCharExprQuantifier operCharExprQuantifier) {
		return quant.equals(operCharExprQuantifier.quant);
	}

	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
