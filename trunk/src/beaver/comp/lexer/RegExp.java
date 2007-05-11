/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp.lexer;

public abstract class RegExp
{
	abstract void accept(OpVisitor v);
	
	public static class NullOp extends RegExp
	{
		void accept(OpVisitor v)
		{
			v.visit(this);
		}
	}
	
	public static class MatchCharOp extends RegExp
	{
		char c;
		
		public MatchCharOp(char c)
		{
			this.c = c;
		}
		
		void accept(OpVisitor v)
		{
			v.visit(this);
		}
	}

	public static class MatchRangeOp extends RegExp
	{
		Range match;

		public MatchRangeOp(Range match)
		{
			this.match = match;
		}
		
		public RegExp minus(MatchRangeOp op)
		{
			return new MatchRangeOp(Range.minus(match, op.match));
		}
		
		void accept(OpVisitor v)
		{
			v.visit(this);
		}
	}
	
	public static class AltOp extends RegExp
	{
		RegExp exp1, exp2;
		
		public AltOp(RegExp exp1, RegExp exp2)
		{
			this.exp1 = exp1;
			this.exp2 = exp2;
		}

		void accept(OpVisitor v)
		{
			v.visit(this);
		}
	}

	public static class CatOp extends RegExp
	{
		RegExp exp1, exp2;
		
		public CatOp(RegExp exp1, RegExp exp2)
		{
			this.exp1 = exp1;
			this.exp2 = exp2;
		}

		void accept(OpVisitor v)
		{
			v.visit(this);
		}
	}

	public static class CloseOp extends RegExp
	{
		RegExp exp;
		
		public CloseOp(RegExp exp)
		{
			this.exp = exp;
		}

		void accept(OpVisitor v)
		{
			v.visit(this);
		}
	}

	public static class CloseVOp extends RegExp
	{
		RegExp exp;
		int    min;
		int    max;
		
		public CloseVOp(RegExp exp, int min, int max)
		{
			this.exp = exp;
			this.min = min;
			this.max = max;
		}

		void accept(OpVisitor v)
		{
			v.visit(this);
		}
	}

	public static class RuleOp extends RegExp
	{
		RegExp   exp;
		RegExp   ctx;
		NFA.Node node;
		int      accept;
		String   eventName;

		public RuleOp(RegExp exp, RegExp ctx)
		{
			this.exp = exp;
			this.ctx = ctx;
		}
		
		public RuleOp(RegExp exp, RegExp ctx, String eventName)
		{
			this(exp, ctx);
			this.eventName = eventName;
		}
		
		public RuleOp(RegExp exp, RegExp ctx, int accept)
		{
			this.exp = exp;
			this.ctx = ctx;
			this.accept = accept;
		}

		public RuleOp(RegExp exp, RegExp ctx, int accept, String eventName)
		{
			this(exp, ctx, accept);
			this.eventName = eventName;
		}
		
		public void setId(int id)
		{
			if ( accept != 0 )
				throw new IllegalStateException("already set");
			accept = id;
		}
		
		public int getId()
		{
			return accept;
		}
		
		void accept(OpVisitor v)
		{
			v.visit(this);
		}
	}
	
	static interface OpVisitor
	{
		void visit(NullOp       op);
		void visit(MatchCharOp  op);
		void visit(MatchRangeOp op);
		void visit(AltOp        op);
		void visit(CatOp        op);
		void visit(CloseOp      op);
		void visit(CloseVOp     op);
		void visit(RuleOp       op);
	}
}
