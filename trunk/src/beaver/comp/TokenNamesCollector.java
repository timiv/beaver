/**
 * 
 */
package beaver.comp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import beaver.comp.cst.CharExprNested;
import beaver.comp.cst.CharExprRange;
import beaver.comp.cst.CharExprText;
import beaver.comp.cst.RegExpItemClose;
import beaver.comp.cst.RegExpItemQuant;
import beaver.comp.cst.TermDecl;
import beaver.comp.cst.TreeWalker;

/**
 * @author Alexander Demenchuk
 *
 */
public class TokenNamesCollector extends TreeWalker
{
	private Collection names   = new ArrayList();
	private Collection marks   = new HashSet();
	private Map        aliases = new HashMap();
	private String     constTermText;
	private int        textCounter;

    public void visit(TermDecl node)
    {
    	String name = node.name.text;
    	if ( !marks.contains(name) )
    	{
    		names.add(name);
    		marks.add(name);
    	}
    	textCounter = 0;
	    super.visit(node);
	    if ( node.ctx == null && textCounter == 1 )
	    {
	    	aliases.put(constTermText, name);
	    }
    }

    public void visit(RegExpItemClose node)
    {
    	textCounter = 2;
	    super.visit(node);
    }

    public void visit(RegExpItemQuant node)
    {
    	textCounter = 2;
	    super.visit(node);
    }

	public void visit(CharExprNested node)
    {
    	textCounter = 2;
	    super.visit(node);
    }

    public void visit(CharExprRange node)
    {
    	textCounter = 2;
	    super.visit(node);
    }

    public void visit(CharExprText node)
    {
    	constTermText = node.text.text.substring(1, node.text.text.length() - 1);
    	textCounter++;
	    super.visit(node);
    }
    
    public Collection getNames()
    {
    	return names;
    }
    
    public Map getConstTermAliases()
    {
    	return aliases;
    }
}
