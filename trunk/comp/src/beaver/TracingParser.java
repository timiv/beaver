package beaver;

import java.io.IOException;
import java.io.InputStream;

public abstract class TracingParser extends Parser
{

	protected TracingParser(InputStream bptInputStream) throws IOException
	{
		super(bptInputStream);
	}
}
