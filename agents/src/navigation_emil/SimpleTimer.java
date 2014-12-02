package navigation_emil;

public class SimpleTimer {
	private static long startTime = System.currentTimeMillis();
	
	public static void reset() {
		startTime = System.currentTimeMillis();
	}
	
	public static void reset(String resetMessage) {
		System.out.print(resetMessage);
		reset();
	}
	
	public static void printTime() {
		long currTime = System.currentTimeMillis();
		int dt = (int) (currTime - startTime);
		int s = dt / 1000;
		int ms = dt - (s*1000);
		System.out.println(s+","+ms+"s");
	}
}
