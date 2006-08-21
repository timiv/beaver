/**
 * 
 */
package beaver.comp.spec;

/**
 * @author Alexander Demenchuk
 *
 */
public class InlineRulesExtractor extends TreeWalker
{
	private RuleList rules;
	private int lastRuleId;
	
	public void visit(Spec node)
	{
		rules = node.rules;
		super.visit(node);
	}
	
	public void visit(ItemInline node)
	{
		super.visit(node);
		/*
		 * the boostrap grammar does not support inline rule names
		 * we work around this by generating some synthetic names
		 */
		String nt = "SynRule" + ++lastRuleId;
		Term name = new Term(nt);
		Alt ntDef = new Alt(node.def);
		ntDef.copyLocation(node.def);
		
		rules.add(new Rule(name, new AltList(ntDef)));

		Item rhsSym = new ItemSymbol(node.ref, new Term(nt), node.close);
		rhsSym.copyLocation(node);
		
		node.replaceWith(rhsSym);
	}
}
