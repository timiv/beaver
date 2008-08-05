package beaver.parser;

abstract class Symbol
{
	/**
	 * Symbol's ID
	 */
	int    id;

	/**
	 * Name that was used to reference this symbol in the specification.
	 */
	String name;

	Symbol(String name)
	{
		this.name = name;
	}

	public String toString()
	{
		return name;
	}

	/**
	 * Informs the caller if this symbols can match an empty string. This method always return false
	 * for terminals. For non-terminals result depends on whether on of their derivation rules can
	 * match an empty string.
	 * 
	 * @return true if symbol can match an empty string
	 */
	abstract boolean isNullable();
}
