package web_crawler;

import java.util.IllegalFormatException;
import java.util.concurrent.Callable;

/**
 * TODO
 * @author Kevin Bechman
 *
 * @param <V>
 */
public abstract class TimedCallable<V> implements Callable<V>{
	private Stopwatch stopwatch;
	
	public TimedCallable() {
		stopwatch = new Stopwatch();
		logThread("Thread Created.");
	}
	
	protected void logThread(String event) {
		try {
			System.out.printf("[%.6f] "+Thread.currentThread()+" ["+event+"]\n", stopwatch.seconds());
		} catch (IllegalFormatException e) {
			System.out.printf("[%.6f] "+Thread.currentThread()+" [Error in printing.]\n", stopwatch.seconds());
		}
	}
}
