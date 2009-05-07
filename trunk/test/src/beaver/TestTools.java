package beaver;

import java.io.IOException;
import java.io.InputStreamReader;

import beaver.cc.Log;

public final class TestTools
{
	public static String readResource(Class<?> refClass, String name) throws IOException
    {
    	InputStreamReader is = new InputStreamReader(refClass.getResourceAsStream(name));
    	StringBuilder txt = new StringBuilder(4096);
    	char[] buf = new char[1024];
    	for (int cnt = is.read(buf); cnt > 0; cnt = is.read(buf))
    	{
    		txt.append(buf, 0, cnt);
    	}
    	return txt.toString();
    }

	public static final Log consoleLog = new Log()
	{
		@Override
	    public void warning(String text)
	    {
		    System.out.print("Warning: ");
		    System.out.println(text);
	    }
	
		@Override
	    public void error(String text)
	    {
		    System.err.print("Error: ");
		    System.err.println(text);
	    }
	};
}
