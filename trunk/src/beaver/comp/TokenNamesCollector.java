/**
 * 
 */
package beaver.comp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import beaver.comp.cst.TermDecl;
import beaver.comp.cst.TreeWalker;

/**
 * @author Alexander Demenchuk
 *
 */
public class TokenNamesCollector extends TreeWalker
{
	private Collection names = new ArrayList();
	private Collection marks = new HashSet();

    public void visit(TermDecl node)
    {
    	String name = node.name.text;
    	if ( !marks.contains(name) )
    	{
    		names.add(name);
    		marks.add(name);
    	}
	    super.visit(node);
    }
    
    public String[] getNames()
    {
    	return (String[]) names.toArray(new String[names.size()]);
    }
}
