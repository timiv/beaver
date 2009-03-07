package beaver.parser;

import java.util.Comparator;

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
	/**
	 * Number of states where this symbol is used as a lookahead
	 */
	int    numStates;

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
	
	/**
	 * Informs the caller if this symbols carries a value. A value is non-keyword/non-static data
	 * matched by a scanner.  
	 * 
	 * @return true is symbol carries a value
	 */
	abstract boolean isValueProducer();
	
	static Comparator CMP_NUM_STATES = new Comparator()
	{
		public int compare(Object obj1, Object obj2)
        {
			Symbol sym1 = (Symbol) obj1;
			Symbol sym2 = (Symbol) obj2;
			
	        int cmp = sym2.numStates - sym1.numStates;
	        if (cmp == 0)
	        {
	        	cmp = sym1.id - sym2.id;
	        }
	        return cmp;
        }
	};
}
