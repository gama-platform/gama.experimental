package visitors;

public class VisitorDebug {
	private static Boolean DEBUG = false;
	
	public static void DEBUG(String aString)
	{
		if(DEBUG)
		{
			System.out.println("[MD Documentation package] "+aString);
		}
	}
}
