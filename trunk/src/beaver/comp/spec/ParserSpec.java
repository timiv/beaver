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
public class ParserSpec extends beaver.util.Node
{
	public RuleList       ruleList;
	public PrecedenceList precedenceDecl;

	public ParserSpec(RuleList ruleList, PrecedenceList precedenceDecl)
	{
		this.ruleList       = ruleList;
		this.precedenceDecl = precedenceDecl;
	}

	public void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
