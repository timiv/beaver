package beaver.lexer;

class NFAEdgeCollector implements NFANode.NodeVisitor
{
	CharMap transitions = new CharMap();
	Accept  accept;

	public void visit(NFANode.Fork fork)
	{
		// there are no forks in a kernel
	}

	public void visit(NFANode.Char node)
	{
		for (int i = 0; i < node.charClasses.length; i++)
		{
			CharClass cls = node.charClasses[i];
			
			NFANode.Array charTransitions = (NFANode.Array) transitions.get((char) cls.id);
			if (charTransitions == null)
			{
				transitions.put((char) cls.id, charTransitions = new NFANode.Array());
			}
			charTransitions.add(node.next);
		}
	}

	public void visit(NFANode.Term term)
	{
		if (accept == null || accept.prec < term.accept.prec)
		{
			accept = term.accept;
		}
	}

	void reset()
	{
		transitions.clear();
		accept = null;
	}

}
