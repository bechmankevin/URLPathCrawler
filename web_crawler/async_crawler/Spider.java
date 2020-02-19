package web_crawler.async_crawler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import web_crawler.Crawler;
import web_crawler.TimedCallable;

/**
 * This class will:
 *  -Connect to a web page and parse it for URLs
 *  -Add those URLs to a HostQueueDepot for queueing
 *  -Maintain a host URL to parse
 */
public class Spider extends TimedCallable<Integer> {
	// Constants:
	public final static int WAIT_TIME = 5000;

	// Fields and Structures:
	private HostQueueDepot queueDepot;
	private HashMap<String, URLNode> archives;
	private String startURL, targetURL;
	private int depthLimit;
	
	private LinkedList<URLNode> toBeQueued;
	private String host;
	private int n;	// Count of URL's parsed
	private boolean waiting;
	
	public Spider(HostQueueDepot queueDepot, HashMap<String, URLNode> archives, 
			String startURL, String targetURL, int depthLimit) {
		this.queueDepot = queueDepot;
		this.archives = archives;
		this.depthLimit = depthLimit;
		this.startURL = startURL;
		this.targetURL = targetURL;
		
		this.toBeQueued = new LinkedList<URLNode>();
		this.host = null;
		n = 0;
		waiting = false;
	}

	@Override
	public Integer call() {
		while (true) {
			// If thread was not able to acquire a new host, let it wait for more to be added.
			if (waiting) {
				try {
					Thread.sleep(WAIT_TIME);
					waiting = false;
				} catch (InterruptedException e) {
					logThread("Interrupted.");
					return n;
				}
			}
			// Begin thread-sensitive segment:
			// The thread needs to add its list of nodes into the Depot's queue system,
			// ensure that its host still has more nodes to be parsed (assigning a new host if not),
			// and get a new node to parse from the Depot.
			URLNode currentNode;
			synchronized (queueDepot) {
				// Add URLNodes to be parsed into their respective host queues
				queueDepot.add(toBeQueued);
				toBeQueued.clear();
				
				// Assign host to be parsed by this thread if need be
				if (host == null || queueDepot.queueIsEmpty(host)) {
					// If another host is available to be parsed, set it as this thread's host
					if (queueDepot.hasAvailHosts()) {
						host = queueDepot.getNewHost();
					}
					else {
						waiting = true;
					}
				}
				// Access next URLNode in the queue corresponding to this thread's current host
				if (!waiting) {
					currentNode = queueDepot.getNextURL(host);
				}
				else {
					currentNode = null;
				}
				// TODO clean this area up a bit ^
			}
			
			// Now either a new node has been received or there are none to parse
			if (!waiting) {
				// Establish connection to the given node's URL
				Document doc;
				try {
					doc = Jsoup.connect(currentNode.url).get();
				} catch (Exception e) {
					logThread("Couldn't connect to " + currentNode.url);
					return n;
				}
				// Get a list of links occurring in the web page.
				List<String> links = Crawler.getLinks(doc);
				// For each link, 
				for (String link : links) {
					
				}
			}
		}
	}

	
	
	
	
	
	
	
	
	
}
