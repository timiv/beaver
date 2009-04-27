package beaver.cc.spec;

public class BeaverParser extends beaver.Parser {
	public static final int QUANT = 11;
	public static final int ASSOC = 40;
	public static final int NUM = 29;
	public static final int ID = 1;
	public static final int TEXT = 2;
	public static final int RANGE = 7;

	public BeaverParser() throws java.io.IOException {
		super(BeaverParser.class.getResourceAsStream("BeaverParser.bpt"));
	}
	protected Object makeTerm(int id, Object text, int line, int column) {
		return new Term(id, text, line, column);
	}
	protected Object reduce(Object[] stack, int top, int rule) {
		switch (rule) {
			case 0: { // Spec = "%rules" RuleList OptPrecedenceList OptMacroList "%tokens" TokenList
				return new Spec((RuleList) stack[top + 4], (PrecedenceList) stack[top + 3], (MacroList) stack[top + 2], (TokenList) stack[top]);
			}
			case 1: { // OptPrecedenceList = "%precedences" PrecedenceList
				return stack[top];
			}
			case 2: { // OptPrecedenceList =
				return new PrecedenceList();
			}
			case 3: { // OptMacroList = "%macros" MacroList
				return stack[top];
			}
			case 4: { // OptMacroList =
				return new MacroList();
			}
			case 5: { // RuleList = Rule
				return new RuleList((Rule) stack[top]);
			}
			case 6: { // RuleList = RuleList Rule
				return ((RuleList) stack[top + 1]).add((Rule) stack[top]);
			}
			case 7: { // PrecedenceList = Precedence
				return new PrecedenceList((Precedence) stack[top]);
			}
			case 8: { // PrecedenceList = PrecedenceList Precedence
				return ((PrecedenceList) stack[top + 1]).add((Precedence) stack[top]);
			}
			case 9: { // MacroList = Macro
				return new MacroList((Macro) stack[top]);
			}
			case 10: { // MacroList = MacroList Macro
				return ((MacroList) stack[top + 1]).add((Macro) stack[top]);
			}
			case 11: { // TokenList = Token
				return new TokenList((Token) stack[top]);
			}
			case 12: { // TokenList = TokenList Token
				return ((TokenList) stack[top + 1]).add((Token) stack[top]);
			}
			case 13: { // Rule = ID "=" AltDefList ";"
				return new Rule((Term) stack[top + 3], (AltDefList) stack[top + 1]);
			}
			case 14: { // AltDefList = AltDef
				return new AltDefList((AltDef) stack[top]);
			}
			case 15: { // AltDefList = AltDefList "|" AltDef
				return ((AltDefList) stack[top + 2]).add((AltDef) stack[top]);
			}
			case 16: { // AltDef = OptRuleName OptRhsItemList
				return new AltDef((Term) stack[top + 1], (RhsItemList) stack[top]);
			}
			case 17: { // OptRuleName = "{" ID "}"
				return stack[top + 1];
			}
			case 18: { // OptRuleName =
				return null;
			}
			case 19: { // OptRhsItemList = RhsItemList
				return stack[top];
			}
			case 20: { // OptRhsItemList =
				return new RhsItemList();
			}
			case 21: { // RhsItemList = RhsItem
				return new RhsItemList((RhsItem) stack[top]);
			}
			case 22: { // RhsItemList = RhsItemList RhsItem
				return ((RhsItemList) stack[top + 1]).add((RhsItem) stack[top]);
			}
			case 23: { // RhsItem = { Keyword } TEXT
				return new KeywordRhsItem((Term) stack[top]);
			}
			case 24: { // RhsItem = { Symbol } ref:ID ":" RhsSymbol
				return new SymbolRhsItem((Term) stack[top + 2], (RhsSymbol) stack[top]);
			}
			case 25: { // RhsItem = { Symbol } RhsSymbol
				return new SymbolRhsItem((RhsSymbol) stack[top]);
			}
			case 26: { // RhsItem = { Inline } ref:ID ":" "(" prefix:OptText RhsSymbol suffix:OptText ")" QUANT
				return new InlineRhsItem((Term) stack[top + 7], (Term) stack[top + 4], (RhsSymbol) stack[top + 3], (Term) stack[top + 2], (Term) stack[top]);
			}
			case 27: { // RhsItem = { Inline } "(" prefix:OptText RhsSymbol suffix:OptText ")" QUANT
				return new InlineRhsItem((Term) stack[top + 4], (RhsSymbol) stack[top + 3], (Term) stack[top + 2], (Term) stack[top]);
			}
			case 28: { // OptText = TEXT
				return stack[top];
			}
			case 29: { // OptText =
				return null;
			}
			case 30: { // RhsSymbol = ID OptQuant
				return new RhsSymbol((Term) stack[top + 1], (Term) stack[top]);
			}
			case 31: { // OptQuant = QUANT
				return stack[top];
			}
			case 32: { // OptQuant =
				return null;
			}
			case 33: { // Precedence = PrecSymbolList ":" ASSOC ";"
				return new Precedence((PrecSymbolList) stack[top + 3], (Term) stack[top + 1]);
			}
			case 34: { // PrecSymbolList = PrecSymbol
				return new PrecSymbolList((PrecSymbol) stack[top]);
			}
			case 35: { // PrecSymbolList = PrecSymbolList "," PrecSymbol
				return ((PrecSymbolList) stack[top + 2]).add((PrecSymbol) stack[top]);
			}
			case 36: { // PrecSymbol = { TermText } TEXT
				return new TermTextPrecSymbol((Term) stack[top]);
			}
			case 37: { // PrecSymbol = { RuleName } ID
				return new RuleNamePrecSymbol((Term) stack[top]);
			}
			case 38: { // Macro = ID "=" AltRegExprList ";"
				return new Macro((Term) stack[top + 3], (AltRegExprList) stack[top + 1]);
			}
			case 39: { // AltRegExprList = CatRegExprList
				return new AltRegExprList((CatRegExprList) stack[top]);
			}
			case 40: { // AltRegExprList = AltRegExprList "|" CatRegExprList
				return ((AltRegExprList) stack[top + 2]).add((CatRegExprList) stack[top]);
			}
			case 41: { // CatRegExprList = CatRegExpr
				return new CatRegExprList((CatRegExpr) stack[top]);
			}
			case 42: { // CatRegExprList = CatRegExprList CatRegExpr
				return ((CatRegExprList) stack[top + 1]).add((CatRegExpr) stack[top]);
			}
			case 43: { // CatRegExpr = CharExpr OptCharExprQuantifier
				return new CatRegExpr((CharExpr) stack[top + 1], (CharExprQuantifier) stack[top]);
			}
			case 44: { // OptCharExprQuantifier = CharExprQuantifier
				return stack[top];
			}
			case 45: { // OptCharExprQuantifier =
				return null;
			}
			case 46: { // CharExpr = { Text } TEXT
				return new TextCharExpr((Term) stack[top]);
			}
			case 47: { // CharExpr = { Range } RangeExpr
				return new RangeCharExpr((RangeExpr) stack[top]);
			}
			case 48: { // CharExpr = { Nested } "(" AltRegExprList ")"
				return new NestedCharExpr((AltRegExprList) stack[top + 1]);
			}
			case 49: { // RangeExpr = { Simple } RANGE
				return new SimpleRangeExpr((Term) stack[top]);
			}
			case 50: { // RangeExpr = { Macro } ID
				return new MacroRangeExpr((Term) stack[top]);
			}
			case 51: { // RangeExpr = { Diff } min:RangeExpr "\" sub:RangeExpr
				return new DiffRangeExpr((RangeExpr) stack[top + 2], (RangeExpr) stack[top]);
			}
			case 52: { // CharExprQuantifier = { Oper } QUANT
				return new OperCharExprQuantifier((Term) stack[top]);
			}
			case 53: { // CharExprQuantifier = { Mult } "{" min:NUM "}"
				return new MultCharExprQuantifier((Term) stack[top + 1]);
			}
			case 54: { // CharExprQuantifier = { Mult } "{" min:NUM "," max:OptNum "}"
				return new MultCharExprQuantifier((Term) stack[top + 3], (Term) stack[top + 1]);
			}
			case 55: { // OptNum = NUM
				return stack[top];
			}
			case 56: { // OptNum =
				return null;
			}
			case 57: { // Token = ID "=" AltRegExprList OptContext OptEvent ";"
				return new Token((Term) stack[top + 5], (AltRegExprList) stack[top + 3], (CatRegExpr) stack[top + 2], (Term) stack[top + 1]);
			}
			case 58: { // OptContext = "/" CatRegExpr
				return stack[top];
			}
			case 59: { // OptContext =
				return null;
			}
			case 60: { // OptEvent = "->" ID
				return stack[top];
			}
			case 61: { // OptEvent =
				return null;
			}
		}
		throw new IndexOutOfBoundsException("production #" + rule);
	}
}
