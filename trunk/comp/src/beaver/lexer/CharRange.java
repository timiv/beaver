package beaver.lexer;

/**
 * A range of characters - an ordered set of single characters and character spans
 * 
 * @author Alexander Demenchuk
 * 
 */
public class CharRange
{
	CharSpan[] spans;

	CharRange()
	{
		spans = EMPTY;
	}

	CharRange(CharSpan span)
	{
		spans = new CharSpan[] { span };
	}

	/**
	 * Compiles the range from its string representation.
	 * 
	 * @param str range representation
	 */
	CharRange(CharReader str)
	{
		int strStart = str.mark();
		boolean inv = !str.isEmpty() && str.readChar() == '^';
		if (inv)
		{
			spans = new CharSpan[] { new CharSpan('\0', '\ufffe') };
		}
		else
		{
			spans = EMPTY;
			str.reset(strStart);
		}
		while (!str.isEmpty())
		{
			char lb = (char) str.readChar();
			if (str.isEmpty())
			{
				if (inv) 
				{ 
					sub(lb); 
				} 
				else
				{ 
					add(lb);
				} 
				break;
			}
			char nc = (char) str.readChar();
			while (nc != '-') 
			{
				if (inv) 
				{ 
					sub(lb); 
				} 
				else
				{ 
					add(lb);
				} 
				if (str.isEmpty())
				{
					break;
				}
				else
				{
					lb = nc;
					nc = (char) str.readChar();
				}
			}
			if (str.isEmpty())
			{
				if (inv)
				{
					sub(nc);
				}
				else
				{
					add(nc);
				}
				break;
			}
			char rb = (char) str.readChar();
			if (inv)
			{
				sub(new CharSpan(lb, rb));
			}
			else
			{
				add(new CharSpan(lb, rb));
			}
		}
	}
	
	public String toString()
	{
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < spans.length; i++)
        {
	        str.append(spans[i].toString());
        }
		return str.toString();
	}
	
	public boolean equals(Object obj)
	{
		return obj instanceof CharRange && this.equals((CharRange) obj);
	}
	
	int size()
	{
		int n = 0;
		for (int i = 0; i < spans.length; i++)
        {
	        n += spans[i].size();
        }
		return n;
	}
	
	boolean contains(char c)
	{
		for (int i = 0; i < spans.length; i++)
		{
			if (spans[i].contains(c))
				return true;
		}
		return false;
	}
	
	/**
	 * Adds another span to the range. Keeps spans ordered. The specified span will be merged with an existing span if
	 * possible.
	 *  
	 * @param span to add to this range. The span instance might be inserted into the range.
	 */
	void add(CharSpan span)
	{
		int index = 0, cmp = 0;
		while (index < spans.length && (cmp = spans[index].compare(span)) < 0)
		{
			index++;
		}
		if (index < spans.length && cmp == 0)
		{
			spans[index].add(span);
			if (index + 1 < spans.length && spans[index].compare(spans[index + 1]) == 0)
			{
				spans[index].add(spans[index + 1]);
				remove(index + 1);
			}
		}
		else
		{
			insertAt(index, span);
		}
	}
	
	/**
	 * Adds a character to the range
	 * 
	 * @param c character to add
	 */
	void add(char c)
	{
		int index = 0, cmp = 0;
		while (index < spans.length && (cmp = spans[index].compare(c)) < 0)
		{
			index++;
		}
		if (index < spans.length && cmp == 0)
		{
			spans[index].add(c);
			if (index + 1 < spans.length && spans[index].compare(spans[index + 1]) == 0)
			{
				spans[index].add(spans[index + 1]);
				remove(index + 1);
			}
		}
		else
		{
			insertAt(index, new CharSpan(c));
		}
	}
	
	/**
	 * Adds characters from another range to this range
	 * 
	 * @param range to add
	 */
	void add(CharRange range)
	{
		for (int i = 0; i < range.spans.length; i++)
        {
			this.add(new CharSpan(range.spans[i]));
        }
	}

	/**
	 * Removes a single character from this range  
	 * 
	 * @param c character to remove
	 */
	void sub(char c)
	{
		sub(new CharSpan(c));
	}
	
	/**
	 * Removes characters from this range that are present in the subtrahend span  
	 * 
	 * @param subSpan
	 */
	void sub(CharSpan subSpan)
	{
		for (int i = 0; i < spans.length && spans.length > 0; i++) // note, "spans" array may shrink
		{
			CharSpan span = spans[i];
			
            int cmp = span.compare(subSpan);
            if (cmp < 0)
            	continue;
            if (cmp > 0)
            	break;
            
            if (subSpan.isInnerSubsetOf(span))
    		{
    			insertAt(i + 1, span.subSplit(subSpan));
    			break;
    		}
    		else if (span.isSubsetOf(subSpan))
    		{
    			remove(i);
    			// there is now a different span at i
    			--i;
    		}
    		else if (span.intersects(subSpan))
        	{
    			span.sub(subSpan);
        	}
		}
	}
	
	/**
	 * Removes characters from this range that are present in the other range  
	 * 
	 * @param range to subtract
	 */
	void sub(CharRange range)
	{
		for (int i = 0; i < range.spans.length; i++)
		{
			this.sub(range.spans[i]);
		}
	}
	
	boolean equals(CharRange range)
	{
		if (this.spans.length != range.spans.length)
		{
			return false;
		}
		for (int i = 0; i < this.spans.length; i++)
        {
	        if (!this.spans[i].equals(range.spans[i]))
	        {
	        	return false;
	        }
        }
		return true;
	}
	
	void accept(CharVisitor visitor)
	{
		for (int i = 0; i < spans.length; i++)
        {
	        spans[i].accept(visitor);
        }
	}

	private void insertAt(int index, CharSpan span)
	{
		if (index > spans.length)
			throw new IndexOutOfBoundsException();

		CharSpan[] newSpans = new CharSpan[spans.length + 1];
		if (index == 0)
		{
			newSpans[0] = span;
			System.arraycopy(spans, 0, newSpans, 1, spans.length);
		}
		else if (index == spans.length)
		{
			System.arraycopy(spans, 0, newSpans, 0, spans.length);
			newSpans[index] = span;
		}
		else
		{
			System.arraycopy(spans, 0, newSpans, 0, index);
			newSpans[index] = span;
			System.arraycopy(spans, index, newSpans, index + 1, spans.length - index);
		}
		spans = newSpans;
	}
	
	private void remove(int index)
	{
		CharSpan[] newSpans = new CharSpan[spans.length - 1];
		if (index == newSpans.length)
		{
			System.arraycopy(spans, 0, newSpans, 0, newSpans.length);
		}
		else
		{
			System.arraycopy(spans, 0, newSpans, 0, index);
			System.arraycopy(spans, index + 1, newSpans, index, newSpans.length - index);
		}
		spans = newSpans;
	}
	
	private static final CharSpan[] EMPTY = new CharSpan[0];
}
