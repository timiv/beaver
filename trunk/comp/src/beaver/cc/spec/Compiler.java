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
import beaver.cc.Log;

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
	
	public static boolean checkInlineRulesCorrectness(final RuleList rules, final Log log)
	{
		final boolean[] res = new boolean[1];
		res[0] = true; // assume everything is fine
		new AstTreeWalker()
		{
			public void visit(InlineRhsItem item)
			{
				String errMsg = null;
				int rc = (item.prefix == null ? 0 : 1) + (item.suffix == null ? 0 : 1);
				if (rc == 0)
				{
					errMsg = (item.quant.value.toString().charAt(0) != '?' ? "prefix and/or suffix" : "item separator") + " must be present";
				}
				else if (rc == 2 && item.quant.value.toString().charAt(0) != '?')
				{
					errMsg = "two list separators";
				}
				if (errMsg != null)
				{
					FirstTermFinder firstTermFinder = new FirstTermFinder();
					firstTermFinder.visit(item);
					Term term = firstTermFinder.term;
					log.error("@" + term.line + "," + term.column + " - " + errMsg);
					res[0] = false;
				}
			}
		}.visit(rules);
		return res[0];
	}
	
	static class InlineListDef
	{
		Term symbol;
		Term separator;
		
		InlineListDef(InlineRhsItem item)
		{
			symbol = item.rhsSymbol.id;
			separator = (item.prefix != null ? item.prefix : item.suffix);
		}
		
		public boolean equals(Object o)
		{
			return o instanceof InlineListDef
				&& ((InlineListDef) o).symbol.value.equals(symbol.value)
				&& ((InlineListDef) o).separator.value.equals(separator.value)
			;
		}
		
		public int hashCode()
		{
			return symbol.value.hashCode() ^ separator.value.hashCode();
		}
	}
	
	public static void extractInlinedSymbols(final RuleList rules)
	{
		new AstTreeWalker()
		{
			private RhsItemList rhsItemList;
			private RhsItem     prevRhsItem;
			private Map         extractedLists = new HashMap();
			
			public void enter(RhsItemList rhsItemList)
            {
	            this.rhsItemList = rhsItemList;
	            this.prevRhsItem = null;
            }

			public void leave(KeywordRhsItem keywordRhsItem)
            {
				prevRhsItem = keywordRhsItem;
            }

			public void leave(SymbolRhsItem symbolRhsItem)
            {
				prevRhsItem = symbolRhsItem;
            }

			public void visit(InlineRhsItem item)
            {
				Term ruleId;
				switch (item.quant.value.toString().charAt(0))
				{
					case '?':
					{
						ruleId = getOptionalRule(rules, item.ref, item.prefix, item.rhsSymbol.id, item.suffix).id;
						break;
					}
					case '+':
					{
						ruleId = getListSymbol(item);
						break;
					}
					case '*':
					{
						ruleId = getOptionalRule(rules, getListSymbol(item)).id;
						break;
					}
					default:
					{
						throw new IllegalStateException("quantifier");
					}
				}
				substitute(item, new SymbolRhsItem(item.ref, new RhsSymbol(ruleId, null)));
            }
			
			private Term getListSymbol(InlineRhsItem item)
			{
				InlineListDef def = new InlineListDef(item);
				Term listSymbolId = (Term) extractedLists.get(def);
				if (listSymbolId == null)
				{
					extractedLists.put(def, listSymbolId = getListRule(rules, item.ref, def.symbol, def.separator).id);
				}
				return listSymbolId;
			}
			
			private void substitute(InlineRhsItem inlineRhsItem, SymbolRhsItem symbolRhsItem)
			{
				symbolRhsItem.next = inlineRhsItem.next;
				
				if (prevRhsItem != null)
				{
					prevRhsItem.next = symbolRhsItem; 
				}
				else
				{
					rhsItemList.first = symbolRhsItem;
				}
				
				if (rhsItemList.last == inlineRhsItem)
				{
					rhsItemList.last = symbolRhsItem;
				}
				
				prevRhsItem = symbolRhsItem;
			}
		}.visit(rules);
	}
	
	private static Rule getOptionalRule(RuleList rules, Term ref, Term prefix, Term id, Term suffix)
	{
		AltDefList def = defineOptional(prefix, id, suffix);
		Rule rule = findRule(rules, def);
		if (rule == null)
		{
			rule = new Rule(new Term(BeaverParser.ID, getOptRuleName(rules, ref, id), 0, 0), def);
			rules.add(rule);
		}
		return rule;
	}
	
	private static String getOptRuleName(RuleList rules, Term ref, Term id)
	{
		String name = "Opt" + (ref != null ? getTitleCasedName(ref.value.toString()) : getTitleCasedName(id.value.toString()));
		return getUniqueRuleName(rules, name);
	}

	private static String getUniqueRuleName(RuleList rules, String name)
    {
	    if (hasRuleNamed(rules, name))
		{
			int suffix = 1;
			while (hasRuleNamed(rules, name + suffix))
			{
				++suffix;
			}
			name += suffix;
		}
		return name;
    }
	
	private static boolean hasRuleNamed(RuleList rules, String name)
	{
		for (Rule rule = rules.first; rule != null; rule = rule.next)
        {
	        if (name.equals(rule.id.value))
	        {
	        	return true;
	        }
        }
		return false;
	}
	
	private static AltDefList defineOptional(Term prefix, Term id, Term suffix)
	{	
		RhsItemList rhsItemList; 
		if (prefix != null)
		{
			rhsItemList = new RhsItemList(
					new KeywordRhsItem(
							prefix
					)
			).add(
					new SymbolRhsItem(
							new RhsSymbol(id, null)
					)
			);
		}
		else
		{
			rhsItemList = new RhsItemList(
					new SymbolRhsItem(
							new RhsSymbol(id, null)
					)
			);
		}
		if (suffix != null)
		{
			rhsItemList.add(
					new KeywordRhsItem(
							suffix
					)
			);
		}
		return new AltDefList(
				new AltDef(
						new Term(BeaverParser.ID, "Opt", 0, 0),
						new RhsItemList()
				)
		).add(
				new AltDef(
						null,
						rhsItemList
				)
		);
	}

	private static Rule getListRule(RuleList rules, Term ref, Term id, Term separator)
	{
		String listSymbolName = getListRuleName(rules, ref, id);
		AltDefList def = defineListOf(id, separator, listSymbolName);
		Rule rule = findRule(rules, def);
		if (rule == null)
		{
			rule = new Rule(new Term(BeaverParser.ID, listSymbolName, 0, 0), def);
			rules.add(rule);
		}
		return rule;
	}
	
	private static String getListRuleName(RuleList rules, Term ref, Term id)
	{
		String name = ref != null ? getTitleCasedName(ref.value.toString()) : getTitleCasedName(id.value.toString()) + "List";
		return getUniqueRuleName(rules, name);
	}
	
	private static AltDefList defineListOf(Term id, Term sep, String listSymbolName)
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
										new RhsSymbol(sep, null)
								)
						).add(
								new SymbolRhsItem(
										new RhsSymbol(id, null)
								)
						)
				)
		);
	}

	private static Rule getOptionalRule(RuleList rules, Term id)
	{
		AltDefList def = defineOptional(id);
		Rule rule = findRule(rules, def);
		if (rule == null)
		{
			rule = new Rule(new Term(BeaverParser.ID, "Opt" + getTitleCasedName(id.value.toString()), 0, 0), def);
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
		String listSymbolName = getTitleCasedName(id.value.toString() + "List"); 
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
	
	private static String getTitleCasedName(String name)
	{
		char[] chars = name.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		if (isUpperCase(chars))
		{
			for (int i = 1; i < chars.length; i++)
			{
				chars[i] = Character.toLowerCase(chars[i]);
			}
		}
		return new String(chars);
	}
	
	private static boolean isUpperCase(char[] chars)
	{
		for (int i = 1; i < chars.length; i++)
        {
			if (!Character.isUpperCase(chars[i]))
			{
				return false;
			}
        }
		return true;
	}
	
	static class FirstTermFinder extends AstTreeWalker
	{
		Term term;

		public void visit(AltDef altDef)
        {
	        if (term == null)
	        {
	        	super.visit(altDef);
	        }
        }

		public void visit(AltDefList altDefList)
        {
	        if (term == null)
	        {
	        	super.visit(altDefList);
	        }
        }

		public void visit(AltRegExprList altRegExprList)
        {
	        if (term == null)
	        {
	        	super.visit(altRegExprList);
	        }
        }

		public void visit(CatRegExpr catRegExpr)
        {
	        if (term == null)
	        {
	        	super.visit(catRegExpr);
	        }
        }

		public void visit(CatRegExprList catRegExprList)
        {
	        if (term == null)
	        {
	        	super.visit(catRegExprList);
	        }
        }

		public void visit(DiffRangeExpr diffRangeExpr)
        {
	        if (term == null)
	        {
	        	super.visit(diffRangeExpr);
	        }
        }

		public void visit(InlineRhsItem inlineRhsItem)
        {
	        if (term == null)
	        {
	        	super.visit(inlineRhsItem);
	        }
        }

		public void visit(KeywordRhsItem keywordRhsItem)
        {
	        if (term == null)
	        {
	        	super.visit(keywordRhsItem);
	        }
        }

		public void visit(Macro macro)
        {
	        if (term == null)
	        {
	        	super.visit(macro);
	        }
        }

		public void visit(MacroList macroList)
        {
	        if (term == null)
	        {
	        	super.visit(macroList);
	        }
        }

		public void visit(MacroRangeExpr macroRangeExpr)
        {
	        if (term == null)
	        {
	        	super.visit(macroRangeExpr);
	        }
        }

		public void visit(MultCharExprQuantifier multCharExprQuantifier)
        {
	        if (term == null)
	        {
	        	super.visit(multCharExprQuantifier);
	        }
        }

		public void visit(NestedCharExpr nestedCharExpr)
        {
	        if (term == null)
	        {
	        	super.visit(nestedCharExpr);
	        }
        }

		public void visit(OperCharExprQuantifier operCharExprQuantifier)
        {
	        if (term == null)
	        {
	        	super.visit(operCharExprQuantifier);
	        }
        }

		public void visit(Precedence precedence)
        {
	        if (term == null)
	        {
	        	super.visit(precedence);
	        }
        }

		public void visit(PrecedenceList precedenceList)
        {
	        if (term == null)
	        {
	        	super.visit(precedenceList);
	        }
        }

		public void visit(PrecSymbolList precSymbolList)
        {
	        if (term == null)
	        {
	        	super.visit(precSymbolList);
	        }
        }

		public void visit(RangeCharExpr rangeCharExpr)
        {
	        if (term == null)
	        {
	        	super.visit(rangeCharExpr);
	        }
        }

		public void visit(RhsItemList rhsItemList)
        {
	        if (term == null)
	        {
	        	super.visit(rhsItemList);
	        }
        }

		public void visit(RhsSymbol rhsSymbol)
        {
	        if (term == null)
	        {
	        	super.visit(rhsSymbol);
	        }
        }

		public void visit(Rule rule)
        {
	        if (term == null)
	        {
	        	super.visit(rule);
	        }
        }

		public void visit(RuleList ruleList)
        {
	        if (term == null)
	        {
	        	super.visit(ruleList);
	        }
        }

		public void visit(RuleNamePrecSymbol ruleNamePrecSymbol)
        {
	        if (term == null)
	        {
	        	super.visit(ruleNamePrecSymbol);
	        }
        }

		public void visit(SimpleRangeExpr simpleRangeExpr)
        {
	        if (term == null)
	        {
	        	super.visit(simpleRangeExpr);
	        }
        }

		public void visit(Spec spec)
        {
	        if (term == null)
	        {
	        	super.visit(spec);
	        }
        }

		public void visit(SymbolRhsItem symbolRhsItem)
        {
	        if (term == null)
	        {
	        	super.visit(symbolRhsItem);
	        }
        }

		public void visit(Term term)
        {
			this.term = term;
        }

		public void visit(TermTextPrecSymbol termTextPrecSymbol)
        {
	        if (term == null)
	        {
	        	super.visit(termTextPrecSymbol);
	        }
        }

		public void visit(TextCharExpr textCharExpr)
        {
	        if (term == null)
	        {
	        	super.visit(textCharExpr);
	        }
        }

		public void visit(Token token)
        {
	        if (term == null)
	        {
	        	super.visit(token);
	        }
        }

		public void visit(TokenList tokenList)
        {
	        if (term == null)
	        {
	        	super.visit(tokenList);
	        }
        }
	};
}
