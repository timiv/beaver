/**
 * Beaver: compiler front-end construction toolkit
 * Copyright (c) 2007 Alexander Demenchuk <alder@softanvil.com>
 * All rights reserved.
 *
 * See the file "LICENSE" for the terms and conditions for copying,
 * distribution and modification of Beaver.
 */
package beaver.comp.spec;

/**
 * @author <a href="http://beaver.sourceforge.net">Beaver</a> parser generator
 */
public interface NodeVisitor
{
	void visit(Alt            node);
	void visit(AltList        node);
	void visit(ItemInline     node);
	void visit(ItemList       node);
	void visit(ItemStatic     node);
	void visit(ItemSymbol     node);
	void visit(ParserSpec     node);
	void visit(PrecItemList   node);
	void visit(PrecItemRule   node);
	void visit(PrecItemTerm   node);
	void visit(Precedence     node);
	void visit(PrecedenceList node);
	void visit(Rule           node);
	void visit(RuleList       node);
	void visit(Spec           node);
	void visit(Term           node);
}
