package beaver.lexer;

class DFAClosure implements NFANode.NodeVisitor
{
	private NFANode.Array kernel = new NFANode.Array();
	private boolean       foundCtxNode;

	public void visit(NFANode.Char node)
	{
		if (!node.marked)
		{
			if (node.isCtxStart())
			{
				foundCtxNode = true;
			}
			node.marked = true;
			kernel.add(node);
		}
	}

	public void visit(NFANode.Fork node)
	{
		if (node.isCtxStart())
		{
			foundCtxNode = true;
		}
		node.next.accept(this);
		node.alt.accept(this);
	}

	public void visit(NFANode.Term node)
	{
		if (!node.marked)
		{
			node.marked = true;
			kernel.add(node);
		}
	}
	
	boolean kernelMatches(NFANode[] stateKernel)
	{
		return kernel.matches(stateKernel);
	}
	
	void reset()
	{
		kernel.clear();
		foundCtxNode = false;
	}

	NFANode[] getKernel()
	{
		return kernel.toArray();
	}

	boolean isPreCtx()
	{
		return foundCtxNode;
	}
	
}
