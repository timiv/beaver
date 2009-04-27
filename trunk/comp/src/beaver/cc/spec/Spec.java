package beaver.cc.spec;

public class Spec {
	public RuleList ruleList;
	public PrecedenceList optPrecedenceList;
	public MacroList optMacroList;
	public TokenList tokenList;

	Spec(RuleList ruleList, PrecedenceList optPrecedenceList, MacroList optMacroList, TokenList tokenList) {
		this.ruleList = ruleList;
		this.optPrecedenceList = optPrecedenceList;
		this.optMacroList = optMacroList;
		this.tokenList = tokenList;
	}
}
