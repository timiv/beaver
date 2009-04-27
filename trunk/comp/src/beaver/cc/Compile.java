package beaver.cc;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import beaver.CharScanner;
import beaver.Parser;
import beaver.SyntaxErrorException;
import beaver.cc.spec.BeaverParser;
import beaver.cc.spec.BeaverScanner;
import beaver.cc.spec.Spec;

public class Compile
{
	static Log log = new Log()
	{
	    public void warning(String text)
	    {
		    System.out.print("Warning: ");
		    System.out.println(text);
	    }
	
	    public void error(String text)
	    {
		    System.err.print("Error: ");
		    System.err.println(text);
	    }
	};
	
	public static void main(String[] args)
	{
		// the last argument should be the name of the file of the front-end spec
		if (args.length == 0)
		{
			log.warning(
					"No arguments were provided.\n" +
					"Usage: beaver.cc.Compile [options] FrontEndSpec.bps\n" +
					"Where options are:\n" +
					" -x  prefer Shift over Reduce in parser shift-reduce conflict resolutions\n" +
					" -x  do not write passthrough parser action methods\n" +
					" -x  generate AST stubs\n" +
					" -x  inline parser action methods\n" +
					" -x  save parser states into .bst file"
			);
			return;
		}
		File specFile = new File(args[args.length - 1]);
		if (!specFile.exists() || !specFile.canRead())
		{
			log.error("File " + specFile.getName() + " does not exist or inaccessible.");
			return;
		}
		try
		{			
			Spec spec = parse(specFile);
			if (spec != null)
			{
			}
		}
		catch (IOException e)
		{
			log.error("I/O -- " + e.getMessage());
		}
		catch (SyntaxErrorException e)
		{
			log.error("Syntax -- " + e.getMessage());
		}
	}
	
	static Spec parse(File specFile) throws IOException, SyntaxErrorException
	{
		Reader specReader = new FileReader(specFile);
		try
		{
			CharScanner lexer = new BeaverScanner(specReader);
			Parser specParser = new BeaverParser();
			return (Spec) specParser.parse(lexer);
		}
		finally
		{
			specReader.close();
		}
	}
	
}
