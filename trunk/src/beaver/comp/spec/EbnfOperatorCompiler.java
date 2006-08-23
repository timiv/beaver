/**
 * 
 */
package beaver.comp.spec;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Alexander Demenchuk
 *
 */
public class EbnfOperatorCompiler extends TreeWalker
{
	private RuleList rules;
	private Set addedRules = new HashSet();
	
	public void visit(Spec node)
	{
		rules = node.rules;
		super.visit(node);
	}
	
	public void visit(ItemSymbol node)
	{
		if (node.operator == null)
			return;
		
		switch (node.operator.text.charAt(0))
		{
			case '?':
			{
				node.symName.text = getOptionalSymbolName(node.symName.text);
				break;
			}
			case '+':
			{
				node.symName.text = getListSymbolName(node.symName.text);
				break;
			}
			case '*':
			{
				node.symName.text = getOptionalSymbolName(getListSymbolName(node.symName.text));
				break;
			}
			default:
				throw new IllegalStateException("Unrecognized EBNF operator - " + node.operator.text + " on " + node.symName.text);
		}
		node.operator = null;
	}
	
	/**
	 * Creates an OptSym rule for a symbol Sym
	 * 
	 * @param symName name of the symbol
	 * @return name of the optional symbol
	 */
	private String getOptionalSymbolName(String symName)
	{
		String optSymName = "Opt" + symName;
		if (!addedRules.contains(optSymName))
		{
			rules.add(
				new Rule(
					new Term(optSymName),
					new AltList(
						new Alt(
							new ItemList()
						)
					).add(
						new Alt(
							new ItemList(
								new ItemSymbol(new Term("item"), new Term(symName), null)
							)
						)
					)
				)
			);
			addedRules.add(optSymName);
		}
		return optSymName;
	}
	
	/**
	 * Create a SymList rule for a symbol Sym
	 * 
	 * @param symName name of the symbol that is an element of the list
	 * @return name of the symbol that represent a list
	 */
	private String getListSymbolName(String symName)
	{
		String lstSymName = symName + "List";
		if (!addedRules.contains(lstSymName))
		{
			rules.add(
				new Rule(
					new Term(lstSymName),
					new AltList(
						new Alt(
							new ItemList(
								new ItemSymbol(new Term("item"), new Term(symName), null)
							)
						)
					).add(
						new Alt(
							new ItemList(
								new ItemSymbol(new Term("list"), new Term(lstSymName), null)
							).add(
								new ItemSymbol(new Term("item"), new Term(symName), null)
							)
						)
					)
				)
			);
			addedRules.add(lstSymName);
		}
		return lstSymName;
	}
}
