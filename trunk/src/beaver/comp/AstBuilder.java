/***
 * Beaver: compiler front-end construction toolkit
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>
 * All rights reserved.
 *
 * See the file "LICENSE" for the terms and conditions for copying,
 * distribution and modification of Beaver.
 */
package beaver.comp;

import beaver.comp.ast.NumTerm;
import beaver.comp.ast.Term;


/**
 * @author Alexander Demenchuk
 *
 */
public class AstBuilder extends SpecParser
{
	protected Object makeTerm(char id, String value)
	{
		if ( id == SpecParser.NUM )
		{
			return new NumTerm(value);
		}
		else
		{
			return new Term(value);
		}
	}
}
