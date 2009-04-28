package beaver.cc.spec;

public class AstTreeWalker implements NodeVisitor {
	public void enter(RuleList ruleList) {
	}
	public void visit(RuleList ruleList) {
		enter(ruleList);
		for (Rule rule = ruleList.first; rule != null; rule = rule.next) {
			visit(rule);
		}
		leave(ruleList);
	}
	public void leave(RuleList ruleList) {
	}
	public void enter(TokenList tokenList) {
	}
	public void visit(TokenList tokenList) {
		enter(tokenList);
		for (Token token = tokenList.first; token != null; token = token.next) {
			visit(token);
		}
		leave(tokenList);
	}
	public void leave(TokenList tokenList) {
	}
	public void enter(PrecedenceList precedenceList) {
	}
	public void visit(PrecedenceList precedenceList) {
		enter(precedenceList);
		for (Precedence precedence = precedenceList.first; precedence != null; precedence = precedence.next) {
			visit(precedence);
		}
		leave(precedenceList);
	}
	public void leave(PrecedenceList precedenceList) {
	}
	public void enter(MacroList macroList) {
	}
	public void visit(MacroList macroList) {
		enter(macroList);
		for (Macro macro = macroList.first; macro != null; macro = macro.next) {
			visit(macro);
		}
		leave(macroList);
	}
	public void leave(MacroList macroList) {
	}
	public void enter(AltDefList altDefList) {
	}
	public void visit(AltDefList altDefList) {
		enter(altDefList);
		for (AltDef altDef = altDefList.first; altDef != null; altDef = altDef.next) {
			visit(altDef);
		}
		leave(altDefList);
	}
	public void leave(AltDefList altDefList) {
	}
	public void enter(RhsItemList rhsItemList) {
	}
	public void visit(RhsItemList rhsItemList) {
		enter(rhsItemList);
		for (RhsItem rhsItem = rhsItemList.first; rhsItem != null; rhsItem = rhsItem.next) {
			rhsItem.dispatch(this);
		}
		leave(rhsItemList);
	}
	public void leave(RhsItemList rhsItemList) {
	}
	public void enter(PrecSymbolList precSymbolList) {
	}
	public void visit(PrecSymbolList precSymbolList) {
		enter(precSymbolList);
		for (PrecSymbol precSymbol = precSymbolList.first; precSymbol != null; precSymbol = precSymbol.next) {
			precSymbol.dispatch(this);
		}
		leave(precSymbolList);
	}
	public void leave(PrecSymbolList precSymbolList) {
	}
	public void enter(AltRegExprList altRegExprList) {
	}
	public void visit(AltRegExprList altRegExprList) {
		enter(altRegExprList);
		for (CatRegExprList catRegExprList = altRegExprList.first; catRegExprList != null; catRegExprList = catRegExprList.next) {
			visit(catRegExprList);
		}
		leave(altRegExprList);
	}
	public void leave(AltRegExprList altRegExprList) {
	}
	public void enter(CatRegExprList catRegExprList) {
	}
	public void visit(CatRegExprList catRegExprList) {
		enter(catRegExprList);
		for (CatRegExpr catRegExpr = catRegExprList.first; catRegExpr != null; catRegExpr = catRegExpr.next) {
			visit(catRegExpr);
		}
		leave(catRegExprList);
	}
	public void leave(CatRegExprList catRegExprList) {
	}
	public void enter(Spec spec) {
	}
	public void visit(Spec spec) {
		enter(spec);
		visit(spec.ruleList);
		visit(spec.precedenceList);
		visit(spec.macroList);
		visit(spec.tokenList);
		leave(spec);
	}
	public void leave(Spec spec) {
	}
	public void enter(Rule rule) {
	}
	public void visit(Rule rule) {
		enter(rule);
		visit(rule.id);
		visit(rule.altDefList);
		leave(rule);
	}
	public void leave(Rule rule) {
	}
	public void enter(AltDef altDef) {
	}
	public void visit(AltDef altDef) {
		enter(altDef);
		if (altDef.optRuleName != null) { 
			visit(altDef.optRuleName);
		}
		visit(altDef.rhsItemList);
		leave(altDef);
	}
	public void leave(AltDef altDef) {
	}
	public void enter(KeywordRhsItem keywordRhsItem) {
	}
	public void visit(KeywordRhsItem keywordRhsItem) {
		enter(keywordRhsItem);
		visit(keywordRhsItem.text);
		leave(keywordRhsItem);
	}
	public void leave(KeywordRhsItem keywordRhsItem) {
	}
	public void enter(SymbolRhsItem symbolRhsItem) {
	}
	public void visit(SymbolRhsItem symbolRhsItem) {
		enter(symbolRhsItem);
		if (symbolRhsItem.ref != null) { 
			visit(symbolRhsItem.ref);
		}
		visit(symbolRhsItem.rhsSymbol);
		leave(symbolRhsItem);
	}
	public void leave(SymbolRhsItem symbolRhsItem) {
	}
	public void enter(InlineRhsItem inlineRhsItem) {
	}
	public void visit(InlineRhsItem inlineRhsItem) {
		enter(inlineRhsItem);
		if (inlineRhsItem.ref != null) { 
			visit(inlineRhsItem.ref);
		}
		if (inlineRhsItem.prefix != null) { 
			visit(inlineRhsItem.prefix);
		}
		visit(inlineRhsItem.rhsSymbol);
		if (inlineRhsItem.suffix != null) { 
			visit(inlineRhsItem.suffix);
		}
		visit(inlineRhsItem.quant);
		leave(inlineRhsItem);
	}
	public void leave(InlineRhsItem inlineRhsItem) {
	}
	public void enter(RhsSymbol rhsSymbol) {
	}
	public void visit(RhsSymbol rhsSymbol) {
		enter(rhsSymbol);
		visit(rhsSymbol.id);
		if (rhsSymbol.optQuant != null) { 
			visit(rhsSymbol.optQuant);
		}
		leave(rhsSymbol);
	}
	public void leave(RhsSymbol rhsSymbol) {
	}
	public void enter(Precedence precedence) {
	}
	public void visit(Precedence precedence) {
		enter(precedence);
		visit(precedence.precSymbolList);
		visit(precedence.assoc);
		leave(precedence);
	}
	public void leave(Precedence precedence) {
	}
	public void enter(TermTextPrecSymbol termTextPrecSymbol) {
	}
	public void visit(TermTextPrecSymbol termTextPrecSymbol) {
		enter(termTextPrecSymbol);
		visit(termTextPrecSymbol.text);
		leave(termTextPrecSymbol);
	}
	public void leave(TermTextPrecSymbol termTextPrecSymbol) {
	}
	public void enter(RuleNamePrecSymbol ruleNamePrecSymbol) {
	}
	public void visit(RuleNamePrecSymbol ruleNamePrecSymbol) {
		enter(ruleNamePrecSymbol);
		visit(ruleNamePrecSymbol.id);
		leave(ruleNamePrecSymbol);
	}
	public void leave(RuleNamePrecSymbol ruleNamePrecSymbol) {
	}
	public void enter(Macro macro) {
	}
	public void visit(Macro macro) {
		enter(macro);
		visit(macro.id);
		visit(macro.altRegExprList);
		leave(macro);
	}
	public void leave(Macro macro) {
	}
	public void enter(CatRegExpr catRegExpr) {
	}
	public void visit(CatRegExpr catRegExpr) {
		enter(catRegExpr);
		catRegExpr.charExpr.dispatch(this);
		if (catRegExpr.optCharExprQuantifier != null) { 
			catRegExpr.optCharExprQuantifier.dispatch(this);
		}
		leave(catRegExpr);
	}
	public void leave(CatRegExpr catRegExpr) {
	}
	public void enter(TextCharExpr textCharExpr) {
	}
	public void visit(TextCharExpr textCharExpr) {
		enter(textCharExpr);
		visit(textCharExpr.text);
		leave(textCharExpr);
	}
	public void leave(TextCharExpr textCharExpr) {
	}
	public void enter(RangeCharExpr rangeCharExpr) {
	}
	public void visit(RangeCharExpr rangeCharExpr) {
		enter(rangeCharExpr);
		rangeCharExpr.rangeExpr.dispatch(this);
		leave(rangeCharExpr);
	}
	public void leave(RangeCharExpr rangeCharExpr) {
	}
	public void enter(NestedCharExpr nestedCharExpr) {
	}
	public void visit(NestedCharExpr nestedCharExpr) {
		enter(nestedCharExpr);
		visit(nestedCharExpr.altRegExprList);
		leave(nestedCharExpr);
	}
	public void leave(NestedCharExpr nestedCharExpr) {
	}
	public void enter(SimpleRangeExpr simpleRangeExpr) {
	}
	public void visit(SimpleRangeExpr simpleRangeExpr) {
		enter(simpleRangeExpr);
		visit(simpleRangeExpr.range);
		leave(simpleRangeExpr);
	}
	public void leave(SimpleRangeExpr simpleRangeExpr) {
	}
	public void enter(MacroRangeExpr macroRangeExpr) {
	}
	public void visit(MacroRangeExpr macroRangeExpr) {
		enter(macroRangeExpr);
		visit(macroRangeExpr.id);
		leave(macroRangeExpr);
	}
	public void leave(MacroRangeExpr macroRangeExpr) {
	}
	public void enter(DiffRangeExpr diffRangeExpr) {
	}
	public void visit(DiffRangeExpr diffRangeExpr) {
		enter(diffRangeExpr);
		diffRangeExpr.min.dispatch(this);
		diffRangeExpr.sub.dispatch(this);
		leave(diffRangeExpr);
	}
	public void leave(DiffRangeExpr diffRangeExpr) {
	}
	public void enter(OperCharExprQuantifier operCharExprQuantifier) {
	}
	public void visit(OperCharExprQuantifier operCharExprQuantifier) {
		enter(operCharExprQuantifier);
		visit(operCharExprQuantifier.quant);
		leave(operCharExprQuantifier);
	}
	public void leave(OperCharExprQuantifier operCharExprQuantifier) {
	}
	public void enter(MultCharExprQuantifier multCharExprQuantifier) {
	}
	public void visit(MultCharExprQuantifier multCharExprQuantifier) {
		enter(multCharExprQuantifier);
		visit(multCharExprQuantifier.min);
		if (multCharExprQuantifier.max != null) { 
			visit(multCharExprQuantifier.max);
		}
		leave(multCharExprQuantifier);
	}
	public void leave(MultCharExprQuantifier multCharExprQuantifier) {
	}
	public void enter(Token token) {
	}
	public void visit(Token token) {
		enter(token);
		visit(token.id);
		visit(token.altRegExprList);
		if (token.optContext != null) { 
			visit(token.optContext);
		}
		if (token.optEvent != null) { 
			visit(token.optEvent);
		}
		leave(token);
	}
	public void leave(Token token) {
	}
	public void visit(Term term) {
	}
}
