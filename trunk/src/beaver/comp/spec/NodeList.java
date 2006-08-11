package beaver.comp.spec;

/**
 * List of nodes.
 *  
 * @author Alexander Demenchuk
 */
public class NodeList extends Node
{
	private int length;
	// first := this.next
	// last  := this.prev
	
	protected NodeList(Node node)
	{
		this.next = this.prev = node;
		node.next = node.prev = this;
		length = 1;
	}
	
	public void add(Node node)
	{
		this.prev = (node.prev = (node.next = this).prev).next = node;
		length++;
	}
	
	public int length()
	{
		return length;
	}
	
	public Node end()
	{
		return this;
	}
}