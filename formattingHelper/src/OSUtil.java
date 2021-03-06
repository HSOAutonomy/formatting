public class OSUtil
{
	private static String OS = System.getProperty("os.name").toLowerCase();

	static boolean isWindows()
	{
		return (OS.contains("win"));
	}

	static boolean isMac()
	{
		return (OS.contains("mac"));
	}

	static boolean isUnix()
	{
		return (OS.contains("nix") || OS.contains("nux") || OS.indexOf("aix") > 0);
	}
}
