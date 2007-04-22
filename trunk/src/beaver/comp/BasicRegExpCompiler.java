/***
 * Beaver: compiler front-end construction toolkit
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */

package beaver.comp;

import java.util.Map;

import beaver.comp.ast.CharExprNested;
import beaver.comp.ast.CharExprRange;
import beaver.comp.ast.CharExprText;
import beaver.comp.ast.RangeExprMacro;
import beaver.comp.ast.RangeExprMinus;
import beaver.comp.ast.RangeExprRange;
import beaver.comp.ast.RegExp;
import beaver.comp.ast.RegExpCompiler;
import beaver.comp.ast.RegExpItem;
import beaver.comp.ast.RegExpItemClose;
import beaver.comp.ast.RegExpItemList;
import beaver.comp.ast.RegExpItemQuant;
import beaver.comp.lexer.ScannerBuilder;
import beaver.util.SubStr;

/**
 * @author Alexander Demenchuk
 *
 */
public class BasicRegExpCompiler implements RegExpCompiler
{
	protected Map macros;
	
	protected BasicRegExpCompiler(Map macros)
	{
		this.macros = macros; 
	}

	public beaver.comp.lexer.RegExp compile(CharExprNested expr)
    {
	    return expr.regExp.accept(this);
    }

	public beaver.comp.lexer.RegExp compile(CharExprRange expr)
    {
	    return expr.rangeExpr.accept(this);
    }

	public beaver.comp.lexer.RegExp compile(CharExprText expr)
    {
		SubStr text = new SubStr(expr.text.toString());
		text.trim1(); // remove "
	    return ScannerBuilder.strToRegExp(text);
    }

	public beaver.comp.lexer.RegExp compile(RangeExprMacro expr)
    {
		String macroName = expr.macro.toString();
		beaver.comp.lexer.RegExp re = (beaver.comp.lexer.RegExp) macros.get(macroName);
		if ( re == null )
		{
			if ( macroName.equals("$") )
				re = ScannerBuilder.makeMatchEOLChar();
			else
				throw new IllegalStateException("reference to the undefined macro \"" + expr.macro + "\"");
		}
	    return re;
    }

	public beaver.comp.lexer.RegExp compile(RangeExprMinus expr)
    {
		beaver.comp.lexer.RegExp diff  = expr.diff.accept(this);
		if ( diff instanceof beaver.comp.lexer.RegExp.MatchRangeOp )
        {
			beaver.comp.lexer.RegExp range = expr.range.accept(this);
			if ( range instanceof beaver.comp.lexer.RegExp.MatchRangeOp )
			{
				diff = ((beaver.comp.lexer.RegExp.MatchRangeOp) diff).minus((beaver.comp.lexer.RegExp.MatchRangeOp) range);
			}
        }
	    return diff;
    }

	public beaver.comp.lexer.RegExp compile(RangeExprRange expr)
    {
	    return ScannerBuilder.rangeToRegExp(expr.range.toString());
    }

	public beaver.comp.lexer.RegExp compile(RegExp expr)
    {
		RegExpItemList item = (RegExpItemList) expr.first();
		if ( item == null )
		{
			return new beaver.comp.lexer.RegExp.NullOp();
		}

		beaver.comp.lexer.RegExp re = item.accept(this);
			
		for ( item = (RegExpItemList) item.next(); item != null; item = (RegExpItemList) item.next() )
		{
			re = new beaver.comp.lexer.RegExp.AltOp(re, item.accept(this));
		}
		return re;
    }

	public beaver.comp.lexer.RegExp compile(RegExpItem expr)
    {
	    return expr.charExpr.accept(this);
    }

	public beaver.comp.lexer.RegExp compile(RegExpItemClose expr)
    {
		beaver.comp.lexer.RegExp re = expr.charExpr.accept(this);
		
		switch ( expr.oper.toString().charAt(0) )
		{
			case '?':
			{
				return new beaver.comp.lexer.RegExp.AltOp(new beaver.comp.lexer.RegExp.NullOp(), re);
			}
			case '+':
			{
				return new beaver.comp.lexer.RegExp.CloseOp(re);
			}
			case '*':
			{
				return new beaver.comp.lexer.RegExp.AltOp(new beaver.comp.lexer.RegExp.NullOp(), new beaver.comp.lexer.RegExp.CloseOp(re));
			}
		}
		throw new IllegalStateException("Unknown EBNF operator '" + expr.oper + "'");
    }

	public beaver.comp.lexer.RegExp compile(RegExpItemList expr)
    {
		RegExpItem item = (RegExpItem) expr.first();
		if ( item == null )
		{
			return new beaver.comp.lexer.RegExp.NullOp();
		}
		beaver.comp.lexer.RegExp re = item.accept(this);
		for ( item = (RegExpItem) item.next(); item != null; item = (RegExpItem) item.next() )
		{
			re = new beaver.comp.lexer.RegExp.CatOp(re, item.accept(this));
		}
		return re;
    }

	public beaver.comp.lexer.RegExp compile(RegExpItemQuant expr)
    {
		return new beaver.comp.lexer.RegExp.CloseVOp(expr.charExpr.accept(this), expr.quantifier.min.value, expr.quantifier.max == null ? -1 : expr.quantifier.max.value);
    }
}
