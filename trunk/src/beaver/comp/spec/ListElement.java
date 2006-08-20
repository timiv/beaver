/**
 * 
 */
package beaver.comp.spec;

/**
 * @author Alexander Demenchuk
 *
 */
public class ListElement
{

	/**
     * Next node in the doubly linked list of nodes
     */
    protected ListElement next;
	/**
     * Previous node in the list
     */
    protected ListElement prev;

	/**
     * Replaces this node in the list with the specified one.
     *
     */
    public void replaceWith(ListElement node)
    {
    	(node.next = this.next).prev = (node.prev = this.prev).next = node;
    }

}
