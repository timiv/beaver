package beaver.lexer;

/**
 * A single span of consecutive characters
 * 
 * @author Alexander Demenchuk
 *
 */
class CharSpan
{
	char lb, ub; // [lb,ub)

	CharSpan(char lb, char ub)
	{
		if (lb > ub)
			throw new IllegalArgumentException("lb > ub");
		this.lb = lb;
		this.ub = (char) (ub + 1);
	}
	
	CharSpan(char c)
	{
		this.lb = c;
		this.ub = (char) (c + 1);
	}
	
	CharSpan(CharSpan span)
	{
		this.lb = span.lb;
		this.ub = span.ub;
	}
	
	int size()
	{
		return ub - lb;
	}
	
	boolean equals(CharSpan span)
	{
		return this.lb == span.lb && this.ub == span.ub;
	}
	
	boolean intersects(CharSpan span)
	{
		return this.lb < span.ub && span.lb < this.ub;
	}
	
	boolean isAdjacentTo(CharSpan span)
	{
		return this.lb == span.ub || this.ub == span.lb;
	}
	
	boolean isSubsetOf(CharSpan span)
	{
		return span.lb <= this.lb && this.ub <= span.ub;
	}
	
	boolean contains(char c)
	{
		return this.lb <= c && c < this.ub;
	}
	
	boolean isAdjacentTo(char c)
	{
		return this.lb - c == 1 || c == this.ub;
	}
	
	/**
	 * Compares relative position of the two spans.
	 * 
	 * @param span
	 * @return 0 if this span intersects with or is adjacent to the specified span
	 *         1 if this span completely to the right of the specified span
	 *        -1 if this span completely to the left of the specified span
	 */
	int compare(CharSpan span)
	{
		if (this.ub < span.lb)
			return -1;
		if (span.ub < this.lb)
			return 1;
		return 0; 
	}
	
	/**
	 * Compares the position of the span relative to the specified character
	 * 
	 * @param c character 
	 * @return 0 if this span contains or is adjacent to the specified character 
	 *         1 if this span completely to the right of the specified character
	 *        -1 if this span completely to the left of the specified character
	 */
	int compare(char c)
	{
		if (this.ub < c)
			return -1;
		if (this.lb - c > 1)
			return 1;
		return 0; 
	}
	
	/**
	 * Expands the current span to also include characters from the other span. 
	 *  
	 * @param span
	 */
	void add(CharSpan span)
	{
		if (compare(span) != 0)
			throw new IllegalArgumentException("disjoint span");
		if (this.lb > span.lb)
			this.lb = span.lb;
		if (this.ub < span.ub)
			this.ub = span.ub;
	}
	
	/**
	 * Adds a character to the span
	 *  
	 * @param c
	 */
	void add(char c)
	{
		if (!this.contains(c))
		{
			if (this.ub == c)
				this.ub++;
			else if (this.lb - c == 1)
				this.lb--;
			else
				throw new IllegalArgumentException("disjoint char");
		}
	}
	
	void accept(CharVisitor visitor)
	{
		for (char c = lb; c < ub; c++)
		{
			visitor.visit(c);
		}
	}
	
	public String toString()
	{
		switch (ub - lb)
		{
			case 1:
			{
				return Character.toString(lb);
			}
			case 2:
			{
				return Character.toString(lb) + (char) (lb + 1);
			}
			default:
			{
				return lb + "-" + (char) (ub - 1);
			}
		}
	}
	
	public boolean equals(Object obj)
	{
		return obj instanceof CharSpan && this.equals((CharSpan) obj);
	}

}