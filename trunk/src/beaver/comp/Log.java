/**
 * 
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
