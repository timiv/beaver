/**
 * Beaver: compiler front-end construction toolkit
 * Copyright (c) 2007 Alexander Demenchuk <alder@softanvil.com>
 * All rights reserved.
 *
 * See the file "LICENSE" for the terms and conditions for copying,
 * distribution and modification of Beaver.
 */
package beaver.comp.spec;

import java.io.DataInputStream;
import java.io.IOException;

import beaver.ParsingTables;
import beaver.Symbol;

/**
 * @author <a href="http://beaver.sourceforge.net">Beaver</a> parser generator
 */
public abstract class SpecParser extends beaver.Parser
{
	public static final char EOF            = '\000';
	public static final char PRECEDENCE_KWD = '\001'; // "%precedence"
	public static final char RULES_KWD      = '\002'; // "%rules"
	public static final char LPAREN         = '\003'; // "("
	public static final char RPAREN         = '\004'; // ")"
	public static final char COMMA          = '\005'; // ","
	public static final char COLON          = '\006'; // ":"
	public static final char SEMI           = '\007'; // ";"
	public static final char EQ             = '\010'; // "="
	public static final char LCURL          = '\011'; // "{"
	public static final char BAR            = '\012'; // "|"
	public static final char RCURL          = '\013'; // "}"
	public static final char ASSOC          = '\014';
	public static final char NAME           = '\015';
	public static final char OPER           = '\016';
	public static final char TEXT           = '\017';

	protected abstract Spec        onSpec         (ParserSpec parserSpec);
	protected abstract ParserSpec  onParserSpec   (RuleList ruleList, PrecedenceList precedenceDecl);
	protected abstract Rule        onRule         (Term name, AltList altList);
	protected abstract Alt         onAlt          (ItemList itemList);
	protected abstract Alt         onAlt          (Term name, ItemList itemList);
	protected abstract Item        onItemStatic   (Term text);
	protected abstract Item        onItemSymbol   (Term name, Term oper);
	protected abstract Item        onItemSymbol   (Term ref, Term name, Term oper);
	protected abstract Item        onItemInline   (Term name, ItemList itemList, Term oper);
	protected abstract Precedence  onPrecedence   (PrecItemList precItemList, Term assoc);
	protected abstract PrecItem    onPrecItemTerm (Term text);
	protected abstract PrecItem    onPrecItemRule (Term name);

	protected AltList  onAltList (Alt alt)
	{
		return new AltList().add(alt);
	}

	protected AltList  onAltList (AltList altList, Alt alt)
	{
		return altList.add(alt);
	}

	protected PrecItemList  onPrecItemList (PrecItem precItem)
	{
		return new PrecItemList().add(precItem);
	}

	protected PrecItemList  onPrecItemList (PrecItemList precItemList, PrecItem precItem)
	{
		return precItemList.add(precItem);
	}

	protected RuleList  onRuleList (Rule item)
	{
		return new RuleList().add(item);
	}

	protected RuleList  onRuleList (RuleList list, Rule item)
	{
		return list.add(item);
	}

	protected ItemList  onItemList (Item item)
	{
		return new ItemList().add(item);
	}

	protected ItemList  onItemList (ItemList list, Item item)
	{
		return list.add(item);
	}

	protected PrecedenceList  onPrecedenceList (Precedence item)
	{
		return new PrecedenceList().add(item);
	}

	protected PrecedenceList  onPrecedenceList (PrecedenceList list, Precedence item)
	{
		return list.add(item);
	}

	protected Symbol reduce(Symbol[] symbols, int at, int ruleNo)
	{
		switch (ruleNo)
		{
			case  0: // Spec = OptParserSpec
			{
				ParserSpec parserSpec = (ParserSpec) symbols[at - 0].getValue();

				return symbol ( onSpec(parserSpec) );
			}
			case  1: // ParserSpec = "%rules" RuleList OptPrecedenceDecl
			{
				RuleList       ruleList       = (RuleList      ) symbols[at - 1].getValue();
				PrecedenceList precedenceDecl = (PrecedenceList) symbols[at - 2].getValue();

				return symbol ( onParserSpec(ruleList, precedenceDecl) );
			}
			case  2: // Rule = NAME "=" AltList ";"
			{
				Term    name    = (Term   ) symbols[at - 0].getValue();
				AltList altList = (AltList) symbols[at - 2].getValue();

				return symbol ( onRule(name, altList) );
			}
			case  3: // AltList = Alt
			{
				Alt alt = (Alt) symbols[at - 0].getValue();

				return symbol ( onAltList(alt) );
			}
			case  4: // AltList = AltList "|" Alt
			{
				AltList altList = (AltList) symbols[at - 0].getValue();
				Alt     alt     = (Alt    ) symbols[at - 2].getValue();

				return symbol ( onAltList(altList, alt) );
			}
			case  5: // Alt = OptItemList
			{
				ItemList itemList = (ItemList) symbols[at - 0].getValue();

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
				Term text = (Term) symbols[at - 0].getValue();

				return symbol ( onItemStatic(text) );
			}
			case  8: // Item = NAME OptOPER
			{
				Term name = (Term) symbols[at - 0].getValue();
				Term oper = (Term) symbols[at - 1].getValue();

				return symbol ( onItemSymbol(name, oper) );
			}
			case  9: // Item = NAME ":" NAME OptOPER
			{
				Term ref  = (Term) symbols[at - 0].getValue();
				Term name = (Term) symbols[at - 2].getValue();
				Term oper = (Term) symbols[at - 3].getValue();

				return symbol ( onItemSymbol(ref, name, oper) );
			}
			case 10: // Item = NAME ":" "(" ItemList ")" OPER
			{
				Term     name     = (Term    ) symbols[at - 0].getValue();
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
				PrecItemList precItemList = (PrecItemList) symbols[at - 0].getValue();
				Term         assoc        = (Term        ) symbols[at - 2].getValue();

				return symbol ( onPrecedence(precItemList, assoc) );
			}
			case 13: // PrecItemList = PrecItem
			{
				PrecItem precItem = (PrecItem) symbols[at - 0].getValue();

				return symbol ( onPrecItemList(precItem) );
			}
			case 14: // PrecItemList = PrecItemList "," PrecItem
			{
				PrecItemList precItemList = (PrecItemList) symbols[at - 0].getValue();
				PrecItem     precItem     = (PrecItem    ) symbols[at - 2].getValue();

				return symbol ( onPrecItemList(precItemList, precItem) );
			}
			case 15: // PrecItem = TEXT
			{
				Term text = (Term) symbols[at - 0].getValue();

				return symbol ( onPrecItemTerm(text) );
			}
			case 16: // PrecItem = NAME
			{
				Term name = (Term) symbols[at - 0].getValue();

				return symbol ( onPrecItemRule(name) );
			}
			case 17: // OptParserSpec =
			{
				return symbol( null );
			}
			case 18: // OptParserSpec = ParserSpec
			{
				return copy( symbols[at - 0] );
			}
			case 19: // RuleList = Rule
			{
				Rule item = (Rule) symbols[at - 0].getValue();

				return symbol ( onRuleList(item) );
			}
			case 20: // RuleList = RuleList Rule
			{
				RuleList list = (RuleList) symbols[at - 0].getValue();
				Rule     item = (Rule    ) symbols[at - 1].getValue();

				return symbol ( onRuleList(list, item) );
			}
			case 21: // OptPrecedenceDecl =
			{
				return symbol( null );
			}
			case 22: // OptPrecedenceDecl = PrecedenceDecl
			{
				return copy( symbols[at - 0] );
			}
			case 23: // ItemList = Item
			{
				Item item = (Item) symbols[at - 0].getValue();

				return symbol ( onItemList(item) );
			}
			case 24: // ItemList = ItemList Item
			{
				ItemList list = (ItemList) symbols[at - 0].getValue();
				Item     item = (Item    ) symbols[at - 1].getValue();

				return symbol ( onItemList(list, item) );
			}
			case 25: // OptItemList =
			{
				return symbol( null );
			}
			case 26: // OptItemList = ItemList
			{
				return copy( symbols[at - 0] );
			}
			case 27: // OptOPER =
			{
				return symbol( null );
			}
			case 28: // OptOPER = OPER
			{
				return copy( symbols[at - 0] );
			}
			case 29: // PrecedenceList = Precedence
			{
				Precedence item = (Precedence) symbols[at - 0].getValue();

				return symbol ( onPrecedenceList(item) );
			}
			case 30: // PrecedenceList = PrecedenceList Precedence
			{
				PrecedenceList list = (PrecedenceList) symbols[at - 0].getValue();
				Precedence     item = (Precedence    ) symbols[at - 1].getValue();

				return symbol ( onPrecedenceList(list, item) );
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
