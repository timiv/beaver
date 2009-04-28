package beaver.cc.spec;

public class Spec {
	public RuleList ruleList;
	public PrecedenceList precedenceList;
	public MacroList macroList;
	public TokenList tokenList;

	Spec(RuleList ruleList, PrecedenceList precedenceList, MacroList macroList, TokenList tokenList) {
		this.ruleList = ruleList;
		this.precedenceList = precedenceList;
		this.macroList = macroList;
		this.tokenList = tokenList;
	}

	public boolean equals(Spec spec) {
		return ruleList.equals(spec.ruleList) && precedenceList.equals(spec.precedenceList) && macroList.equals(spec.macroList) && tokenList.equals(spec.tokenList);
	}
}
