/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp.lexer;

abstract class RegExp
{
	abstract void accept(OpVisitor v);
	
	static class NullOp extends RegExp
	{
		void accept(OpVisitor v)
		{
			v.visit(this);
		}
	}
	
	static class MatchCharOp extends RegExp
	{
		char c;
		
		MatchCharOp(char c)
		{
			this.c = c;
		}
		
		void accept(OpVisitor v)
		{
			v.visit(this);
		}
	}

	static class MatchRangeOp extends RegExp
	{
		Range match;

		MatchRangeOp(Range match)
		{
			this.match = match;
		}
		
		void accept(OpVisitor v)
		{
			v.visit(this);
		}
	}
	
	static class AltOp extends RegExp
	{
		RegExp exp1, exp2;
		
		AltOp(RegExp exp1, RegExp exp2)
		{
			this.exp1 = exp1;
			this.exp2 = exp2;
		}

		void accept(OpVisitor v)
		{
			v.visit(this);
		}
	}

	static class CatOp extends RegExp
	{
		RegExp exp1, exp2;
		
		CatOp(RegExp exp1, RegExp exp2)
		{
			this.exp1 = exp1;
			this.exp2 = exp2;
		}

		void accept(OpVisitor v)
		{
			v.visit(this);
		}
	}

	static class CloseOp extends RegExp
	{
		RegExp exp;
		
		CloseOp(RegExp exp)
		{
			this.exp = exp;
		}

		void accept(OpVisitor v)
		{
			v.visit(this);
		}
	}

	static class CloseVOp extends RegExp
	{
		RegExp exp;
		int    min;
		int    max;
		
		CloseVOp(RegExp exp, int min, int max)
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

	static class RuleOp extends RegExp
	{
		RegExp   exp;
		RegExp   ctx;
		NFA.Node node;
		int      accept;
		String   eventName;
		
		RuleOp(RegExp exp, RegExp ctx, int accept)
		{
			this.exp = exp;
			this.ctx = ctx;
			this.accept = accept;
		}

		RuleOp(RegExp exp, RegExp ctx, int accept, String eventName)
		{
			this(exp, ctx, accept);
			this.eventName = eventName;
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
