/**
 * 
 */
package beaver.comp.spec;

import beaver.Location;

/**
 * Base class for all tree elements.
 * 
 * @author Alexander Demenchuk
 */
public class Node extends ListElement implements Location
{
	/**
	 * A line where the first character of the symbol is found.
	 */
	int line;
	
	/**
	 * A columns where the first character of the symbol is found. 
	 */
	int column;
	
	/**
	 * A line where the last character of the symbol is found.
	 */
	int endLine;
	
	/**
	 * A position of the next to last character of the symbol. 
	 */
	int endColumn;
	
	/**
	 * @see beaver.Location#setLocation(int, int, int, int)
	 */
	public void setLocation(int line, int column, int endLine, int endColumn)
	{
		this.line = line;
		this.column = column;
		this.endLine = endLine;
		this.endColumn = endColumn;
	}
}
