package utils;

public class Log
{
	public static boolean DEBUG = false;
	public static boolean TEST = true;
	
	public static void d(int tabs, String message)
	{
		if (DEBUG)
		{
			while (tabs-- > 0) System.out.print("  ");
			System.out.println(message);
		}
	}
	
	public static void t(int tabs, String message)
	{
		if (TEST)
		{
			while (tabs-- > 0) System.out.print("  ");
			System.out.println(message);
		}
	}
}
