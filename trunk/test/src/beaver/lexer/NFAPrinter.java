package beaver.lexer;

import beaver.lexer.NFANode.Char;
import beaver.lexer.NFANode.Fork;
import beaver.lexer.NFANode.NodeVisitor;
import beaver.lexer.NFANode.Term;

class NFAPrinter implements NodeVisitor
{
	int indent = 0;
	
	private void printIndent()
	{
		for (int i = 0; i < indent; i++)
		{
			System.out.print("    ");
		}
	}
	
	@Override
	public void visit(Fork fork)
	{
		if (!fork.marked)
		{
			fork.marked = true;
			printIndent();
			System.out.println("Fork @ " + System.identityHashCode(fork));
			printIndent();
			System.out.println("goto " + System.identityHashCode(fork.next) + " {");
			indent++;
			fork.next.accept(this);
			indent--;
			printIndent();
			System.out.println("}");
			printIndent();
			System.out.println("else " + System.identityHashCode(fork.alt) + " {");
			indent++;
			fork.alt.accept(this);
			indent--;
			printIndent();
			System.out.println("}");
			fork.marked = false;
		}
	}

	@Override
	public void visit(Char node)
	{
		if (!node.marked)
		{
			node.marked = true;
			printIndent();
			System.out.print("Char @ " + System.identityHashCode(node) + ": ");
			System.out.print("[" + node.charClasses[0].range.size() + "]=");
			System.out.print(node.charClasses[0].range);
			for (int i = 1; i < node.charClasses.length; i++)
			{
				System.out.print(", [" + node.charClasses[i].range.size() + "]=");
				System.out.print(node.charClasses[i].range);
			}
			System.out.println();
			node.next.accept(this);
			node.marked = false;
		}
	}

	@Override
	public void visit(Term term)
	{
		if (!term.marked)
		{
			term.marked = true;
			printIndent();
			System.out.println("Term @ " + System.identityHashCode(term) + ": " + term.accept);
			if (term.next != null)
			{
				term.next.accept(this);
			}
			term.marked = false;
		}
	}

}
