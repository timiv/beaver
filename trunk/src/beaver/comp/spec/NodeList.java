package beaver.comp.spec;

/**
 * List of nodes.
 *  
 * @author Alexander Demenchuk
 */
public class NodeList extends Node
{
	private int length;
	protected ListElement root = new ListElement();
	// first := root.next
	// last  := root.prev
	
	protected NodeList(Node node)
	{
		root.next = root.prev = node;
		node.next = node.prev = root;
		length = 1;
	}
	
	public void add(Node node)
	{
		root.prev = (node.prev = (node.next = root).prev).next = node;
		length++;
	}
	
	public int length()
	{
		return length;
	}
}