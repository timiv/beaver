/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp.lexer;

import java.util.ArrayList;

import beaver.util.CharMap;

class NFA
{
	Node start;

	NFA(RegExp re, CharSet cs)
	{
		Compiler comp = new Compiler(cs);
		re.accept(comp);
		start = comp.getFirst();
		Node[] nodes = comp.getAllNodes();

		start = optimize(start, nodes);

		for (int i = 0; i < nodes.length; i++)
		{
			nodes[i].marked = false;
		}
	}

	Node optimize(Node first, Node[] allNodes)
	{
		for (Node node = first; node != null && !node.marked; node = node.next)
		{
			node.marked = true;

			if (node instanceof Fork)
			{
				Fork fork = (Fork) node;
				fork.next = optimize(fork.next, allNodes);
				fork.alt = optimize(fork.alt, allNodes);

				if (fork.isRemovable())
				{
					unlink(node, allNodes);

					while (node.next instanceof Fork && ((Fork) node.next).isRemovable())
					{
						unlink(node = node.next, allNodes);
					}
					if (fork == first)
					{
						first = node.next;
					}
				}
				break;
			}
		}
		return first;
	}

	void unlink(Node node, Node[] allNodes)
	{
		for (int i = 0; i < allNodes.length; i++)
		{
			allNodes[i].unlink(node);
		}
	}

	static abstract class Node
	{
		Node    next;
		boolean marked;

		void unlink(Node node)
		{
			if (next == node)
			{
				next = node.next;
			}
		}

		void markCtx()
		{
			// do nothing
		}

		boolean isCtxStart()
		{
			return false;
		}

		abstract void accept(NodeVisitor v);

		/**
         * Sometimes it is impossible to set all node fields at the time of
         * construction. Typically happens with forks pointing forward, or
         * branches merging with a main trunk. When a node is added, which these
         * incomplete nodes have to point to, fixes are run to set missing
         * references.
         */
		static abstract class Fix
		{
			Fix next;

			abstract void apply(Node node);
		}
	}

	static abstract class CtxStartNode extends Node
	{
		boolean isCtxStart;

		void markCtx()
		{
			isCtxStart = true;
		}

		boolean isCtxStart()
		{
			return isCtxStart;
		}
	}

	static class Char extends CtxStartNode
	{
		CharClass[] charClasses;

		Char(ArrayList classes)
		{
			charClasses = (CharClass[]) classes.toArray(new CharClass[classes.size()]);
		}

		Char(CharClass cls)
		{
			if (cls == null)
				throw new IllegalArgumentException("Character class must be provided");
			charClasses = new CharClass[] { cls };
		}

		void accept(NodeVisitor v)
		{
			v.visit(this);
		}
	}

	static class Fork extends CtxStartNode
	{
		Node alt;

		void unlink(Node node)
		{
			super.unlink(node);
			if (alt == node)
			{
				alt = node.next;
			}
		}

		boolean isRemovable()
		{
			return alt == next || alt == this;
		}

		void accept(NodeVisitor v)
		{
			v.visit(this);
		}
	}

	static class Term extends Node
	{
		int    accept;
		String eventName;

		Term(RegExp.RuleOp op)
		{
			this.accept = op.accept;
			this.eventName = op.eventName;
		}

		Term(int accept)
		{
			this.accept = accept;
		}

		void accept(NodeVisitor v)
		{
			v.visit(this);
		}
	}

	static interface NodeVisitor
	{
		void visit(Fork fork);
		void visit(Char node);
		void visit(Term term);
	}

	static class NodeArray
	{
		private Node[] nodes = new Node[16];
		private int    count;

		void add(Node node)
		{
			if (count >= nodes.length)
			{
				// expand the backing store
				Node[] expanded = new Node[nodes.length << 1];
				System.arraycopy(nodes, 0, expanded, 0, nodes.length);
				nodes = expanded;
			}
			nodes[count++] = node;
		}

		boolean matches(Node[] otherNodes)
		{
			if (otherNodes.length != count)
				return false;
			for (int i = 0; i < otherNodes.length; i++)
			{
				if (!otherNodes[i].marked)
					return false;
			}
			return true;
		}

		Node[] getNodes()
		{
			Node[] result = new Node[count];
			System.arraycopy(nodes, 0, result, 0, count);
			return result;
		}

		void reset()
		{
			while (count > 0)
			{
				nodes[--count].marked = false;
			}
		}

		void clear()
		{
			reset();
			count = 0;
		}

		boolean isEmpty()
		{
			return count == 0;
		}

		void forEachNodeAccept(NodeVisitor visitor)
		{
			for (int i = 0; i < count; i++)
			{
				nodes[i].accept(visitor);
			}
		}
	}

	static class Compiler implements RegExp.OpVisitor
	{
		private Node            first;
		private Node            last;
		private Node.Fix        firstFix;
		private Node.Fix        lastFix;

		private final NodeArray allNodes;
		private final ArrayList matchClasses;
		private final CharMap   classes;

		Compiler(CharSet cset)
		{
			this.allNodes = new NodeArray();
			this.matchClasses = new ArrayList(cset.size);
			this.classes = cset.classes;
		}

		Node getFirst()
		{
			if (firstFix != null)
			{
				this.add(new Term(Integer.MAX_VALUE));
			}
			return first;
		}

		Node[] getAllNodes()
		{
			return allNodes.getNodes();
		}

		public void visit(RegExp.NullOp op)
		{
			// nothing to do here
		}

		public void visit(RegExp.MatchCharOp op)
		{
			this.add(new Char((CharClass) classes.get(op.c)));
		}

		public void visit(RegExp.MatchRangeOp op)
		{
			matchClasses.clear();
			for (Range r = op.match; r != null; r = r.next)
			{
				for (char c = r.lb; c < r.ub; c++)
				{
					CharClass cls = (CharClass) classes.get(c);
					if (cls.id == c)
					{
						matchClasses.add(cls);
					}
				}
			}
			this.add(new Char(matchClasses));
		}

		public void visit(RegExp.AltOp op)
		{
			Fork fork = new Fork();
			this.add(fork);
			op.exp1.accept(this);

			Compiler comp = new Compiler(this);
			op.exp2.accept(comp);
			fork.alt = comp.first;

			addFixes(comp);
			addFix(new BranchLastNodeFix(comp.last));
		}

		public void visit(RegExp.CatOp op)
		{
			op.exp1.accept(this);
			op.exp2.accept(this);
		}

		public void visit(RegExp.CloseOp op)
		{
			Node lastNode = last;
			op.exp.accept(this);

			Fork fork = new Fork();
			this.add(fork);
			fork.alt = lastNode != null ? lastNode.next : first; // i.e. the
                                                                    // first
                                                                    // node of
                                                                    // the
                                                                    // expression
                                                                    // it just
                                                                    // has
                                                                    // compiled
		}

		public void visit(RegExp.CloseVOp op)
		{
			if (op.max > op.min)
			{
				Fork[] forwardForks = new Fork[op.max - op.min];
				for (int i = 0; i < forwardForks.length; i++)
				{
					this.add(forwardForks[i] = new Fork());
					op.exp.accept(this);
				}
				addFix(new MultiForwardForkFix(forwardForks));
			}

			if (op.max < 0)
			{
				Fork over = null;
				if (op.min == 0)
				{
					this.add(over = new Fork());
				}
				Node lastNode = last;
				op.exp.accept(this);

				Fork back = new Fork();
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

		public void visit(RegExp.RuleOp op)
		{
			addFix(new RuleOpFirstNodeFix(op));
			op.exp.accept(this);
			addFix(new CtxStartMarkFix());
			op.ctx.accept(this);
			add(new Term(op));
		}

		private Compiler(Compiler comp)
		{
			this.allNodes = comp.allNodes;
			this.matchClasses = comp.matchClasses;
			this.classes = comp.classes;
		}

		private void addFix(Node.Fix fix)
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

		private void addFixes(Compiler comp)
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

		private void add(Node node)
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
					for (Node.Fix fix = firstFix; fix != null; fix = fix.next)
					{
						fix.apply(node);
					}
					lastFix = firstFix = null;
				}
			}
			allNodes.add(node);
		}

		static class BranchLastNodeFix extends NFA.Node.Fix
		{
			private Node node;

			BranchLastNodeFix(Node node)
			{
				this.node = node;
			}

			void apply(Node node)
			{
				this.node.next = node;
			}
		}

		static class MultiForwardForkFix extends NFA.Node.Fix
		{
			private Fork[] forks;

			MultiForwardForkFix(Fork[] forks)
			{
				this.forks = forks;
			}

			void apply(Node node)
			{
				for (int i = 0; i < forks.length; i++)
				{
					forks[i].alt = node;
				}
			}
		}

		static class SingleForwardForkFix extends NFA.Node.Fix
		{
			private Fork fork;

			SingleForwardForkFix(Fork fork)
			{
				this.fork = fork;
			}

			void apply(Node node)
			{
				fork.alt = node;
			}
		}

		static class RuleOpFirstNodeFix extends NFA.Node.Fix
		{
			private RegExp.RuleOp op;

			RuleOpFirstNodeFix(RegExp.RuleOp op)
			{
				this.op = op;
			}

			void apply(Node node)
			{
				op.node = node;
			}
		}

		static class CtxStartMarkFix extends NFA.Node.Fix
		{
			void apply(Node node)
			{
				node.markCtx();
			}
		}
	}
}
