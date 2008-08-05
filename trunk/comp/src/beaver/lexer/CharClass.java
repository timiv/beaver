package beaver.lexer;


class CharClass
{
	/**
	 * Class uses code of one of its characters (a "representative") as its ID
	 */ 
	int id;
	
	/**
	 * Number of characters in the class
	 */
	int cardinality;

	/**
	 * The range of characters in this class
	 */
	CharRange range;
	
	CharClass()
	{
		this.id = -1;
	}
	
	public boolean equals(Object obj)
	{
		return obj instanceof CharClass
		    && cardinality == ((CharClass) obj).cardinality
		    && range.equals(((CharClass) obj).range);
	}
}
