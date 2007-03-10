/***
 * Beaver: compiler front-end construction toolkit
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>
 * All rights reserved.
 *
 * See the file "LICENSE" for the terms and conditions for copying,
 * distribution and modification of Beaver.
 */
package beaver.comp.spec;


/**
 * @author Alexander Demenchuk
 *
 */
public class AstBuilder extends SpecParser
{
	protected Spec onSpec(ParserSpec optParserSpec)
	{
		return new Spec(optParserSpec);
	}

	protected ParserSpec onParserSpec(RuleList ruleList, PrecedenceList optPrecedenceDecl)
	{
		return new ParserSpec(ruleList, optPrecedenceDecl);
	}

	protected Rule onRule(Term name, AltList alts)
	{
		return new Rule(name, alts);
	}

	protected Alt  onAlt(ItemList rhs)
	{
		return new Alt(rhs);
	}

	protected Alt  onAlt(Term name, ItemList rhs)
	{
		return new Alt(name, rhs);
	}

	protected Item onItemStatic(Term text)
	{
		return new ItemStatic(text);
	}

	protected Item onItemSymbol(Term name, Term operator)
	{
		return new ItemSymbol(name, operator);
	}

	protected Item onItemSymbol(Term ref, Term name, Term operator)
	{
		return new ItemSymbol(ref, name, operator);
	}

	protected Item onItemInline(Term ref, ItemList def, Term operator)
	{
		return new ItemInline(ref, def, operator);
	}

	protected PrecedenceList onPrecedenceDecl(PrecedenceList precedenceList)
	{
		return precedenceList;
	}

	protected Precedence onPrecedence(PrecItemList precItemList, Term assoc)
	{
		return new Precedence(precItemList, assoc);
	}

	protected PrecItem onPrecItemSymbol(Term name)
	{
		return new PrecItemSymbol(name);
	}

	protected PrecItem onPrecItemStatic(Term text)
	{
		return new PrecItemStatic(text);
	}

	protected Object makeTerm(char id, String value)
	{
		return new Term(value);
	}
}
