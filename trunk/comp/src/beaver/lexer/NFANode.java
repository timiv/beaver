package beaver.lexer;

import java.util.Arrays;


abstract class NFANode
{
	NFANode next;
	boolean marked;

	void unlink(NFANode node)
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
	
	static abstract class CtxStart extends NFANode
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

	static class Char extends CtxStart
	{
		CharClass[] charClasses;

		Char(CharClass[] classes)
		{
			charClasses = classes;
		}
		
		Char(CharClass cls)
		{
			if (cls == null)
				throw new IllegalArgumentException("null");
			charClasses = new CharClass[] { cls };
		}

		void accept(NodeVisitor v)
		{
			v.visit(this);
		}
		
		public boolean equals(Object obj)
		{
			return obj instanceof Char && Arrays.equals(charClasses, ((Char) obj).charClasses);
		}
	}

	static class Fork extends CtxStart
	{
		NFANode alt;

		void unlink(NFANode node)
		{
			super.unlink(node);
			if (alt == node)
			{
				alt = node.next;
			}
		}

		boolean isRemovable()
		{
			return alt == next || alt == this || alt == null;
		}

		void accept(NodeVisitor v)
		{
			v.visit(this);
		}
	}

	static class Term extends NFANode
	{
		Accept accept;

		Term(RegExp.Rule op)
		{
			this.accept = op.acc;
		}

		void accept(NodeVisitor v)
		{
			v.visit(this);
		}
		
		public boolean equals(Object obj)
		{
			return obj instanceof Term && accept == ((Term) obj).accept;
		}
	}

	static interface NodeVisitor
	{
		void visit(Fork fork);
		void visit(Char node);
		void visit(Term term);
	}

	static class Array
	{
		private NFANode[] nodes = new NFANode[16];
		private int       count;

		void add(NFANode node)
		{
			if (count >= nodes.length)
			{
				// expand the backing store
				NFANode[] expanded = new NFANode[nodes.length << 1];
				System.arraycopy(nodes, 0, expanded, 0, nodes.length);
				nodes = expanded;
			}
			nodes[count++] = node;
		}
		
		boolean matches(NFANode[] otherNodes)
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

		void unlink(NFANode node)
		{
			for (int i = 0; i < count; i++)
            {
	            nodes[i].unlink(node);
            }
		}

		void reset()
		{
			for (int i = 0; i < count; i++)
			{
				nodes[i].marked = false;
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
		
		NFANode[] toArray()
		{
			NFANode[] array = new NFANode[count];
			System.arraycopy(nodes, 0, array, 0, count);
			return array;
		}

		void accept(NodeVisitor visitor)
		{
			for (int i = 0; i < count; i++)
			{
				nodes[i].accept(visitor);
			}
		}
	}
}
