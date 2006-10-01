/**
 * 
 */
package beaver.comp.parser;

/**
 * Terminals 
 * 
 * @author Alexander Demenchuk
 */
public class Terminal extends Symbol
{
	/**
	 * Precedence: 0 - undefined (lowest), 0xffff - highest
	 */
	char precedence;
	
	/**
	 * 'L' - left, 'R' - right, 'N' - none
	 */
	char accociativity;

	public Terminal(char id, String name)
	{
		super(id, name);
	}

}
