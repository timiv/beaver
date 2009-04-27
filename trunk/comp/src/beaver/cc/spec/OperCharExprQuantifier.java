package beaver.cc.spec;

public class OperCharExprQuantifier extends CharExprQuantifier {
	public Term quant;

	OperCharExprQuantifier(Term quant) {
		this.quant = quant;
	}
	void dispatch(NodeVisitor visitor) {
		visitor.visit(this);
	}
}
