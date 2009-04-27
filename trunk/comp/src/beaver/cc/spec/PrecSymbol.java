package beaver.cc.spec;

public abstract class PrecSymbol {
	PrecSymbol next;

	abstract void dispatch(NodeVisitor visitor);
}
