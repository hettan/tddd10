package firebrigade.prediction.training;

public class Logger {

	public static enum Level { DEBUG, INFO, ERROR, WARNING };
	
	public static void Write(Level level, String msg)
	{
		System.out.println("[" + level.toString() + "]: " + msg);
	}
	
	public static void Write(String msg)
	{
		Write(Level.INFO, msg);
	}
}
