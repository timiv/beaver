/***
 * Beaver: compiler front-end construction toolkit
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */

package beaver.comp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import beaver.comp.cst.RegExpCompiler;
import beaver.comp.cst.ScannerSpec;
import beaver.comp.cst.TermDecl;

/**
 * @author Alexander Demenchuk
 *
 */
public class TokenCompiler extends BasicRegExpCompiler implements RegExpCompiler
{
	private Collection names = new ArrayList();
	private Map rules = new HashMap();
	
	public TokenCompiler(Map macros)
	{
		super(macros);
	}
	
	public Map compile(ScannerSpec spec)
	{
		if ( !names.isEmpty() )
		{
			names.clear();
		}
		if ( !rules.isEmpty() )
		{
			rules.clear();
		}
		
		if ( spec.terminals != null )
		{
			for ( TermDecl item = (TermDecl) spec.terminals.first(); item != null; item = (TermDecl) item.next() )
			{
				beaver.comp.lexer.RegExp exp = item.regExp.accept(this);
				beaver.comp.lexer.RegExp ctx = item.ctx != null ? item.ctx.accept(this) 
						                                        : new beaver.comp.lexer.RegExp.NullOp();
				names.add(item.name.toString());
				rules.put(item.name.toString(), new beaver.comp.lexer.RegExp.RuleOp(exp, ctx));
			}
		}
		return rules;
	}
	
	public String[] getTerminalNames()
	{
		return (String[]) names.toArray(new String[names.size()]);
	}
}
