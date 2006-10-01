/**
 * 
 */
package beaver.comp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import beaver.comp.parser.NonTerminal;
import beaver.comp.parser.Terminal;
import beaver.comp.spec.Alt;
import beaver.comp.spec.ItemString;
import beaver.comp.spec.ItemSymbol;
import beaver.comp.spec.Rule;
import beaver.comp.spec.TreeWalker;

/**
 * @author Alexander Demenchuk
 *
 */
public class GrammarBuilder extends TreeWalker
{
	private Map symbols = new HashMap();
	private NonTerminal prodLHS;
	
	public GrammarBuilder(Set constTerms, Set namedTerms, Set nonterms)
	{
		char id = 0;
		for (Iterator i = constTerms.iterator(); i.hasNext(); )
		{
			String name = (String) i.next();
			symbols.put(name, new Terminal(++id, name));
		}
		for (Iterator i = namedTerms.iterator(); i.hasNext(); )
		{
			String name = (String) i.next();
			symbols.put(name, new Terminal(++id, name));
		}
		for (Iterator i = nonterms.iterator(); i.hasNext(); )
		{
			String name = (String) i.next();
			symbols.put(name, new NonTerminal(++id, name));
		}
	}

	public void visit(Rule rule)
    {
		prodLHS = (NonTerminal) symbols.get(rule.name.text);
	    super.visit(rule);
    }

	public void visit(Alt alt)
    {
	    // TODO Implement method
	    super.visit(alt);
    }

	public void visit(ItemString item)
    {
	    // TODO Implement method
	    super.visit(item);
    }

	public void visit(ItemSymbol item)
    {
	    // TODO Implement method
	    super.visit(item);
    }

}
