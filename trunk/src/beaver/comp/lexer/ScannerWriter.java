/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp.lexer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public abstract class ScannerWriter
{
	public void write(String className, File binDir, DFA defDFA, DFA[] inclusiveDFAs, DFA[] exclusiveDFAs) throws IOException
	{
		File clsFile = new File(binDir, className + ".class");
		File outDir = clsFile.getParentFile();
		if (!outDir.exists())
		{
			outDir.mkdirs();
		}

		FileOutputStream out = new FileOutputStream(clsFile);
		try
		{
			out.write(assemble(defDFA, inclusiveDFAs, exclusiveDFAs, className));
		}
		finally
		{
			out.close();
		}
	}
	
	protected abstract byte[] assemble(DFA defaultDFA, DFA[] incDFAs, DFA[] excDFAs, String className);

	protected static CharTransition[] getCharTransitions(DFA.State st)
	{
		ArrayList x = null;
		for (DFA.State.Transition t = st.move; t != null; t = t.next)
		{
			for (CharClass.Span s = t.span; s != null; s = s.next)
			{
				if (s.length() == 1)
				{
					if (x == null)
					{
						x = new ArrayList();
					}
					x.add(new CharTransition(s.lb, t.dest.sid));
				}
			}
		}
		if (x == null)
			return null;
		int n = x.size();
		if (n > 1)
			Collections.sort(x);
		return (CharTransition[]) x.toArray(new CharTransition[x.size()]);
	}

	protected static SpanTransition getSpanTransitionsTree(DFA.State st)
	{
		ArrayList x = null;
		for (DFA.State.Transition t = st.move; t != null; t = t.next)
		{
			for (CharClass.Span s = t.span; s != null; s = s.next)
			{
				if (s.length() > 1)
				{
					if (x == null)
					{
						x = new ArrayList();
					}
					x.add(new SpanTransition(s.lb, s.ub, t.dest.sid));
				}
			}
		}
		if (x == null)
			return null;
		int n = x.size();
		if (n > 1)
			Collections.sort(x);
		return SpanTransition.root(x, 0, n);
	}

	protected static class CharTransition implements Comparable
	{
		char c;
		int  to;

		CharTransition(char c, int to)
		{
			this.c = c;
			this.to = to;
		}

		public int compareTo(Object o)
		{
			return c - ((CharTransition) o).c;
		}
	}

	protected static class SpanTransition implements Comparable
	{
		char lb, ub;
		int  to;
		SpanTransition left, right;

		SpanTransition(char lb, char ub, int sid)
		{
			this.lb = lb;
			this.ub = ub;
			this.to = sid;
		}

		public int compareTo(Object o)
		{
			return lb - ((SpanTransition) o).lb;
		}

		static SpanTransition root(ArrayList jumps, int from, int to)
		{
			int d = to - from;
			if (d == 1)
			{
				return (SpanTransition) jumps.get(from);
			}
			else
			{
				int i = d / 2 + from;
				SpanTransition r = (SpanTransition) jumps.get(i);
				if (i > from)
					r.left = root(jumps, from, i);
				if (++i < to)
					r.right = root(jumps, i, to);
				return r;
			}
		}
	}
}
