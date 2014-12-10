package navigation_emil;

import java.text.DecimalFormat;

/**
 * 
 * @author emiol791
 * A static timer for fast and simple time measurement.
 * 
 * Use:
 * SimpleTimer.reset("Time to do work: ");
 * //Do work
 * SimpleTimer.printTime();
 */
public class SimpleTimer {
	private static long startTime = System.currentTimeMillis();
	private static DecimalFormat formatter = new DecimalFormat("#0.000");
	private static int previousTime = 0;
	
	/**
	 * Reset the timer before measurement starts
	 */
	public static void reset() {
		startTime = System.currentTimeMillis();
	}
	
	/**
	 * Reset the timer before measurement starts
	 * @param resetMessage You can add a message that can be printed before measurement starts.
	 */
	public static void reset(String resetMessage) {
		System.out.print(resetMessage);
		reset();
	}
	
	/**
	 * Prints the time that has passed since reset() was called.
	 * Format: 3,14s
	 */
	public static void printTime() {
		long currTime = System.currentTimeMillis();
		previousTime = (int) (currTime - startTime);
		double s = (double)previousTime / 1000.0;
		System.out.println(formatter.format(s)+"s");
	}
	
	/**
	 * @return The last measured time span in milliseconds
	 */
	public static int getPreviousTimeMillis() {
		return previousTime;
	}
}
