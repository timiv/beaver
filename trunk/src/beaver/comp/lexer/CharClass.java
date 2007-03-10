/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp.lexer;

import beaver.util.CharMap;

class CharClass
{
	/**
	 * Class uses code of one of its characters (a "representative") as its ID
	 */ 
	char id;
	
	int cardinality;
	Span span;

	CharClass next;
	CharClass next_fin_split;
	
	CharClass()
	{
		this.id = NO_ID;
	}
	
	static final char NO_ID = '\uffff';
	
	static void setSpans(CharMap classes, char min, char max)
	{
		CharClass act = null;
		for (char c = min; c <= max; c++)
		{
			CharClass cls = (CharClass) classes.get(c);
			if (cls == null)
			{
				if (act != null)
				{
					act.span.ub = c;
					act = null;
				}
			} 
			else if (cls != act)
			{
				if (act != null)
				{
					act.span.ub = c;
				}
				act = cls;
				cls.span = new Span(c, cls.span);
			}
		}
		if (act != null)
		{
			act.span.ub = ++max;
		}
	}
	
	static class Span
	{
		char lb, ub;
		Span next;
		
		Span(char c, Span next)
		{
			this.lb = c;
			this.next = next;
		}
		
		Span(Span span, Span next)
		{
			this.lb = span.lb;
			this.ub = span.ub;
			this.next = next;
		}
		
		int length()
		{
			return ub - lb;
		}
		
		boolean equals(Span s)
		{
			return lb == s.lb && ub == s.ub;
		}
		
		boolean intersects(Span s)
		{
			return lb <= s.lb && s.lb < ub
				|| s.lb <= lb && lb < s.ub;
		}
		
		boolean joins(Span s)
		{
			return lb == s.ub || ub == s.lb;
		}
		
		void union(Span s)
		{
			if (lb > s.lb)
				lb = s.lb;
			if (s.ub > ub)
				ub = s.ub;
		}
		
		static Span join(Span s, Span next)
		{
			Span span = next;
			while (s != null)
			{
				span = new Span(s, span);
				s = s.next;
			}
			return span;
		}
		
		static void merge(Span s)
		{
			for (; s != null; s = s.next)
			{
				for (Span n = s.next, p = s; n != null; n = n.next)
				{
					if (s.intersects(n) || s.joins(n))
					{
						s.union(n);
						p.next = n.next;
					}
					else
					{
						p = n;
					}
				}
			}
		}
	}
	
	/**
	 * Counts classes and finds the range of characters. 
	 */
	static class Counter implements CharMap.EntryVisitor
	{
		int n = 0;
		char min = '\uffff';
		char max = '\0';
	
		public void visit(char c, Object v)
		{
			CharClass cls = (CharClass) v;
			if (cls.id == NO_ID)
			{
				cls.id = c;
				n++;
			}
			if (c < min) min = c;
			if (c > max) max = c;
		}
	}

	/**
	 * Splits all recognizable characters into character classes.
	 */
	static class Builder implements RegExp.OpVisitor
	{
		CharMap classes = new CharMap();
		
		public void visit(RegExp.NullOp op)
		{
			// nothing to do here
		}
	
		public void visit(RegExp.MatchCharOp op)
		{
			char c = op.c;
			
			CharClass char_class = (CharClass) classes.get(c);
			if (char_class != null)
			{
				if (char_class.cardinality == 1)
					return;
	
				// "remove" character from the original class
				char_class.cardinality--;
			}
			// and "put" it into the new class
			char_class = new CharClass();
			char_class.cardinality++;
			classes.put(c, char_class);
		}
		
		public void visit(RegExp.MatchRangeOp op)
		{
			CharClass new_class = null;
			CharClass fin_class = null;
	
			for (Range r = op.match; r != null; r = r.next)
			{
				for (char c = r.lb; c < r.ub; c++)
				{
					CharClass char_class = (CharClass) classes.get(c);
					if (char_class == null) // not classified yet
					{
						if (new_class == null)
							new_class = new CharClass();
						new_class.cardinality++;
						classes.put(c, new_class);
					}
					else
					{
						CharClass next_class = char_class.next;
						if (next_class == null) // have not started splitting "char_class" yet
						{
							if (char_class.cardinality == 1) // no other characters in this class
								continue;
	
							char_class.next = next_class = new CharClass();
							char_class.next_fin_split = fin_class;
							fin_class = char_class;
						}
						// "remove" character from the original class
						char_class.cardinality--;
						// and "put" it into the next class
						next_class.cardinality++;
						classes.put(c, next_class);
					}
				}
			}
	
			for (; fin_class != null; fin_class = fin_class.next_fin_split)
			{
				fin_class.next = null;
			}
		}
	
		public void visit(RegExp.AltOp op)
		{
			op.exp1.accept(this);
			op.exp2.accept(this);
		}
	
		public void visit(RegExp.CatOp op)
		{
			op.exp1.accept(this);
			op.exp2.accept(this);
		}
	
		public void visit(RegExp.CloseOp op)
		{
			op.exp.accept(this);
		}
		
		public void visit(RegExp.CloseVOp op)
		{
			op.exp.accept(this);
		}
		
		public void visit(RegExp.RuleOp op)
		{
			op.exp.accept(this);
			op.ctx.accept(this);
		}
	} 
}