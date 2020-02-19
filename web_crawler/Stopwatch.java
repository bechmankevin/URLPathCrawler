package web_crawler;

public class Stopwatch {
	private long startTime;
	public Stopwatch() {
		startTime = System.nanoTime();
	}
	public double seconds() {
		return ((double) (System.nanoTime() - startTime)) / 1000000000;
	}
}
