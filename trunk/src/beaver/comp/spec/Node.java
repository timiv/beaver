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
public class Node implements Location
{
	/**
	 * Next node in the doubly linked list of nodes
	 */
	protected Node next;
	
	/**
	 * Previous node in the list
	 */
	protected Node prev;
	
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
	
	/**
	 * Replaces this node in the list with the specified one.
	 *
	 */
	public void replaceWith(Node node)
	{
		node.next = this.next;
		node.prev = this.prev;
	}
	
	public void writeTo(StringBuilder str)
	{
		str.append(line).append(',').append(column).append("..").append(endLine).append(',').append(endColumn);
	}
}
