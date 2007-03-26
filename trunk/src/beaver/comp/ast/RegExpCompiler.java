/***
 * Beaver: compiler front-end construction toolkit
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */

package beaver.comp.ast;

/**
 * @author Alexander Demenchuk
 *
 */
public interface RegExpCompiler
{
	beaver.comp.lexer.RegExp compile(CharExprNested  expr);
	beaver.comp.lexer.RegExp compile(CharExprRange   expr);
	beaver.comp.lexer.RegExp compile(CharExprText    expr);
	beaver.comp.lexer.RegExp compile(Context         expr);
	beaver.comp.lexer.RegExp compile(RangeExprMinus  expr);
	beaver.comp.lexer.RegExp compile(RangeExprMacro  expr);
	beaver.comp.lexer.RegExp compile(RangeExprRange  expr);
	beaver.comp.lexer.RegExp compile(RegExp          expr);
	beaver.comp.lexer.RegExp compile(RegExpItem      expr);
	beaver.comp.lexer.RegExp compile(RegExpItemClose expr);
	beaver.comp.lexer.RegExp compile(RegExpItemQuant expr);
	beaver.comp.lexer.RegExp compile(RegExpItemList  expr);
}
