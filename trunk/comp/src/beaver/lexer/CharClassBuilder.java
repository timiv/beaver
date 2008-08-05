/**
 * 
 */
package beaver.lexer;

import java.util.HashMap;
import java.util.Map;

import beaver.lexer.RegExp.Alt;
import beaver.lexer.RegExp.Cat;
import beaver.lexer.RegExp.Close;
import beaver.lexer.RegExp.MatchChar;
import beaver.lexer.RegExp.MatchRange;
import beaver.lexer.RegExp.Null;
import beaver.lexer.RegExp.Rule;

class CharClassBuilder implements RegExp.Visitor
{
	private final CharMap classes = new CharMap();
	private int nClasses = 0;
	
	CharMap getCharClassesMap()
	{
		if (nClasses == 0)
		{
			CharClassFinalizer finalizer = new CharClassFinalizer();
			classes.accept(finalizer);
			nClasses = finalizer.counter;
		}
		return classes;
	}
	
	int getNumberOfClasses()
	{
		if (nClasses == 0)
		{
			CharClassFinalizer finalizer = new CharClassFinalizer();
			classes.accept(finalizer);
			nClasses = finalizer.counter;
		}
		return nClasses;
	}

	public void visit(Null op)
    {
		// nothing to do here
    }

	public void visit(MatchChar op)
    {
        CharClass cls = (CharClass) classes.get(op.c);
        if (cls != null)
        {
        	if (cls.cardinality == 1)
        		return;
        	// "remove" character from the original class
        	cls.cardinality--;
        }
        // "put" this character into the new class
        cls = new CharClass();
        cls.cardinality++;
        classes.put(op.c, cls);
    }

	public void visit(MatchRange op)
    {
        op.range.accept(new CharVisitor()
        {
        	CharClass newClass = null;
        	Map splitClasses = new HashMap();
        	
			public void visit(char c)
            {
		        CharClass cls = (CharClass) classes.get(c);
		        if (cls == null) // not classified yet
		        {
		        	if (newClass == null)
		        		newClass = new CharClass();
		        	newClass.cardinality++;
		        	classes.put(c, newClass);
		        }
		        else
		        {
		        	CharClass splitClass = (CharClass) splitClasses.get(cls);
		        	if (splitClass == null) // have not started splitting this class yet
		        	{
		        		splitClasses.put(cls, splitClass = new CharClass());
		        	}
		        	// "remove" character from the original class
		        	cls.cardinality--;
		        	// and "put" it into the split class
		        	splitClass.cardinality++;
		        	// associate current character with the split class
		        	classes.put(c, splitClass);
		        }
            }
        });
        
    }

	public void visit(Alt op)
    {
		op.exp1.accept(this);
		op.exp2.accept(this);
    }

	public void visit(Cat op)
    {
		op.exp1.accept(this);
		op.exp2.accept(this);
    }

	public void visit(Close op)
    {
		op.exp.accept(this);
    }

	public void visit(Rule op)
    {
		op.exp.accept(this);
		op.ctx.accept(this);
    }

	/**
     * Count unique classes and builds ranges of characters for all classes. 
     */
    static class CharClassFinalizer implements CharMap.EntryVisitor
    {
    	int counter = 0;
    	
    	public void visit(char c, Object v)
    	{
    		CharClass cls = (CharClass) v;
    		if (cls.id < 0)
    		{
    			cls.id = c;
    			cls.range = new CharRange();
    			counter++;
    		}
    		else if (cls.id > c) // this is not really necessary, but it eases test writing
    		{
    			cls.id = c;
    		}
    		cls.range.add(c);
    	}
    }
	
}