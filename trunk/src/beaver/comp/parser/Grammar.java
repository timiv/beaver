/**
 * 
 */
package beaver.comp.parser;


/**
 * @author Alexander Demenchuk
 */
public class Grammar
{
	/**
	 * Symbols that are created by parser, i.e. when a RHS of a production is reduced to a LHS.
	 */
	NonTerminal[] nonterminals;
	
	/**
	 * Symbols are created by a scanner and represent input tokens for the partser. 
	 */
	Terminal[] terminals;
	
	/**
	 * LHS symbol that is created by the very first production, a.k.a. the goal of the parser.
	 */
	NonTerminal goal;
	
}
