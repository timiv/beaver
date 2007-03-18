/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
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
		this(lb, ub);
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
	
	void add(Range r)
	{
		Range n = this;
		while ( n.next != null )
		{
			n = n.next;
		}
		n.next = r;
	}
	
	void compact()
	{
		Range r = this;
		while ( r != null )
		{
			boolean merged = false;
			for ( Range n = r.next, p = r; n != null; n = n.next )
			{
				if ( r.intersects(n) || r.isContinuation(n) )
				{
					r.union(n);
					merged = true;
					p.next = n.next;
				}
				else
				{
					p = n;
				}
			}
			if ( !merged )
			{
				r = r.next;
			}
		}
	}
	
	static Range copy(Range r)
	{
		Range c = new Range(r.lb, r.ub);
		Range n = c;
		while ( (r = r.next) != null )
		{
			n = n.next = new Range(r.lb, r.ub);
		}
		return c;
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
		if ( str.isEmpty() )
			return null;

		Range r = null;
	read:
		while ( !str.isEmpty() )
		{
			char lb = str.readNextChar();
			if ( str.isEmpty() )
			{
				r = Range.make(lb, r);
				break read;
			}
			char nc = str.readNextChar();
			while ( nc != '-' )
			{
				r = Range.make(lb, r);
				if ( str.isEmpty() )
				{
					r = Range.make(nc, r);
					break read;
				}
				lb = nc;
				nc = str.readNextChar();
			}
			if ( str.isEmpty() )
			{
				r = Range.make(lb, r);
				r = Range.make(nc, r);
				break read;
			}
			char ub = str.readNextChar();
			if ( ub > lb )
				r = Range.make(lb, ub, r);
			else
				r = Range.make(ub, lb, r);
		}
		r.compact();
		return r;
	}
	
	static Range union(Range r1, Range r2)
	{
		if ( r1 == null )
			return r2;
		if ( r2 == null )
			return r1;

		Range r = copy(r1);
		r.add(copy(r2));
		r.compact();
		return r;
	}
	
	static Range minus(Range r1, Range r2)
	{
		Range r = copy(r1);
		
		for (Range a = r; a != null; a = a.next)
		{
			for (Range b = r2; b != null; b = b.next)
			{
				if ( a.intersects(b) )
				{
					a.minus(b);
				}
			}
		}
		return r;
	}
}
