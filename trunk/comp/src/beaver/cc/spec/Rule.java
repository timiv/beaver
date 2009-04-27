package beaver.cc.spec;

public class Rule {
	Rule next;
	public Term id;
	public AltDefList altDefList;

	Rule(Term id, AltDefList altDefList) {
		this.id = id;
		this.altDefList = altDefList;
	}
}
