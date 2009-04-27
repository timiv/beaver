package beaver.cc.spec;

public class AltDef {
	AltDef next;
	public Term optRuleName;
	public RhsItemList optRhsItemList;

	AltDef(Term optRuleName, RhsItemList optRhsItemList) {
		this.optRuleName = optRuleName;
		this.optRhsItemList = optRhsItemList;
	}
}
