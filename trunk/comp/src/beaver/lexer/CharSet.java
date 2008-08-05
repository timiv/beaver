package beaver.lexer;

/**
 * Represents a set of all recognizable characters and their classes
 */
class CharSet
{
	CharMap classes;

	/**
	 * number of unique classes in this set
	 */
	int     size;

	CharSet(RegExp re)
	{
		CharClassBuilder builder = new CharClassBuilder();
		re.accept(builder);
		classes = builder.getCharClassesMap();
		size    = builder.getNumberOfClasses();
	}
}
