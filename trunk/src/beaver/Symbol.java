package beaver;

/**
 * Represents a symbol of a grammar.
 * Note:
 *  Symbols are abstract entities that are manipulated by the parser,
 *  they should not be referenced directly by the implementation.
 *
 * @author Alexander Demenchuk
 */
public final class Symbol
{
	/**
	 * Next symbol in a singly linked list of available symbols in the symbol pool.
	 */
	Symbol next;

	/**
	 * Symbol (terminal or non-terminal) ID.
	 */
	char id;
	
	/**
	 * Payload - a value associated with the symbol.
	 */
	Object value;
	
	/**
	 * A line where the first character of the symbol is found.
	 */
	int startLine;
	
	/**
	 * A columns where the first character of the symbol is found. 
	 */
	int startColumn;
	
	/**
	 * A line where the last character of the symbol is found.
	 */
	int endLine;
	
	/**
	 * A position of the next to last character of the symbol. 
	 */
	int endColumn;
	
	/**
	 * Creates new instance.
	 * Note: it is defined (at the package access level) to enforce allocatiion of new symbols
	 * though Parser's alloc method.
	 * 
	 * @see Parser.alloc()
	 * 
	 * @param id symbol ID
	 */
	Symbol()
	{
	}
	
	/**
	 * Sets the location of a symbol and, if symbol's value implements
	 * the Location protocol, updates its position. 
	 */
	void setLocation(int line, int column, int endLine, int endColumn)
	{
		this.startLine   = line;
		this.startColumn = column;
		this.endLine     = endLine;
		this.endColumn   = endColumn;
		
		if (this.value instanceof Location)
		{
			((Location) this.value).setLocation(line, column, endLine, endColumn);
		}
	}
	
	/**
	 * @return symbol's payload
	 */
	public Object getValue()
	{
		return value;
	}
}
