/**
 * 
 */
package beaver.comp.parser;

/**
 * Represents symbols of a grammar.
 * 
 * @author Alexander Demenchuk
 */
public class Symbol
{
	/**
	 * Symbol's ID
	 */
	char id;
	
	/**
	 * String that is used to reference this symbol in the specification.
	 */
	String name;
	
	protected Symbol(char id, String name)
	{
		this.name = name;
	}
}
