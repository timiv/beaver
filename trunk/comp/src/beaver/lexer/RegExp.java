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
		void visit(RegExp.Null op);

		void visit(RegExp.MatchChar op);

		void visit(RegExp.MatchRange op);

		void visit(RegExp.Alt op);

		void visit(RegExp.Cat op);

		void visit(RegExp.Close op);

		void visit(RegExp.Rule op);
	}

	public static RegExp matchText(String text)
    {
    	if (text.isEmpty())
    		return new Null();
    	
    	RegExp re = new RegExp.MatchChar(text.charAt(0));
    	int n = text.length();
    	for (int i = 1; i < n; i++)
    	{
    		re = new RegExp.Cat(re, new RegExp.MatchChar(text.charAt(i)));
    	}
    	return re;
    }
	
	public static RegExp diff(RegExp re1, RegExp re2)
	{
		if (!(re1 instanceof MatchRange && re2 instanceof MatchRange))
			throw new IllegalArgumentException("Range regexp expected");
		CharRange range = new CharRange(((MatchRange) re1).range);
		range.sub(((MatchRange) re2).range);
		return new RegExp.MatchRange(range);
	}
	
	public static RegExp diff(RegExp re, CharRange subRange)
	{
		if (!(re instanceof MatchRange))
			throw new IllegalArgumentException("Range regexp expected");
		CharRange range = new CharRange(((MatchRange) re).range);
		range.sub(subRange);
		return new RegExp.MatchRange(range);
	}
}
