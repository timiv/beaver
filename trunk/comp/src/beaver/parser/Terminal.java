package beaver.parser;

public class Terminal extends Symbol
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
	
	/**
	 * Terminal produces a value when it matches a non-static/non-keyword text.
	 */
	public boolean isValueProducer()
	{
		return text == null;
	}
	
	public String toString()
	{
		return text != null ? '"' + text + '"' : name;
	}
	
	public String getText()
	{
		return text;
	}
}
