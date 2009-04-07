package beaver.lexer;

public class Accept
{
	/**
	 * ID of the recognized token
	 */
	int id;
	/**
	 * Token precedence. When more than one rule matches the same token the ID
	 * the rule with higher precedence will be used. 
	 */
	int prec;
	/**
	 * When token is recognized an event, if present, that is associated with a
	 * recognition rule is fired.
	 */
	String event;
	/**
	 * A "pointer" to an event call-back method used by scanner generator
	 */
	int eventId;
	
	public Accept(int id, int prec)
	{
		this.id = id;
		this.prec = prec;
	}
	
	public Accept(int id, int prec, String event)
	{
		this(id, prec);
		this.event = event;
	}
}
