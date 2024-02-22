public class XMLStringBuilder {
	StringBuilder theBuilder;
	
	public XMLStringBuilder()
	{
		theBuilder = new StringBuilder();
	}
	
	public void append(String aString)
	{
		theBuilder.append(aString);
	}
	
	public String toString()
	{
		return theBuilder.toString();
	}
	
	public void nextLine()
	{
		this.theBuilder.append("\n");
	}
	public void dispose()
	{
		this.theBuilder=null;
	}
}
