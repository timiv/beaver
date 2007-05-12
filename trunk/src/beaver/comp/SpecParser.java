/**
* Beaver: compiler front-end construction toolkit
* Copyright (c) 2007 Alexander Demenchuk <alder@softanvil.com>
* All rights reserved.
*
* See the file "LICENSE" for the terms and conditions for copying,
* distribution and modification of Beaver.
*/
package beaver.comp;

import java.io.DataInputStream;
import java.io.IOException;

import beaver.ParsingTables;
import beaver.Symbol;

import beaver.comp.cst.*;

/**
 * @author <a href="http://beaver.sourceforge.net">Beaver</a> parser generator
 */
public abstract class SpecParser extends beaver.Parser
{
	public static final char EOF    = '\000';
	public static final char MACROS = '\001'; // "%macros"
	public static final char TOKENS = '\004'; // "%tokens"
	public static final char STSEL  = '\023';
	public static final char OPER   = '\024';
	public static final char NUM    = '\025';
	public static final char ASSOC  = '\026';
	public static final char NAME   = '\027';
	public static final char TEXT   = '\030';
	public static final char RANGE  = '\031';


	protected Spec onSpec(ParserSpec parserSpec, ScannerSpec scannerSpec)
	{
		return new Spec(parserSpec, scannerSpec);
	}

	protected ParserSpec onParserSpec(RuleList ruleList)
	{
		return new ParserSpec(ruleList);
	}

	protected ParserSpec onParserSpec(RuleList ruleList, PrecedenceList precedenceList)
	{
		return new ParserSpec(ruleList, precedenceList);
	}

	protected Rule onRule(Term name, AltList altList)
	{
		return new Rule(name, altList);
	}

	protected Alt onAlt(ItemList itemList)
	{
		return new Alt(itemList);
	}

	protected Alt onAlt(Term name, ItemList itemList)
	{
		return new Alt(name, itemList);
	}

	protected Item onItemStatic(Term text)
	{
		return new ItemStatic(text);
	}

	protected Item onItemSymbol(Term name, Term oper)
	{
		return new ItemSymbol(name, oper);
	}

	protected Item onItemInline(ItemList itemList)
	{
		return new ItemInline(itemList);
	}

	protected Item onItemSymbol(Term ref, Term name, Term oper)
	{
		return new ItemSymbol(ref, name, oper);
	}

	protected Precedence onPrecedence(PrecItemList precItemList, Term assoc)
	{
		return new Precedence(precItemList, assoc);
	}

	protected PrecItem onPrecItemTerm(Term text)
	{
		return new PrecItemTerm(text);
	}

	protected PrecItem onPrecItemRule(Term name)
	{
		return new PrecItemRule(name);
	}

	protected ScannerSpec onScannerSpec(TermDeclList terminals, ScannerStateList states)
	{
		return new ScannerSpec(terminals, states);
	}

	protected ScannerSpec onScannerSpec(MacroDeclList macros, TermDeclList terminals, ScannerStateList states)
	{
		return new ScannerSpec(macros, terminals, states);
	}

	protected MacroDecl onMacroDecl(Term name, RegExp regExp)
	{
		return new MacroDecl(name, regExp);
	}

	protected TermDecl onTermDecl(Term name, RegExp regExp)
	{
		return new TermDecl(name, regExp);
	}

	protected TermDecl onTermDecl(Term name, RegExp regExp, RegExp ctx)
	{
		return new TermDecl(name, regExp, ctx);
	}

	protected TermDecl onTermDecl(Term name, RegExp regExp, Term event)
	{
		return new TermDecl(name, regExp, event);
	}

	protected TermDecl onTermDecl(Term name, RegExp regExp, RegExp ctx, Term event)
	{
		return new TermDecl(name, regExp, ctx, event);
	}

	protected ScannerState onScannerState(Term selector, Term name, TermDeclList terminals)
	{
		return new ScannerState(selector, name, terminals);
	}

	protected RegExpItem onRegExpItem(CharExpr charExpr)
	{
		return new RegExpItem(charExpr);
	}

	protected RegExpItem onRegExpItemClose(CharExpr charExpr, Term oper)
	{
		return new RegExpItemClose(charExpr, oper);
	}

	protected RegExpItem onRegExpItemQuant(CharExpr charExpr, Quantifier quantifier)
	{
		return new RegExpItemQuant(charExpr, quantifier);
	}

	protected Quantifier onQuantifier(NumTerm min)
	{
		return new Quantifier(min);
	}

	protected Quantifier onQuantifier(NumTerm min, NumTerm max)
	{
		return new Quantifier(min, max);
	}

	protected CharExpr onCharExprText(Term text)
	{
		return new CharExprText(text);
	}

	protected CharExpr onCharExprRange(RangeExpr rangeExpr)
	{
		return new CharExprRange(rangeExpr);
	}

	protected CharExpr onCharExprNested(RegExp regExp)
	{
		return new CharExprNested(regExp);
	}

	protected RangeExpr onRangeExprRange(Term range)
	{
		return new RangeExprRange(range);
	}

	protected RangeExpr onRangeExprMacro(Term macro)
	{
		return new RangeExprMacro(macro);
	}

	protected RangeExpr onRangeExprMinus(RangeExpr diff, RangeExpr range)
	{
		return new RangeExprMinus(diff, range);
	}

	protected AltList onAltList(Alt alt)
	{
		return new AltList().add(alt);
	}

	protected AltList onAltList(AltList altList, Alt alt)
	{
		return altList.add(alt);
	}

	protected PrecItemList onPrecItemList(PrecItem precItem)
	{
		return new PrecItemList().add(precItem);
	}

	protected PrecItemList onPrecItemList(PrecItemList precItemList, PrecItem precItem)
	{
		return precItemList.add(precItem);
	}

	protected RegExp onRegExp(RegExpItemList regExpItemList)
	{
		return new RegExp().add(regExpItemList);
	}

	protected RegExp onRegExp(RegExp regExp, RegExpItemList regExpItemList)
	{
		return regExp.add(regExpItemList);
	}

	protected RuleList onRuleList(Rule item)
	{
		return new RuleList().add(item);
	}

	protected RuleList onRuleList(RuleList list, Rule item)
	{
		return list.add(item);
	}

	protected PrecedenceList onPrecedenceList(Precedence item)
	{
		return new PrecedenceList().add(item);
	}

	protected PrecedenceList onPrecedenceList(PrecedenceList list, Precedence item)
	{
		return list.add(item);
	}

	protected ItemList onItemList(Item item)
	{
		return new ItemList().add(item);
	}

	protected ItemList onItemList(ItemList list, Item item)
	{
		return list.add(item);
	}

	protected TermDeclList onTermDeclList(TermDecl item)
	{
		return new TermDeclList().add(item);
	}

	protected TermDeclList onTermDeclList(TermDeclList list, TermDecl item)
	{
		return list.add(item);
	}

	protected ScannerStateList onScannerStateList(ScannerState item)
	{
		return new ScannerStateList().add(item);
	}

	protected ScannerStateList onScannerStateList(ScannerStateList list, ScannerState item)
	{
		return list.add(item);
	}

	protected MacroDeclList onMacroDeclList(MacroDecl item)
	{
		return new MacroDeclList().add(item);
	}

	protected MacroDeclList onMacroDeclList(MacroDeclList list, MacroDecl item)
	{
		return list.add(item);
	}

	protected RegExpItemList onRegExpItemList(RegExpItem item)
	{
		return new RegExpItemList().add(item);
	}

	protected RegExpItemList onRegExpItemList(RegExpItemList list, RegExpItem item)
	{
		return list.add(item);
	}

	protected Symbol reduce(Symbol[] symbols, int at, int ruleNo)
	{
		switch (ruleNo)
		{
			case  0: // Spec = OptParserSpec OptScannerSpec
			{
				ParserSpec  parserSpec  = (ParserSpec ) symbols[at].getValue();
				ScannerSpec scannerSpec = (ScannerSpec) symbols[at - 1].getValue();

				return symbol ( onSpec(parserSpec, scannerSpec) );
			}
			case  1: // ParserSpec = "%rules" RuleList
			{
				RuleList ruleList = (RuleList) symbols[at - 1].getValue();

				return symbol ( onParserSpec(ruleList) );
			}
			case  2: // ParserSpec = "%rules" RuleList "%precedence" PrecedenceList
			{
				RuleList       ruleList       = (RuleList      ) symbols[at - 1].getValue();
				PrecedenceList precedenceList = (PrecedenceList) symbols[at - 3].getValue();

				return symbol ( onParserSpec(ruleList, precedenceList) );
			}
			case  3: // Rule = NAME "=" AltList ";"
			{
				Term    name    = (Term   ) symbols[at].getValue();
				AltList altList = (AltList) symbols[at - 2].getValue();

				return symbol ( onRule(name, altList) );
			}
			case  4: // AltList = Alt
			{
				Alt alt = (Alt) symbols[at].getValue();

				return symbol ( onAltList(alt) );
			}
			case  5: // AltList = AltList "|" Alt
			{
				AltList altList = (AltList) symbols[at].getValue();
				Alt     alt     = (Alt    ) symbols[at - 2].getValue();

				return symbol ( onAltList(altList, alt) );
			}
			case  6: // Alt = OptItemList
			{
				ItemList itemList = (ItemList) symbols[at].getValue();

				return symbol ( onAlt(itemList) );
			}
			case  7: // Alt = "{" NAME "}" OptItemList
			{
				Term     name     = (Term    ) symbols[at - 1].getValue();
				ItemList itemList = (ItemList) symbols[at - 3].getValue();

				return symbol ( onAlt(name, itemList) );
			}
			case  8: // Item = TEXT
			{
				Term text = (Term) symbols[at].getValue();

				return symbol ( onItemStatic(text) );
			}
			case  9: // Item = NAME OptOPER
			{
				Term name = (Term) symbols[at].getValue();
				Term oper = (Term) symbols[at - 1].getValue();

				return symbol ( onItemSymbol(name, oper) );
			}
			case 10: // Item = "[" ItemList "]"
			{
				ItemList itemList = (ItemList) symbols[at - 1].getValue();

				return symbol ( onItemInline(itemList) );
			}
			case 11: // Item = NAME ":" NAME OptOPER
			{
				Term ref  = (Term) symbols[at].getValue();
				Term name = (Term) symbols[at - 2].getValue();
				Term oper = (Term) symbols[at - 3].getValue();

				return symbol ( onItemSymbol(ref, name, oper) );
			}
			case 12: // Precedence = PrecItemList ":" ASSOC ";"
			{
				PrecItemList precItemList = (PrecItemList) symbols[at].getValue();
				Term         assoc        = (Term        ) symbols[at - 2].getValue();

				return symbol ( onPrecedence(precItemList, assoc) );
			}
			case 13: // PrecItemList = PrecItem
			{
				PrecItem precItem = (PrecItem) symbols[at].getValue();

				return symbol ( onPrecItemList(precItem) );
			}
			case 14: // PrecItemList = PrecItemList "," PrecItem
			{
				PrecItemList precItemList = (PrecItemList) symbols[at].getValue();
				PrecItem     precItem     = (PrecItem    ) symbols[at - 2].getValue();

				return symbol ( onPrecItemList(precItemList, precItem) );
			}
			case 15: // PrecItem = TEXT
			{
				Term text = (Term) symbols[at].getValue();

				return symbol ( onPrecItemTerm(text) );
			}
			case 16: // PrecItem = NAME
			{
				Term name = (Term) symbols[at].getValue();

				return symbol ( onPrecItemRule(name) );
			}
			case 17: // ScannerSpec = "%tokens" TermDeclList OptScannerStateList
			{
				TermDeclList     terminals = (TermDeclList    ) symbols[at - 1].getValue();
				ScannerStateList states    = (ScannerStateList) symbols[at - 2].getValue();

				return symbol ( onScannerSpec(terminals, states) );
			}
			case 18: // ScannerSpec = "%macros" MacroDeclList "%tokens" TermDeclList OptScannerStateList
			{
				MacroDeclList    macros    = (MacroDeclList   ) symbols[at - 1].getValue();
				TermDeclList     terminals = (TermDeclList    ) symbols[at - 3].getValue();
				ScannerStateList states    = (ScannerStateList) symbols[at - 4].getValue();

				return symbol ( onScannerSpec(macros, terminals, states) );
			}
			case 19: // MacroDecl = NAME "=" RegExp ";"
			{
				Term   name   = (Term  ) symbols[at].getValue();
				RegExp regExp = (RegExp) symbols[at - 2].getValue();

				return symbol ( onMacroDecl(name, regExp) );
			}
			case 20: // TermDecl = NAME "=" RegExp ";"
			{
				Term   name   = (Term  ) symbols[at].getValue();
				RegExp regExp = (RegExp) symbols[at - 2].getValue();

				return symbol ( onTermDecl(name, regExp) );
			}
			case 21: // TermDecl = NAME "=" RegExp "/" RegExp ";"
			{
				Term   name   = (Term  ) symbols[at].getValue();
				RegExp regExp = (RegExp) symbols[at - 2].getValue();
				RegExp ctx    = (RegExp) symbols[at - 4].getValue();

				return symbol ( onTermDecl(name, regExp, ctx) );
			}
			case 22: // TermDecl = NAME "=" RegExp "->" NAME ";"
			{
				Term   name   = (Term  ) symbols[at].getValue();
				RegExp regExp = (RegExp) symbols[at - 2].getValue();
				Term   event  = (Term  ) symbols[at - 4].getValue();

				return symbol ( onTermDecl(name, regExp, event) );
			}
			case 23: // TermDecl = NAME "=" RegExp "/" RegExp "->" NAME ";"
			{
				Term   name   = (Term  ) symbols[at].getValue();
				RegExp regExp = (RegExp) symbols[at - 2].getValue();
				RegExp ctx    = (RegExp) symbols[at - 4].getValue();
				Term   event  = (Term  ) symbols[at - 6].getValue();

				return symbol ( onTermDecl(name, regExp, ctx, event) );
			}
			case 24: // ScannerState = STSEL NAME TermDeclList
			{
				Term         selector  = (Term        ) symbols[at].getValue();
				Term         name      = (Term        ) symbols[at - 1].getValue();
				TermDeclList terminals = (TermDeclList) symbols[at - 2].getValue();

				return symbol ( onScannerState(selector, name, terminals) );
			}
			case 25: // RegExp = RegExpItemList
			{
				RegExpItemList regExpItemList = (RegExpItemList) symbols[at].getValue();

				return symbol ( onRegExp(regExpItemList) );
			}
			case 26: // RegExp = RegExp "|" RegExpItemList
			{
				RegExp         regExp         = (RegExp        ) symbols[at].getValue();
				RegExpItemList regExpItemList = (RegExpItemList) symbols[at - 2].getValue();

				return symbol ( onRegExp(regExp, regExpItemList) );
			}
			case 27: // RegExpItem = CharExpr
			{
				CharExpr charExpr = (CharExpr) symbols[at].getValue();

				return symbol ( onRegExpItem(charExpr) );
			}
			case 28: // RegExpItem = CharExpr OPER
			{
				CharExpr charExpr = (CharExpr) symbols[at].getValue();
				Term     oper     = (Term    ) symbols[at - 1].getValue();

				return symbol ( onRegExpItemClose(charExpr, oper) );
			}
			case 29: // RegExpItem = CharExpr Quantifier
			{
				CharExpr   charExpr   = (CharExpr  ) symbols[at].getValue();
				Quantifier quantifier = (Quantifier) symbols[at - 1].getValue();

				return symbol ( onRegExpItemQuant(charExpr, quantifier) );
			}
			case 30: // Quantifier = "{" NUM "}"
			{
				NumTerm min = (NumTerm) symbols[at - 1].getValue();

				return symbol ( onQuantifier(min) );
			}
			case 31: // Quantifier = "{" NUM "," OptNUM "}"
			{
				NumTerm min = (NumTerm) symbols[at - 1].getValue();
				NumTerm max = (NumTerm) symbols[at - 3].getValue();

				return symbol ( onQuantifier(min, max) );
			}
			case 32: // CharExpr = TEXT
			{
				Term text = (Term) symbols[at].getValue();

				return symbol ( onCharExprText(text) );
			}
			case 33: // CharExpr = RangeExpr
			{
				RangeExpr rangeExpr = (RangeExpr) symbols[at].getValue();

				return symbol ( onCharExprRange(rangeExpr) );
			}
			case 34: // CharExpr = "(" RegExp ")"
			{
				RegExp regExp = (RegExp) symbols[at - 1].getValue();

				return symbol ( onCharExprNested(regExp) );
			}
			case 35: // RangeExpr = RANGE
			{
				Term range = (Term) symbols[at].getValue();

				return symbol ( onRangeExprRange(range) );
			}
			case 36: // RangeExpr = NAME
			{
				Term macro = (Term) symbols[at].getValue();

				return symbol ( onRangeExprMacro(macro) );
			}
			case 37: // RangeExpr = RangeExpr "\\" RangeExpr
			{
				RangeExpr diff  = (RangeExpr) symbols[at].getValue();
				RangeExpr range = (RangeExpr) symbols[at - 2].getValue();

				return symbol ( onRangeExprMinus(diff, range) );
			}
			case 38: // OptParserSpec =
			{
				return symbol( null );
			}
			case 39: // OptParserSpec = ParserSpec
			{
				return copy( symbols[at] );
			}
			case 40: // OptScannerSpec =
			{
				return symbol( null );
			}
			case 41: // OptScannerSpec = ScannerSpec
			{
				return copy( symbols[at] );
			}
			case 42: // RuleList = Rule
			{
				Rule item = (Rule) symbols[at].getValue();

				return symbol ( onRuleList(item) );
			}
			case 43: // RuleList = RuleList Rule
			{
				RuleList list = (RuleList) symbols[at].getValue();
				Rule     item = (Rule    ) symbols[at - 1].getValue();

				return symbol ( onRuleList(list, item) );
			}
			case 44: // PrecedenceList = Precedence
			{
				Precedence item = (Precedence) symbols[at].getValue();

				return symbol ( onPrecedenceList(item) );
			}
			case 45: // PrecedenceList = PrecedenceList Precedence
			{
				PrecedenceList list = (PrecedenceList) symbols[at].getValue();
				Precedence     item = (Precedence    ) symbols[at - 1].getValue();

				return symbol ( onPrecedenceList(list, item) );
			}
			case 46: // ItemList = Item
			{
				Item item = (Item) symbols[at].getValue();

				return symbol ( onItemList(item) );
			}
			case 47: // ItemList = ItemList Item
			{
				ItemList list = (ItemList) symbols[at].getValue();
				Item     item = (Item    ) symbols[at - 1].getValue();

				return symbol ( onItemList(list, item) );
			}
			case 48: // OptItemList =
			{
				return symbol( null );
			}
			case 49: // OptItemList = ItemList
			{
				return copy( symbols[at] );
			}
			case 50: // OptOPER =
			{
				return symbol( null );
			}
			case 51: // OptOPER = OPER
			{
				return copy( symbols[at] );
			}
			case 52: // TermDeclList = TermDecl
			{
				TermDecl item = (TermDecl) symbols[at].getValue();

				return symbol ( onTermDeclList(item) );
			}
			case 53: // TermDeclList = TermDeclList TermDecl
			{
				TermDeclList list = (TermDeclList) symbols[at].getValue();
				TermDecl     item = (TermDecl    ) symbols[at - 1].getValue();

				return symbol ( onTermDeclList(list, item) );
			}
			case 54: // ScannerStateList = ScannerState
			{
				ScannerState item = (ScannerState) symbols[at].getValue();

				return symbol ( onScannerStateList(item) );
			}
			case 55: // ScannerStateList = ScannerStateList ScannerState
			{
				ScannerStateList list = (ScannerStateList) symbols[at].getValue();
				ScannerState     item = (ScannerState    ) symbols[at - 1].getValue();

				return symbol ( onScannerStateList(list, item) );
			}
			case 56: // OptScannerStateList =
			{
				return symbol( null );
			}
			case 57: // OptScannerStateList = ScannerStateList
			{
				return copy( symbols[at] );
			}
			case 58: // MacroDeclList = MacroDecl
			{
				MacroDecl item = (MacroDecl) symbols[at].getValue();

				return symbol ( onMacroDeclList(item) );
			}
			case 59: // MacroDeclList = MacroDeclList MacroDecl
			{
				MacroDeclList list = (MacroDeclList) symbols[at].getValue();
				MacroDecl     item = (MacroDecl    ) symbols[at - 1].getValue();

				return symbol ( onMacroDeclList(list, item) );
			}
			case 60: // RegExpItemList = RegExpItem
			{
				RegExpItem item = (RegExpItem) symbols[at].getValue();

				return symbol ( onRegExpItemList(item) );
			}
			case 61: // RegExpItemList = RegExpItemList RegExpItem
			{
				RegExpItemList list = (RegExpItemList) symbols[at].getValue();
				RegExpItem     item = (RegExpItem    ) symbols[at - 1].getValue();

				return symbol ( onRegExpItemList(list, item) );
			}
			case 62: // OptNUM =
			{
				return symbol( null );
			}
			case 63: // OptNUM = NUM
			{
				return copy( symbols[at] );
			}
		}
		throw new IndexOutOfBoundsException("unknown production #" + ruleNo);
	}

	protected SpecParser()
	{
		super(tables);
	}

	private static final ParsingTables tables;

	static {
		try
		{
			DataInputStream input = new DataInputStream(SpecParser.class.getResourceAsStream("SpecParser.tables"));
			try
			{
				tables = new ParsingTables(input);
			}
			finally
			{
				input.close();
			}
		}
		catch (IOException _)
		{
			throw new IllegalStateException("cannot load parsing tables");
		}
	}
}
