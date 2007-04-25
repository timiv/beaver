/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp.lexer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import beaver.CharScanner;
import beaver.util.SubStr;

/**
 * Provides methods that assist in scanner building.
 * 
 * @author Alexander Demenchuk
 */
public class ScannerBuilder
{
	public static RegExp makeRule(int n, RegExp re)
	{
		return new RegExp.RuleOp(re, new RegExp.NullOp(), n);
	}

	public static RegExp makeRule(int n, RegExp re, String event)
	{
		return new RegExp.RuleOp(re, new RegExp.NullOp(), n, event);
	}

	public static RegExp makeMatchStringRule(int n, String str)
	{
		return new RegExp.RuleOp(strToRegExp(new SubStr(str)), new RegExp.NullOp(), n);
	}
	
	public static RegExp makeMatchEOLChar()
	{
		return rangeToRegExp("[\\n\\r]");
	}
	
//	public static RegExp makeEOL()
//	{
//		return new RegExp.AltOp(new RegExp.MatchCharOp('\n'),
//			   new RegExp.AltOp(new RegExp.MatchCharOp('\r'),
//			   new RegExp.AltOp(new RegExp.CatOp(new RegExp.MatchCharOp('\r'), new RegExp.MatchCharOp('\n')),
//		       new RegExp.AltOp(new RegExp.CatOp(new RegExp.MatchCharOp('\r'), new RegExp.MatchCharOp('\u0085')),
//			   new RegExp.AltOp(new RegExp.MatchCharOp('\u0085'), new RegExp.MatchCharOp('\u2028'))))));
//	}

	public static RegExp makeEOL()
	{
		return new RegExp.AltOp(new RegExp.MatchCharOp('\n'),
			   new RegExp.AltOp(new RegExp.MatchCharOp('\r'), 
					            new RegExp.CatOp(new RegExp.MatchCharOp('\r'), new RegExp.MatchCharOp('\n'))));
	}
	
	public static RegExp makeEndOfLineRule()
	{
		return makeRule(-1, makeEOL());
	}

	public static RegExp makeEndOfFileRule()
	{
		return makeRule(-2, new RegExp.MatchCharOp(CharScanner.EOF_SENTINEL));
	}
	
	public static RegExp rangeToRegExp(SubStr str)
	{
		if (str.isEmpty())
			return new RegExp.NullOp();
		
		Range r = Range.compile(str);
		if (r == null)
			return new RegExp.NullOp();
		
		return new RegExp.MatchRangeOp(r);
	}
	
	public static RegExp rangeToRegExp(String str)
	{
		SubStr txt = new SubStr(str);
		txt.trim1();
		int marker = txt.getMark();
		if ( txt.readChar() == '^' )
		{
			return invRangeToRegExp(txt);
		}
		txt.reset(marker);	
		return rangeToRegExp(txt);
	}
	
	public static RegExp invRangeToRegExp(SubStr str)
	{
		Range r = new Range('\0', '\ufffe');
		
		if (!str.isEmpty())
		{
			Range x = Range.compile(str);
			if (x != null)
			{
				r = Range.minus(r, x);
			}
		}
		return new RegExp.MatchRangeOp(r);
	}
	
	public static RegExp strToRegExp(SubStr str)
	{
		if ( str.isEmpty() )
			return new RegExp.NullOp();
		
		RegExp re = new RegExp.MatchCharOp(str.readNextChar());
		while( !str.isEmpty() )
		{
			re = new RegExp.CatOp(re, new RegExp.MatchCharOp(str.readNextChar()));
		}
		return re;
	}
	
	public static RegExp strToRegExp(String str)
	{
		return strToRegExp(new SubStr(str));
	}

	public static RegExp strToCaseInsensitiveRegExp(SubStr str)
	{
		if ( str.isEmpty() )
			return new RegExp.NullOp();
		
		RegExp re = ScannerBuilder.matchCharNoCase(str.readNextChar());
		while( !str.isEmpty() )
		{
			re = new RegExp.CatOp(re, ScannerBuilder.matchCharNoCase(str.readNextChar()));
		}
		return re;
	}

	private static RegExp matchCharNoCase(char c)
	{
		char u = c, l = c, t = c;
		switch (Character.getType(c))
		{
			case Character.UPPERCASE_LETTER:
			{
				l = Character.toLowerCase(c);
				t = Character.toTitleCase(c);
				break;
			}
			case Character.LOWERCASE_LETTER:
			{
				u = Character.toUpperCase(c);
				t = Character.toTitleCase(c);
				break;
			}
			case Character.TITLECASE_LETTER:
			{
				l = Character.toLowerCase(c);
				u = Character.toUpperCase(c);
			}
		}
		if (l == u)
			return new RegExp.MatchCharOp(c);
		else if (u == t)
			return new RegExp.AltOp(new RegExp.MatchCharOp(u), new RegExp.MatchCharOp(l));
		else
			return new RegExp.AltOp(new RegExp.MatchCharOp(u), new RegExp.AltOp(new RegExp.MatchCharOp(l), new RegExp.MatchCharOp(t)));
	}
	
	public static DFA compile(Collection rules)
	{
		Iterator i  = rules.iterator();
		if ( i.hasNext() )
		{
			RegExp re = (RegExp) i.next();
			while ( i.hasNext() )
			{
				re = new RegExp.AltOp( re, (RegExp) i.next() );
			}
			CharSet cs = new CharSet(re);
			NFA nfa = new NFA(re, cs);
			return new DFA(nfa, cs);
		}
		throw new IllegalArgumentException("no rules");
	}
	
	public static void saveClass(File dir, String name, byte[] bc) throws IOException
	{
		File classFile = new File(dir, name + ".class");
		File packageDir = classFile.getParentFile();
		if (!packageDir.exists())
		{
			packageDir.mkdirs();
		}
		java.io.FileOutputStream out = new java.io.FileOutputStream(classFile);
		try
		{
			out.write(bc);
		}
		finally
		{
			out.close();
		}
	}

}
