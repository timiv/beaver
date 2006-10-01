/**
 * 
 */
package beaver.comp;

import java.util.HashSet;
import java.util.Set;

import beaver.comp.spec.Rule;
import beaver.comp.spec.TreeWalker;

/**
 * @author Alexander Demenchuk
 *
 */
public class NonTerminalSymbolNamesCollector extends TreeWalker
{
	private Set names = new HashSet();

	public void visit(Rule rule)
    {
		names.add(rule.name.text);
    }

	Set getNames()
	{
		return names;
	}
}
