/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp;

import beaver.Location;

/**
 * @author Alexander Demenchuk
 *
 */
public interface Log
{
	void error(Location where, String descr);
	void error(String descr);
	void warning(Location where, String descr);
	void warning(String descr);
	void information(String descr);
}
