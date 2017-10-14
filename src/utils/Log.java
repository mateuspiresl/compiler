package utils;

public class Log
{
	public static boolean DEBUG = false;
	
	public static void d(int tabs, String message)
	{
		if (DEBUG)
		{
			while (tabs-- > 0) System.out.print("  ");
			System.out.println(message);
		}
	}
}
