/***
 * Beaver: compiler front-end construction toolkit
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */

package beaver.comp;

import java.util.HashMap;
import java.util.Map;

import beaver.comp.spec.MacroDecl;
import beaver.comp.spec.RegExpCompiler;
import beaver.comp.spec.ScannerSpec;


/**
 * @author Alexander Demenchuk
 *
 */
public class MacroCompiler extends BasicRegExpCompiler implements RegExpCompiler
{
	public MacroCompiler()
	{
		super(new HashMap());
	}
	
	public Map compile(ScannerSpec spec)
	{
		if ( ! macros.isEmpty() )
		{
			macros.clear();
		}
		
		if ( spec.macros != null )
		{
			for ( MacroDecl item = (MacroDecl) spec.macros.first(); item != null; item = (MacroDecl) item.next() )
			{
				macros.put(item.name.toString(), item.regExp.accept(this));
			}
		}
		return macros;
	}
}
