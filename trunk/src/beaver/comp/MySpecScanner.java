/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp;

import java.io.Reader;

/**
 * @author Alexander Demenchuk
 * 
 */
class MySpecScanner extends SpecScanner
{
	MySpecScanner(Reader srcReader)
	{
		super(srcReader);
	}

	protected boolean onScannerSpecStart()
	{
		super.state = SCANNER_SPEC;
		return true;
	}
}
