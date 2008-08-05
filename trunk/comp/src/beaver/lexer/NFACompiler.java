package beaver.lexer;

import java.util.ArrayList;
import java.util.Collection;

class NFACompiler implements RegExp.Visitor
{
	private NFANode            first;
	private NFANode            last;
	private NFANodeFix         firstFix;
	private NFANodeFix         lastFix;

	private NFANode.Array      nodes;
	private CharMap            classes;
	private CharClassCollector charClassCollector;

	NFACompiler(CharSet cset)
	{
		nodes = new NFANode.Array();
		classes = cset.classes;
		charClassCollector = new CharClassCollector(cset);
	}

	NFANode getStart()
	{
		optimize();
		return first;
	}

	public void visit(RegExp.Null op)
	{
		// nothing to do here
	}

	public void visit(RegExp.MatchChar op)
	{
		this.add(new NFANode.Char((CharClass) classes.get(op.c)));
	}

	public void visit(RegExp.MatchRange op)
	{
		charClassCollector.reset();
		op.range.accept(charClassCollector);
		this.add(new NFANode.Char(charClassCollector.getCharClasses()));
	}

	public void visit(RegExp.Alt op)
	{
		NFANode.Fork fork = new NFANode.Fork();
		this.add(fork);
		op.exp1.accept(this);

		NFACompiler comp = new NFACompiler(this);
		op.exp2.accept(comp);
		fork.alt = comp.first;

		addFixes(comp);
		addFix(new BranchLastNodeFix(comp.last));
	}

	public void visit(RegExp.Cat op)
	{
		op.exp1.accept(this);
		op.exp2.accept(this);
	}

	public void visit(RegExp.Close op)
	{
		if (op.max > op.min)
		{
			NFANode.Fork[] forwardForks = new NFANode.Fork[op.max - op.min];
			for (int i = 0; i < forwardForks.length; i++)
			{
				this.add(forwardForks[i] = new NFANode.Fork());
				op.exp.accept(this);
			}
			addFix(new MultiForwardForkFix(forwardForks));
		}

		if (op.max < 0)
		{
			NFANode.Fork over = null;
			if (op.min == 0)
			{
				this.add(over = new NFANode.Fork());
			}
			NFANode lastNode = last;
			op.exp.accept(this);

			NFANode.Fork back = new NFANode.Fork();
			this.add(back);
			back.alt = lastNode != null ? lastNode.next : first;

			if (over != null)
			{
				addFix(new SingleForwardForkFix(over));
			}
		}

		for (int i = 1; i < op.min; i++)
		{
			op.exp.accept(this);
		}
	}

	public void visit(RegExp.Rule op)
	{
		addFix(new RuleFirstNodeFix(op));
		op.exp.accept(this);
		addFix(new CtxStartMarkFix());
		op.ctx.accept(this);
		add(new NFANode.Term(op));
	}

	private NFACompiler(NFACompiler comp)
	{
		this.nodes = comp.nodes;
		this.classes = comp.classes;
		this.charClassCollector = comp.charClassCollector;
	}

	private void addFix(NFANodeFix fix)
	{
		if (lastFix == null)
		{
			lastFix = firstFix = fix;
		}
		else
		{
			lastFix = lastFix.next = fix;
		}
	}

	private void addFixes(NFACompiler comp)
	{
		if (comp.firstFix != null)
		{
			if (firstFix == null)
			{
				firstFix = comp.firstFix;
				lastFix = comp.lastFix;
			}
			else
			{
				lastFix.next = comp.firstFix;
				lastFix = comp.lastFix;
			}
		}
	}

	private void add(NFANode node)
	{
		if (last == null)
		{
			last = first = node;
		}
		else
		{
			last = last.next = node;

			if (firstFix != null)
			{
				for (NFANodeFix fix = firstFix; fix != null; fix = fix.next)
				{
					fix.applyTo(node);
				}
				lastFix = firstFix = null;
			}
		}
		nodes.add(node);
	}
	
	private void optimize()
	{
		first = optimize(first);
		nodes.reset();
	}
	
	private NFANode optimize(NFANode start)
	{
		for (NFANode node = start; node != null && !node.marked; node = node.next)
		{
			node.marked = true;

			if (node instanceof NFANode.Fork)
			{
				NFANode.Fork fork = (NFANode.Fork) node;
				if (fork.next == null)
				{
					fork.next = fork.alt;
					fork.alt = null;
				}
				fork.next = optimize(fork.next);
				fork.alt = optimize(fork.alt);

				if (fork.isRemovable())
				{
					nodes.unlink(node);

					while (node.next instanceof NFANode.Fork && ((NFANode.Fork) node.next).isRemovable())
					{
						nodes.unlink(node = node.next);
					}
					if (fork == start)
					{
						start = node.next;
					}
				}
				break;
			}
			else if (node instanceof NFANode.Term)
			{
				node.next = null;
			}
		}
		return start;
	}

	static class CharClassCollector implements CharVisitor
	{
		private CharMap    classes;
		private Collection matches;

		CharClassCollector(CharSet cset)
		{
			classes = cset.classes;
			matches = new ArrayList(cset.size);
		}

		void reset()
		{
			matches.clear();
		}

		CharClass[] getCharClasses()
		{
			return (CharClass[]) matches.toArray(new CharClass[matches.size()]);
		}

		public void visit(char c)
		{
			CharClass cls = (CharClass) classes.get(c);
			if (cls.id == c)
			{
				matches.add(cls);
			}
		}
	}

	/**
	 * Sometimes it is impossible to set all node fields at the time of construction. Typically happens with forks
	 * pointing forward, or branches merging with a main trunk. When a node is added, which these incomplete nodes have
	 * to point to, fixes are run to set missing references.
	 */
	static abstract class NFANodeFix
	{
		NFANodeFix next;

		abstract void applyTo(NFANode node);
	}

	static class BranchLastNodeFix extends NFANodeFix
	{
		private NFANode node;

		BranchLastNodeFix(NFANode node)
		{
			this.node = node;
		}

		void applyTo(NFANode node)
		{
			this.node.next = node;
		}
	}

	static class MultiForwardForkFix extends NFANodeFix
	{
		private NFANode.Fork[] forks;

		MultiForwardForkFix(NFANode.Fork[] forks)
		{
			this.forks = forks;
		}

		void applyTo(NFANode node)
		{
			for (int i = 0; i < forks.length; i++)
			{
				forks[i].alt = node;
			}
		}
	}

	static class SingleForwardForkFix extends NFANodeFix
	{
		private NFANode.Fork fork;

		SingleForwardForkFix(NFANode.Fork fork)
		{
			this.fork = fork;
		}

		void applyTo(NFANode node)
		{
			fork.alt = node;
		}
	}

	static class RuleFirstNodeFix extends NFANodeFix
	{
		private RegExp.Rule op;

		RuleFirstNodeFix(RegExp.Rule op)
		{
			this.op = op;
		}

		void applyTo(NFANode node)
		{
			op.node = node;
		}
	}

	static class CtxStartMarkFix extends NFANodeFix
	{
		void applyTo(NFANode node)
		{
			node.markCtx();
		}
	}
}
