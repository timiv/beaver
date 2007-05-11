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

import beaver.comp.cst.CharExprText;
import beaver.comp.cst.RegExpCompiler;
import beaver.comp.cst.RegExpItem;
import beaver.comp.cst.RegExpItemClose;
import beaver.comp.cst.RegExpItemList;
import beaver.comp.cst.RegExpItemQuant;
import beaver.comp.cst.TermDecl;
import beaver.comp.cst.TermDeclList;

/**
 * @author Alexander Demenchuk
 *
 */
public class TokenCompiler extends BasicRegExpCompiler implements RegExpCompiler
{
	private Collection names  = new ArrayList();
	private Map        rules  = new HashMap();
	private Map        cTerms;
	private Log        log;
	
	public TokenCompiler(Map macros, Map constantTermNames, Log log)
	{
		super(macros);
		cTerms = constantTermNames;
		this.log = log;
	}
	
	public Map compile(TermDeclList terminals, String stateName)
	{
		if ( terminals != null )
		{
			for ( TermDecl item = (TermDecl) terminals.first(); item != null; item = (TermDecl) item.next() )
			{
				String name = item.name.toString(); 
				beaver.comp.lexer.RegExp exp = item.regExp.accept(this);
				beaver.comp.lexer.RegExp ctx;			
				if ( item.ctx != null )
				{
					ctx = item.ctx.accept(this);
				}
				else
				{
					ctx = new beaver.comp.lexer.RegExp.NullOp();
					//
					// Check if this rule overrides a reserved word rule 
					//
					if ( item.regExp.length() == 1 )
					{
						RegExpItemList rel = (RegExpItemList) item.regExp.first();
						if ( rel.length() == 1 )
						{
							RegExpItem rei = (RegExpItem) rel.first();
    						if ( rei.charExpr instanceof CharExprText && !(rei instanceof RegExpItemClose) && !(rei instanceof RegExpItemQuant) )
    						{
    							//
    							// Check naming "collisions"
    							//
    							String termText = ((CharExprText) rei.charExpr).text.text;
    							String prevName = (String) cTerms.get(termText);
    							if ( prevName != null && !prevName.equals(name) )
    							{
    		                    	log.error("State " + stateName + " changes name for terminal \"" + termText + "\" from " + prevName + " to " + name);
    		                    	//
    		                    	// ignore this rule
    		                    	//
    		                    	continue;
    							}
    							cTerms.put(termText, name);
    						}
						}
					}
				}
				rules.put(name, new beaver.comp.lexer.RegExp.RuleOp(exp, ctx, item.event == null ? null : item.event.text));
				names.add(name);
			}
		}
		return rules;
	}

	public Collection getNames()
	{
		return names;
	}
}
