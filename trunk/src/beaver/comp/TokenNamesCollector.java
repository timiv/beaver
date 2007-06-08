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
import beaver.comp.cst.RegExpItemMulti;
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
	private Collection skips   = new ArrayList();
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
	    if ( node.event != null && node.event.text.equals("skip") )
	    {
	    	skips.add(name);
	    }
	    else if ( textCounter == 1 && node.ctx == null )
	    {
	    	aliases.put(constTermText, name);
	    }
    }

    public void visit(RegExpItemClose node)
    {
    	textCounter = 2;
	    super.visit(node);
    }

    public void visit(RegExpItemMulti node)
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
    
    public Collection getSkippableTermNames()
    {
    	return skips;
    }
}
