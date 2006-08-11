package beaver;

/**
 * Protocol to transfer symbol location information.
 * 
 * @author Alexander Demenchuk
 */
public interface Location
{
	void setLocation(int line, int column, int endLine, int endColumn);
}