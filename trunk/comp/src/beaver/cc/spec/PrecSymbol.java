package beaver.cc.spec;

public abstract class PrecSymbol {
	PrecSymbol next;

	public abstract boolean equals(PrecSymbol precSymbol);
	abstract void dispatch(NodeVisitor visitor);
}
