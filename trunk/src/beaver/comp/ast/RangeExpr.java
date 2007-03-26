/**
 * Beaver: compiler front-end construction toolkit
 * Copyright (c) 2007 Alexander Demenchuk <alder@softanvil.com>
 * All rights reserved.
 *
 * See the file "LICENSE" for the terms and conditions for copying,
 * distribution and modification of Beaver.
 */
package beaver.comp.ast;

/**
 * @author <a href="http://beaver.sourceforge.net">Beaver</a> parser generator
 */
public abstract class RangeExpr extends beaver.util.Node
{
	public abstract void accept(NodeVisitor visitor);
	public abstract beaver.comp.lexer.RegExp accept(RegExpCompiler compiler);
}
