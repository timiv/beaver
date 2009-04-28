package beaver.cc.spec;

public class AltDef {
	AltDef next;
	public Term optRuleName;
	public RhsItemList rhsItemList;

	AltDef(Term optRuleName, RhsItemList rhsItemList) {
		this.optRuleName = optRuleName;
		this.rhsItemList = rhsItemList;
	}

	public boolean equals(AltDef altDef) {
		return (optRuleName == null && altDef.optRuleName == null || optRuleName != null && optRuleName.equals(altDef.optRuleName)) && rhsItemList.equals(altDef.rhsItemList);
	}
}
