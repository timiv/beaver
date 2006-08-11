package beaver.comp.lexer;

import beaver.util.CharMap;

/**
 * Represents a set of all characters recognized by the scanner
 */
class CharSet
{
	final CharMap classes;
	
	/**
	 * number of unique classes in this set
	 */
	final int size;
	
	/**
	 * code of the smallest character in the set
	 */
	final char min;
	
	/**
	 * code of the largest character in the set
	 */
	final char max;
	
	CharSet(RegExp re)
	{
		CharClass.Builder cb = new CharClass.Builder();
		re.accept(cb);
		classes = cb.classes;
		
		CharClass.Counter cc = new CharClass.Counter();
		classes.forEachEntryAccept(cc);
		size = cc.n;
		min = cc.min;
		max = cc.max;
		
		CharClass.setSpans(classes, min, max);
	}
	
	CharClass getCharClass(char c)
	{
		return (CharClass) classes.get(c);
	}
}
