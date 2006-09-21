/**
 * 
 */
package beaver.comp;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import beaver.SyntaxErrorException;
import beaver.comp.spec.AstBuilder;
import beaver.comp.spec.Spec;
import beaver.comp.spec.SpecScanner;

/**
 * @author Alexander Demenchuk
 *
 */
public class Compiler
{
	public void compile(File src) throws IOException, SyntaxErrorException
	{
		Spec spec = (Spec) new AstBuilder().parse(new SpecScanner(new FileReader(src)));
		spec.accept(new InlineRulesExtractor());
		spec.accept(new EbnfOperatorCompiler());
		spec.accept(new InlineStringExractor());
	}
	
}
