package web_crawler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import web_crawler.recursive_single_thread.SingleThreadCrawler;

/*
 * Notes:
 * What the web crawler is supposed to do:
 * -Be given a URL and a depth of which to crawl, and return a HashMap of URL's encountered,
 * 		with the number of times they were encountered.
 * 
 * 
 * Strat 1:
 * A HM of <URL, # of times seen> will be maintained for any URL seen by the crawling threads.
 * A blocking queue that stores all the URL's (and their associated depth) to be analyzed by the threads.
 * So, generally, the threads will:
 * 		- Wait for some amount of time (maybe random).
 * 		- Take a URL from the queue and access its HTML body (to get all URL links within the web page)
 * 		- Record that URL in the "encountered URLs" HashMap, incrementing its count
 * 		- If lowest depth has been reached, return count of URL's analyzed
 * 			Else, go back to step 1
 * 	
 * TODO Have to account for a website being visited by two threads at same time
 */



public class Crawler {
	public final static int SLEEP_TIME = 300;
	// TODO Change to Runnable; don't return anything, but pass in an Integer for all threads to increment?
	static class LinkCrawler implements Callable<Integer> {
		int n;  										// Count of URL's parsed
		HashMap<String, Integer> linkCounts;  			// Records of encountered URI's
		Set<String> parsedLinks;						// URI's of web pages that have been parsed for links,
														//   to prevent repeat parsing.
		LinkedBlockingQueue<LinkHolder> processQueue;	// Contains URI's that still need to be processed 
		ArrayList<LinkHolder> linksToBeQueued;			// Links encountered that should be queued
		Stopwatch stopwatch;
		LinkCrawler(
				HashMap<String, Integer> linkCounts,
				Set<String> parsedLinks,
				LinkedBlockingQueue<LinkHolder> processQueue) {
			this.linkCounts = linkCounts;
			this.parsedLinks = parsedLinks;
			this.processQueue = processQueue;
			this.linksToBeQueued = new ArrayList<>();
			n = 0;
		}
		public Integer call() {
			stopwatch = new Stopwatch();
			logThread("Started");
			logThread(""+parsedLinks.getClass());
			// Start main loop
			while(true) {
				// Wait for a bit
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					logThread("Interrupted while sleeping");
					e.printStackTrace();
					return n;
				}
				// Check if thread is interrupted
				if (Thread.interrupted()) {
					logThread("Interrupted");
					return n;
				}

				// Take a LinkHolder item from front of queue.
				LinkHolder linkHolder;
				try {
					// Queue is accessible from multiple threads
					// Be sure that only one thread can access it (and mark a URI as parsed) at a time
					logThread("Entering processQueue sync.");
					synchronized (processQueue) {
						// Queue any links
						if (!linksToBeQueued.isEmpty()) {
							for (LinkHolder lh : linksToBeQueued) {
								processQueue.add(lh);
								logThread("Queued: "+lh.path+" at depth: "+(lh.depth));
							}
						}
						logThread("Process Queue: "+processQueue);
						linkHolder = processQueue.poll(10, TimeUnit.SECONDS);
						// If timeout occurs, there must not be anything else to process, so return.
						if (linkHolder == null) {
							logThread("Queue remaining empty; ending");
							return n;
						}
						logThread("Parsing: "+linkHolder.path);
						parsedLinks.add(linkHolder.path);
						n++;
					}
					logThread("Exiting processQueue sync.");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					logThread("Interrupted in polling");
					e.printStackTrace();
					return n;
				}
				// Connect to the given linkHolder's webpage using Jsoup and retrieve the webpage as a Document
				Document doc;
				try {
					doc = Jsoup.connect(linkHolder.path).get();
				} catch (Exception e) {
					e.printStackTrace();
					logThread("Couldn't connect to " + linkHolder.path);
					return n;
				}
				// Get a list of links from linkHolder's URI using Jsoup
				ArrayList<String> links = getLinks(doc);

				// Process the links
				for (String s : links) {
					// Record link as encountered and increment if need be
					logThread("Entering linkCounts sync.");
					synchronized (linkCounts) {
						Integer val = linkCounts.get(s);
						if (val == null) {
							linkCounts.put(s, 1);
							logThread("Added: "+s);
						}
						else {
							linkCounts.put(s, val+1);
							logThread("Count incremented: "+s);
						}
					}
					logThread("Exiting linkCounts sync.");
					// If the depth of the given linkHolder is greater than 0,
					// add a new LinkHolder for each observed string (that hasn't already been parsed)
					logThread("Entering parsedLinks sync.");
					synchronized (parsedLinks) {
						if (linkHolder.depth > 1 && !parsedLinks.contains(s)) {
							linksToBeQueued.add(new LinkHolder(s, linkHolder.depth - 1));
							parsedLinks.add(s);
						}
					}
					logThread("Exiting pasredLinks sync.");
				}


			}
		}
		
		private void logThread(String event) {
			try {
				System.out.printf("[%.6f] "+Thread.currentThread()+" ["+event+"]\n", stopwatch.seconds());
			} catch (Exception e) {
				System.out.printf("[%.6f] "+Thread.currentThread()+" [Error: Can't process path]\n", stopwatch.seconds());
			}
		}
	}
	
	public static void main(String args[]) throws Exception {
//		String path = "https://en.wikipedia.org/wiki/Wikipedia:Today%27s_featured_article/January_2020";
//		String path2 = "https://jsoup.org/cookbook/";
//		URI u = null;
//		u = new URI(path2);
//		System.out.println(
////				u.getHost()+"\n"+
////				u.getPort()+"\n"+
////				u.getQuery()+"\n"+
////				u.getFragment()+"\n"+
////				u.getScheme()+"\n"+
////				u.getUserInfo()+"\n"+
////				u.normalize()+"\n"+
////				u.getAuthority()+"\n"+
////				u.getRawAuthority()+"\n"+
////				u.getPath()+"\n"+
////				u.getRawPath()+"\n"+
////				u.getScheme()+":"+u.getSchemeSpecificPart()+"\n"+
//				path2+"\n"+
//				simplifyLink(path2) + "\n"+
//				""
//		);
//		Web web = SingleThreadCrawler.crawl("https://jsoup.org/", 2);
//		//web.printMap();
//		web.printPathToStart("https://en.wikipedia.org/wiki/Tropical_wave");
		
		Link link = new Link("blabla", null, 3);
		System.out.println(link);
		
		
		// Initialize the path and depth
//		String path = "https://jsoup.org/";
//		try {
//			path = simplifyLink(path);
//		} catch (URISyntaxException e1) {
//			// TODO Auto-generated catch block
//			System.out.println("Supplied path is malformed.");
//			return;
//		}
//		int depth = 2;
//		
//		// Initialize the various data structures to pass into the threads
//		HashMap<String, Integer> linkCounts = new HashMap<>();
//		Set<String> parsedLinks = Collections.synchronizedSet(new HashSet<String>());
//		LinkedBlockingQueue<LinkHolder> processQueue = new LinkedBlockingQueue<LinkHolder>();
//		
//		// Add the starting link to crawl
//		processQueue.offer(new LinkHolder(path, depth));
//		parsedLinks.add(path);
////		processQueue.offer(new LinkHolder(path2, depth));
////		parsedLinks.add(path2);
////		processQueue.offer(new LinkHolder(path3, depth));
////		parsedLinks.add(path3);
////		processQueue.offer(new LinkHolder(path4, depth));
////		parsedLinks.add(path4);
//		
//		// Create threads and run them
//		int numThreads = 4;
//		ExecutorService crawlerPool = Executors.newFixedThreadPool(numThreads);
//		ArrayList<Future<Integer>> results = new ArrayList<>();
//		for (int i=0; i<numThreads; i++) {
//			results.add(crawlerPool.submit(new LinkCrawler(linkCounts, parsedLinks, processQueue)));
//		}
//		
//		// Wait for the results
//		int totalAccesses = 0;
//		try {
//			for (Future<Integer> result : results) {
//				totalAccesses += result.get(300, TimeUnit.SECONDS).intValue();
//			}
//		} catch (Exception e) {
//			totalAccesses = -1;
//			e.printStackTrace();
//			return;
//		}
//		
//		Iterator<Map.Entry<String,Integer>> linksIterator = linkCounts.entrySet().iterator();
//		Iterator<String> parsedIterator = parsedLinks.iterator();
//		
//		// output
//		System.out.println("*** Output ***");
//		System.out.println("Unprocessed links:\n"+crawlerPool.shutdownNow());
//		System.out.println("------------------------");
//		System.out.println("Parsed links:");
//		while(parsedIterator.hasNext()) {
//			System.out.println(parsedIterator.next());
//		}
//		System.out.println("------------------------");
//		System.out.println("Encountered links:");
//		while(linksIterator.hasNext()) {
//			System.out.println(linksIterator.next());
//		}
//		System.out.println("------------------------");
//		System.out.println("Accesses: "+totalAccesses);
		
		 
	}
	
	/**
	 * TODO
	 * @param path
	 * @return
	 * @throws URISyntaxException
	 */
	public static String simplifyLink(String path) throws URISyntaxException {
		// Cut off trailing '/' character if it exists.
		if (path.charAt(path.length()-1) == '/') {
			path = path.substring(0, path.length()-1);
		}
		URI uri = new URI(path);
		return uri.getScheme()+":"+uri.getSchemeSpecificPart();
	}
	
	/**
	 * TODO
	 * @param doc
	 * @return
	 */
	public static ArrayList<String> getLinks(Document doc) {
		// TODO this is really inefficient probably
		ArrayList<String> links = new ArrayList<>();
		Elements linkElements = doc.select("a[href]");
		for (Element el : linkElements) {
			String s = el.attr("abs:href");
			try {
				s = Crawler.simplifyLink(s);
				if (!links.contains(s))
					links.add(s);
			} catch (Exception e) {
				// do nothing; don't add the link
			}
		}
		return links;
	}
	
	/**
	 * TODO
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static Document connect(String url) throws IOException {
		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//log("Connecting to: "+url);
		Document doc = Jsoup.connect(url).get();
		return doc;
	}
}
