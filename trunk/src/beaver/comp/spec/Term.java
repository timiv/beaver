/**
 * 
 */
package beaver.comp.spec;

/**
 * AST node that keeps values of recognized terminals.
 * 
 * @author Alexander Demenchuk
 *
 */
public class Term extends Node
{
	String text;
	
	Term(String value)
	{
		text = value;
	}
}
