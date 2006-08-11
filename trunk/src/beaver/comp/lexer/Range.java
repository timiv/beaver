package beaver.comp.lexer;

import beaver.util.SubStr;

class Range
{
	char  lb, ub; // [lb,ub)
	Range next;
	
	Range(char lb, char ub)
	{
		this.lb = lb;
		this.ub = ub;
	}
	
	Range(char lb, char ub, Range next)
	{
		this.lb = lb;
		this.ub = ub;
		this.next = next;
	}
	
	boolean intersects(Range r)
	{
		return r.lb < this.ub && this.lb < r.ub;
	}
	
	boolean isContinuation(Range r)
	{
		return r.lb == this.ub || this.lb == r.ub;
	}
	
	boolean isSubrangeOf(Range r)
	{
		return r.lb <= this.lb && this.ub <= r.ub;
	}
	
	void union(Range r)
	{
		if (this.lb >= r.lb)
			this.lb = r.lb;
		if (this.ub <= r.ub)
			this.ub = r.ub;
	}
	
	void minus(Range r)
	{
		if (this.lb < r.lb)
		{
			if (r.ub < this.ub)
			{
				this.next = new Range(r.ub, this.ub, this.next);
			}
			this.ub = r.lb;
		}
		else
		{
			this.lb = r.ub;
		}
	}
	
	static Range make(char first, char last)
	{
		return new Range(first, (char) (last + 1));
	}

	static Range make(char first, char last, Range next)
	{
		return new Range(first, (char) (last + 1), next);
	}
	
	static Range make(char c)
	{
		return new Range(c, (char) (c + 1));
	}
	
	static Range make(char c, Range next)
	{
		return new Range(c, (char) (c + 1), next);
	}
	
	static Range compile(SubStr str)
	{
		if (str.isEmpty())
			return null;
		
		Range r = null;
		while (!str.isEmpty())
		{
			char lb = str.readNextChar();
			if (str.isEmpty())
			{
				return union(r, Range.make(lb));
			}
			char nc = str.readNextChar();		
			while (nc != '-')
			{
				r = union(r, Range.make(lb));
				if (str.isEmpty())
				{
					return union(r, Range.make(nc));
				}
				lb = nc;
				nc = str.readNextChar();
			}
			if (str.isEmpty())
			{
				return union(r, Range.make(lb, Range.make(nc)));
			}
			char ub = str.readNextChar();
			if (ub > lb)
				r = union(r, Range.make(lb, ub));
			else
				r = union(r, Range.make(ub, lb));
		}
		return r;
	}
	
	static Range union(Range a, Range b)
	{
		if (a == null)
			return b;
		
		Range r = a;
		while (r.next != null)
		{
			r = r.next;
		}
		r.next = b;
		
		boolean mutating = true;
		while (mutating)
		{
			mutating = false;
			
			for (r = a; r != null; r = r.next)
			{
				for (Range p = r, n = r.next; n != null; n = n.next)
				{
					if (n.intersects(r) || n.isContinuation(r))
					{
						p.next = n.next;
						r.union(n);
						mutating = true;
					}
					else
					{
						p = n;
					}
				}
			}
		}		
		return a;
	}
	
	static Range minus(Range a, Range b)
	{
		for (Range x = b; x != null; x = x.next)
		{
			for (Range r = a, p = null; r != null; r = r.next)
			{
				if (r.isSubrangeOf(x))
				{
					if (r == a)
					{
						a = r.next;
					}
					else
					{
						p.next = r.next;
					}
				}
				else if (r.intersects(x))
				{
					r.minus(x);
					p = r;
				}
				else
				{
					p = r;
				}
			}
		}
		return a;
	}
}
