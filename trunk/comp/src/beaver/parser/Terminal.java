package beaver.parser;

class Terminal extends Symbol
{
	/**
	 * Precedence: 0 - undefined (lowest), 0xffff - highest
	 */
	char precedence;

	/**
	 * 'L' - left, 'R' - right, 'N' - none
	 */
	char associativity;
	
	/**
	 * A text that this terminal matches. Only relevant for terminals
	 * that match keywords. 
	 */
	String text;

	Terminal(String name)
	{
		super(name);
	}

	Terminal(String name, String text)
	{
		super(name);
		this.text = text;
	}
	
	/**
	 * Terminal cannot match an empty string.
	 * 
	 * @return false
	 */
	boolean isNullable()
	{
		return false;
	}
	
	public String toString()
	{
		return text != null ? '"' + text + '"' : name;
	}
}
