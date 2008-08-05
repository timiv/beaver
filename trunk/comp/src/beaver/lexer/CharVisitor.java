package beaver.lexer;

/**
 * Protocol of the character collection processors.
 * 
 * @author Alexander Demenchuk
 *
 */
interface CharVisitor
{
	void visit(char c);
}
