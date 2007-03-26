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

import beaver.comp.ast.*;

/**
 * @author <a href="http://beaver.sourceforge.net">Beaver</a> parser generator
 */
public abstract class SpecParser extends beaver.Parser
{
	public static final char EOF            = '\000';
	public static final char MACROS_KWD     = '\001'; // "%macros"
	public static final char PRECEDENCE_KWD = '\002'; // "%precedence"
	public static final char RULES_KWD      = '\003'; // "%rules"
	public static final char TOKENS_KWD     = '\004'; // "%tokens"
	public static final char LPAREN         = '\005'; // "("
	public static final char RPAREN         = '\006'; // ")"
	public static final char COMMA          = '\007'; // ","
	public static final char FSLASH         = '\010'; // "/"
	public static final char COLON          = '\011'; // ":"
	public static final char SEMI           = '\012'; // ";"
	public static final char EQ             = '\013'; // "="
	public static final char BSLASH         = '\014'; // "\\"
	public static final char LCURL          = '\015'; // "{"
	public static final char BAR            = '\016'; // "|"
	public static final char RCURL          = '\017'; // "}"
	public static final char OPER           = '\020';
	public static final char NUM            = '\021';
	public static final char ASSOC          = '\022';
	public static final char NAME           = '\023';
	public static final char TEXT           = '\024';
	public static final char RANGE          = '\025';


	protected Spec onSpec(ParserSpec parserSpec, ScannerSpec scannerSpec)
	{
		return new Spec(parserSpec, scannerSpec);
	}

	protected ParserSpec onParserSpec(RuleList ruleList, PrecedenceList precedenceDecl)
	{
		return new ParserSpec(ruleList, precedenceDecl);
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

	protected Item onItemSymbol(Term ref, Term name, Term oper)
	{
		return new ItemSymbol(ref, name, oper);
	}

	protected Item onItemInline(Term name, ItemList itemList, Term oper)
	{
		return new ItemInline(name, itemList, oper);
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

	protected ScannerSpec onScannerSpec(MacroDeclList macros, TermDeclList terminals)
	{
		return new ScannerSpec(macros, terminals);
	}

	protected MacroDecl onMacroDecl(Term name, RegExp regExp)
	{
		return new MacroDecl(name, regExp);
	}

	protected TermDecl onTermDecl(Term name, RegExp regExp, Context context)
	{
		return new TermDecl(name, regExp, context);
	}

	protected Context onContext(RegExp regExp)
	{
		return new Context(regExp);
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

	protected ItemList onItemList(Item item)
	{
		return new ItemList().add(item);
	}

	protected ItemList onItemList(ItemList list, Item item)
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

	protected TermDeclList onTermDeclList(TermDecl item)
	{
		return new TermDeclList().add(item);
	}

	protected TermDeclList onTermDeclList(TermDeclList list, TermDecl item)
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
			case  1: // ParserSpec = "%rules" RuleList OptPrecedenceDecl
			{
				RuleList       ruleList       = (RuleList      ) symbols[at - 1].getValue();
				PrecedenceList precedenceDecl = (PrecedenceList) symbols[at - 2].getValue();

				return symbol ( onParserSpec(ruleList, precedenceDecl) );
			}
			case  2: // Rule = NAME "=" AltList ";"
			{
				Term    name    = (Term   ) symbols[at].getValue();
				AltList altList = (AltList) symbols[at - 2].getValue();

				return symbol ( onRule(name, altList) );
			}
			case  3: // AltList = Alt
			{
				Alt alt = (Alt) symbols[at].getValue();

				return symbol ( onAltList(alt) );
			}
			case  4: // AltList = AltList "|" Alt
			{
				AltList altList = (AltList) symbols[at].getValue();
				Alt     alt     = (Alt    ) symbols[at - 2].getValue();

				return symbol ( onAltList(altList, alt) );
			}
			case  5: // Alt = OptItemList
			{
				ItemList itemList = (ItemList) symbols[at].getValue();

				return symbol ( onAlt(itemList) );
			}
			case  6: // Alt = "{" NAME "}" OptItemList
			{
				Term     name     = (Term    ) symbols[at - 1].getValue();
				ItemList itemList = (ItemList) symbols[at - 3].getValue();

				return symbol ( onAlt(name, itemList) );
			}
			case  7: // Item = TEXT
			{
				Term text = (Term) symbols[at].getValue();

				return symbol ( onItemStatic(text) );
			}
			case  8: // Item = NAME OptOPER
			{
				Term name = (Term) symbols[at].getValue();
				Term oper = (Term) symbols[at - 1].getValue();

				return symbol ( onItemSymbol(name, oper) );
			}
			case  9: // Item = NAME ":" NAME OptOPER
			{
				Term ref  = (Term) symbols[at].getValue();
				Term name = (Term) symbols[at - 2].getValue();
				Term oper = (Term) symbols[at - 3].getValue();

				return symbol ( onItemSymbol(ref, name, oper) );
			}
			case 10: // Item = NAME ":" "(" ItemList ")" OPER
			{
				Term     name     = (Term    ) symbols[at].getValue();
				ItemList itemList = (ItemList) symbols[at - 3].getValue();
				Term     oper     = (Term    ) symbols[at - 5].getValue();

				return symbol ( onItemInline(name, itemList, oper) );
			}
			case 11: // PrecedenceDecl = "%precedence" PrecedenceList
			{
				return copy( symbols[at - 1] );
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
			case 17: // ScannerSpec = OptMacros "%tokens" TermDeclList
			{
				MacroDeclList macros    = (MacroDeclList) symbols[at].getValue();
				TermDeclList  terminals = (TermDeclList ) symbols[at - 2].getValue();

				return symbol ( onScannerSpec(macros, terminals) );
			}
			case 18: // Macros = "%macros" MacroDeclList
			{
				return copy( symbols[at - 1] );
			}
			case 19: // MacroDecl = NAME "=" RegExp ";"
			{
				Term   name   = (Term  ) symbols[at].getValue();
				RegExp regExp = (RegExp) symbols[at - 2].getValue();

				return symbol ( onMacroDecl(name, regExp) );
			}
			case 20: // TermDecl = NAME "=" RegExp OptContext ";"
			{
				Term    name    = (Term   ) symbols[at].getValue();
				RegExp  regExp  = (RegExp ) symbols[at - 2].getValue();
				Context context = (Context) symbols[at - 3].getValue();

				return symbol ( onTermDecl(name, regExp, context) );
			}
			case 21: // Context = "/" RegExp
			{
				RegExp regExp = (RegExp) symbols[at - 1].getValue();

				return symbol ( onContext(regExp) );
			}
			case 22: // RegExp = RegExpItemList
			{
				RegExpItemList regExpItemList = (RegExpItemList) symbols[at].getValue();

				return symbol ( onRegExp(regExpItemList) );
			}
			case 23: // RegExp = RegExp "|" RegExpItemList
			{
				RegExp         regExp         = (RegExp        ) symbols[at].getValue();
				RegExpItemList regExpItemList = (RegExpItemList) symbols[at - 2].getValue();

				return symbol ( onRegExp(regExp, regExpItemList) );
			}
			case 24: // RegExpItem = CharExpr
			{
				CharExpr charExpr = (CharExpr) symbols[at].getValue();

				return symbol ( onRegExpItem(charExpr) );
			}
			case 25: // RegExpItem = CharExpr OPER
			{
				CharExpr charExpr = (CharExpr) symbols[at].getValue();
				Term     oper     = (Term    ) symbols[at - 1].getValue();

				return symbol ( onRegExpItemClose(charExpr, oper) );
			}
			case 26: // RegExpItem = CharExpr Quantifier
			{
				CharExpr   charExpr   = (CharExpr  ) symbols[at].getValue();
				Quantifier quantifier = (Quantifier) symbols[at - 1].getValue();

				return symbol ( onRegExpItemQuant(charExpr, quantifier) );
			}
			case 27: // Quantifier = "{" NUM "}"
			{
				NumTerm min = (NumTerm) symbols[at - 1].getValue();

				return symbol ( onQuantifier(min) );
			}
			case 28: // Quantifier = "{" NUM "," OptNUM "}"
			{
				NumTerm min = (NumTerm) symbols[at - 1].getValue();
				NumTerm max = (NumTerm) symbols[at - 3].getValue();

				return symbol ( onQuantifier(min, max) );
			}
			case 29: // CharExpr = TEXT
			{
				Term text = (Term) symbols[at].getValue();

				return symbol ( onCharExprText(text) );
			}
			case 30: // CharExpr = RangeExpr
			{
				RangeExpr rangeExpr = (RangeExpr) symbols[at].getValue();

				return symbol ( onCharExprRange(rangeExpr) );
			}
			case 31: // CharExpr = "(" RegExp ")"
			{
				RegExp regExp = (RegExp) symbols[at - 1].getValue();

				return symbol ( onCharExprNested(regExp) );
			}
			case 32: // RangeExpr = RANGE
			{
				Term range = (Term) symbols[at].getValue();

				return symbol ( onRangeExprRange(range) );
			}
			case 33: // RangeExpr = NAME
			{
				Term macro = (Term) symbols[at].getValue();

				return symbol ( onRangeExprMacro(macro) );
			}
			case 34: // RangeExpr = RangeExpr "\\" RangeExpr
			{
				RangeExpr diff  = (RangeExpr) symbols[at].getValue();
				RangeExpr range = (RangeExpr) symbols[at - 2].getValue();

				return symbol ( onRangeExprMinus(diff, range) );
			}
			case 35: // OptParserSpec =
			{
				return symbol( null );
			}
			case 36: // OptParserSpec = ParserSpec
			{
				return copy( symbols[at] );
			}
			case 37: // OptScannerSpec =
			{
				return symbol( null );
			}
			case 38: // OptScannerSpec = ScannerSpec
			{
				return copy( symbols[at] );
			}
			case 39: // RuleList = Rule
			{
				Rule item = (Rule) symbols[at].getValue();

				return symbol ( onRuleList(item) );
			}
			case 40: // RuleList = RuleList Rule
			{
				RuleList list = (RuleList) symbols[at].getValue();
				Rule     item = (Rule    ) symbols[at - 1].getValue();

				return symbol ( onRuleList(list, item) );
			}
			case 41: // OptPrecedenceDecl =
			{
				return symbol( null );
			}
			case 42: // OptPrecedenceDecl = PrecedenceDecl
			{
				return copy( symbols[at] );
			}
			case 43: // ItemList = Item
			{
				Item item = (Item) symbols[at].getValue();

				return symbol ( onItemList(item) );
			}
			case 44: // ItemList = ItemList Item
			{
				ItemList list = (ItemList) symbols[at].getValue();
				Item     item = (Item    ) symbols[at - 1].getValue();

				return symbol ( onItemList(list, item) );
			}
			case 45: // OptItemList =
			{
				return symbol( null );
			}
			case 46: // OptItemList = ItemList
			{
				return copy( symbols[at] );
			}
			case 47: // OptOPER =
			{
				return symbol( null );
			}
			case 48: // OptOPER = OPER
			{
				return copy( symbols[at] );
			}
			case 49: // PrecedenceList = Precedence
			{
				Precedence item = (Precedence) symbols[at].getValue();

				return symbol ( onPrecedenceList(item) );
			}
			case 50: // PrecedenceList = PrecedenceList Precedence
			{
				PrecedenceList list = (PrecedenceList) symbols[at].getValue();
				Precedence     item = (Precedence    ) symbols[at - 1].getValue();

				return symbol ( onPrecedenceList(list, item) );
			}
			case 51: // OptMacros =
			{
				return symbol( null );
			}
			case 52: // OptMacros = Macros
			{
				return copy( symbols[at] );
			}
			case 53: // TermDeclList = TermDecl
			{
				TermDecl item = (TermDecl) symbols[at].getValue();

				return symbol ( onTermDeclList(item) );
			}
			case 54: // TermDeclList = TermDeclList TermDecl
			{
				TermDeclList list = (TermDeclList) symbols[at].getValue();
				TermDecl     item = (TermDecl    ) symbols[at - 1].getValue();

				return symbol ( onTermDeclList(list, item) );
			}
			case 55: // MacroDeclList = MacroDecl
			{
				MacroDecl item = (MacroDecl) symbols[at].getValue();

				return symbol ( onMacroDeclList(item) );
			}
			case 56: // MacroDeclList = MacroDeclList MacroDecl
			{
				MacroDeclList list = (MacroDeclList) symbols[at].getValue();
				MacroDecl     item = (MacroDecl    ) symbols[at - 1].getValue();

				return symbol ( onMacroDeclList(list, item) );
			}
			case 57: // OptContext =
			{
				return symbol( null );
			}
			case 58: // OptContext = Context
			{
				return copy( symbols[at] );
			}
			case 59: // RegExpItemList = RegExpItem
			{
				RegExpItem item = (RegExpItem) symbols[at].getValue();

				return symbol ( onRegExpItemList(item) );
			}
			case 60: // RegExpItemList = RegExpItemList RegExpItem
			{
				RegExpItemList list = (RegExpItemList) symbols[at].getValue();
				RegExpItem     item = (RegExpItem    ) symbols[at - 1].getValue();

				return symbol ( onRegExpItemList(list, item) );
			}
			case 61: // OptNUM =
			{
				return symbol( null );
			}
			case 62: // OptNUM = NUM
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
