package beaver.lexer;

public abstract class RegExp
{
	abstract void accept(Visitor v);

	public static class Null extends RegExp
	{
		void accept(Visitor v)
		{
			v.visit(this);
		}
	}

	public static class MatchChar extends RegExp
	{
		char c;

		public MatchChar(char c)
		{
			this.c = c;
		}

		void accept(Visitor v)
		{
			v.visit(this);
		}
	}

	public static class MatchRange extends RegExp
	{
		CharRange range;

		public MatchRange(CharRange range)
		{
			this.range = range;
		}

		void accept(Visitor v)
		{
			v.visit(this);
		}
	}

	public static class Alt extends RegExp
	{
		RegExp exp1;
		RegExp exp2;

		public Alt(RegExp exp1, RegExp exp2)
		{
			this.exp1 = exp1;
			this.exp2 = exp2;
		}

		void accept(Visitor v)
		{
			v.visit(this);
		}
	}

	public static class Cat extends RegExp
	{
		RegExp exp1;
		RegExp exp2;

		public Cat(RegExp exp1, RegExp exp2)
		{
			this.exp1 = exp1;
			this.exp2 = exp2;
		}

		void accept(Visitor v)
		{
			v.visit(this);
		}
	}

	public static class Close extends RegExp
	{
		RegExp exp;
		int    min;
		int    max;

		public Close(RegExp exp, int min, int max)
		{
			this.exp = exp;
			this.min = min;
			this.max = max;
		}

		public Close(RegExp exp, char type)
		{
			this.exp = exp;
			switch (type)
			{
				case '?':
				{
					this.min = 0;
					this.max = 1;
					break;
				}
				case '+':
				{
					this.min = 1;
					this.max = -1;
					break;
				}
				case '*':
				{
					this.min = 0;
					this.max = -1;
					break;
				}
				default:
				{
					throw new IllegalArgumentException("type");
				}
			}
		}

		void accept(Visitor v)
		{
			v.visit(this);
		}
	}

	public static class Rule extends RegExp
	{
		RegExp  exp;
		RegExp  ctx;
		Accept  acc;
		NFANode node;

		public Rule(RegExp exp, RegExp ctx, Accept accept)
		{
			this.exp = exp;
			this.ctx = ctx;
			this.acc = accept;
		}

		void accept(Visitor v)
		{
			v.visit(this);
		}
	}

	static interface Visitor
	{
		void visit(Null op);

		void visit(MatchChar op);

		void visit(MatchRange op);

		void visit(Alt op);

		void visit(Cat op);

		void visit(Close op);

		void visit(Rule op);
	}

}
