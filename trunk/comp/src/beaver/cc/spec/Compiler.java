package beaver.cc.spec;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import beaver.CharScanner;
import beaver.Parser;
import beaver.SyntaxErrorException;

public class Compiler
{
	public static Spec parse(File specFile) throws IOException, SyntaxErrorException
    {
    	Reader specReader = new FileReader(specFile);
    	try
    	{
    		CharScanner lexer = new BeaverScanner(specReader);
    		Parser specParser = new BeaverParser();
    		return (Spec) specParser.parse(lexer);
    	}
    	finally
    	{
    		specReader.close();
    	}
    }
	
	/**
	 * Nonterminal symbols can be defined as
	 *  
	 *   Lhs = RhsA 
	 *       | RhsB
	 *       ;
	 *       
	 * or as
	 * 
	 *   Lhs = RhsA;
	 *   Lhs = RhsB;
	 *   
	 * or as any combination of the two
	 *   
	 * Here we'll look for a first nonterminal definition and will call it
	 * authoritative, then we'll search for any possible alternative definitions
	 * and collate them (append to alternatives to the authoritative list and
	 * remove AST nodes that defined those alternatives)     
	 * 
	 * @param rules
	 */
	public static void collateProductions(RuleList rules)
	{
		Map authDefLists = new HashMap();
		for (Rule prev = null, rule = rules.first; rule != null; rule = rule.next)
		{
			AltDefList authDefList = (AltDefList) authDefLists.get(rule.id.value);
			if (authDefList == null)
			{
				authDefLists.put(rule.id.value, rule.altDefList);
				prev = rule;
			}
			else
			{
				// remove current rule from the list
				prev.next = rule.next;
				--rules.size;
				// add list of alternative productions from this rule to the authoritative list
				authDefList.last.next = rule.altDefList.first;
				authDefList.last = rule.altDefList.last;
				authDefList.size += rule.altDefList.size;
			}
		}
	}
	
	/**
	 * After nonterminal production rules are collated the quantified symbols are expanded.
	 * 
	 * @param rules
	 */
	public static void expandQuantifiedSymbols(final RuleList rules)
	{
		new AstTreeWalker()
		{
			public void enter(RhsSymbol symbol)
            {
				if (symbol.optQuant != null)
				{
					switch (symbol.optQuant.value.toString().charAt(0))
					{
						case '?':
						{
							Rule rule = getOptionalRule(rules, symbol.id);
							symbol.id = rule.id;
							break;
						}
						case '+':
						{
							Rule rule = getListRule(rules, symbol.id);
							symbol.id = rule.id;
							break;
						}
						case '*':
						{
							Rule rule = getOptionalRule(rules, getListRule(rules, symbol.id).id);
							symbol.id = rule.id;
							break;
						}						
					}
					symbol.optQuant = null;
				}
            }
		}.visit(rules);
	}
	
	private static Rule getOptionalRule(RuleList rules, Term id)
	{
		AltDefList def = defineOptional(id);
		Rule rule = findRule(rules, def);
		if (rule == null)
		{
			rule = new Rule(new Term(BeaverParser.ID, "Opt" + getCamelCasedName(id.value.toString()), 0, 0), def);
			rules.add(rule);
		}
		return rule;
	}
	
	private static AltDefList defineOptional(Term id)
	{
		return new AltDefList(
				new AltDef(
						new Term(BeaverParser.ID, "Opt", 0, 0),
						new RhsItemList()
				)
		).add(
				new AltDef(
						null,
						new RhsItemList(
								new SymbolRhsItem(
										new RhsSymbol(id, null)
								)
						)
				)
		);
	}

	private static Rule getListRule(RuleList rules, Term id)
	{
		String listSymbolName = getCamelCasedName(id.value.toString() + "List"); 
		AltDefList def = defineListOf(id, listSymbolName);
		Rule rule = findRule(rules, def);
		if (rule == null)
		{
			rule = new Rule(new Term(BeaverParser.ID, listSymbolName, 0, 0), def);
			rules.add(rule);
		}
		return rule;
	}
	
	private static AltDefList defineListOf(Term id, String listSymbolName)
	{
		return new AltDefList(
				new AltDef(
						new Term(BeaverParser.ID, "New", 0, 0),
						new RhsItemList(
								new SymbolRhsItem(
										new RhsSymbol(id, null)
								)
						)
				)
		).add(
				new AltDef(
						new Term(BeaverParser.ID, "Ext", 0, 0),
						new RhsItemList(
								new SymbolRhsItem(
										new RhsSymbol(new Term(BeaverParser.ID, listSymbolName, 0, 0), null)
								)
						).add(
								new SymbolRhsItem(
										new RhsSymbol(id, null)
								)
						)
				)
		);
	}
	
	
	/**
	 * Searches for a rule that is defined by the specified productions 
	 * 
	 * @param defList rule definition
	 * @param rules existing rules
	 * @return Rule for some nonterminal or null
	 */
	public static Rule findRule(RuleList rules, AltDefList altDefList)
	{
		for (Rule rule = rules.first; rule != null; rule = rule.next)
		{
			if (isDefinedAs(rule, altDefList))
			{
				return rule;
			}
		}
		return null;
	}
	
	private static boolean isDefinedAs(Rule rule, AltDefList altDefList)
	{
		if (rule.altDefList.size != altDefList.size)
		{
			return false;
		}
		for (AltDef def = altDefList.first; def != null; def = def.next)
		{
			if (!hasDefinition(rule, def.rhsItemList))
			{
				return false;
			}
		}
		return true;
	}
	
	private static boolean hasDefinition(Rule rule, RhsItemList rhsItemList)
	{
		for (AltDef def = rule.altDefList.first; def != null; def = def.next)
		{
			if (def.rhsItemList.equals(rhsItemList))
			{
				return true;
			}
		}
		return false;
	}
	
	private static String getCamelCasedName(String name)
	{
		char[] chars = new char[name.length()];
		chars[0] = Character.toUpperCase(name.charAt(0));
		char privCase = 'U';
		for (int i = 1; i < chars.length; i++)
        {
	        char c = name.charAt(i);
	        if (Character.isLowerCase(c))
	        {
	        	chars[i] = c;
	        	privCase = 'L';
	        }
	        else if (privCase == 'U')
	        {
	        	chars[i] = Character.toLowerCase(c);
	        }
	        else
	        {
	        	chars[i] = c;
	        	privCase = 'U';
	        }
        }
		return new String(chars);
	}
}
