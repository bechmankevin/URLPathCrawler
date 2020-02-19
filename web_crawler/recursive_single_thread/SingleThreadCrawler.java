package web_crawler.recursive_single_thread;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import web_crawler.Crawler;
import web_crawler.Link;
import web_crawler.Stopwatch;
import web_crawler.Web;

/**
 * Given a URL path, constructs a graph linking each encountered URL path to the page it was found on.
 * The graph can then be printed or searched.
 */
public class SingleThreadCrawler {
	// Constants:
	public final static int DEFAULT_DEPTH = 1;
	
	// Fields and Structures:
	private static HashMap<String, Link> linkMap;
//	private static LinkedList<Link> queue;
	private static Stopwatch stopwatch;
	private static int depthLimit;
	
	/**
	 * TODO
	 * @param url
	 * @return
	 */
	public static Web crawl(String url) {
		return crawl(url, DEFAULT_DEPTH);
	}
	
	/**
	 * 
	 * @param url
	 * @param depth
	 * @return
	 */
	public static Web crawl(String url, int depth) {
		linkMap = new HashMap<>();
		stopwatch = new Stopwatch();
		depthLimit = depth;
		try {
			url = Crawler.simplifyLink(url);
		} catch (URISyntaxException e) {
			// TODO ? what to do here
			e.printStackTrace();
			return null;
		}
		Link startLink = new Link(url, null, 0);
		linkMap.put(url, startLink);  // TODO how to handle a link that is malformed here?
		crawl(linkMap.get(url));
		log("Crawling complete!");
		return new Web(linkMap);
	}
	
	/**
	 * 
	 * @param url
	 * @param depth
	 * @return
	 */
	private static void crawl(Link link) {
		//log("Starting crawling of "+link);
		// Get the Document associated with the provided URL
		Document doc;
		try {
			doc = Crawler.connect(link.url);
		} catch (IOException e) {
			//log("Cannot connect to "+link.url);
			return;
		}
		linkMap.get(link.url).setCrawled();  // Indicate that this link has been crawled
		// Get the list of urls from the web page
		ArrayList<String> urls = Crawler.getLinks(doc);
		//log("Links: "+urls);
		// Add URL's to hashmap of links
		for (String url : urls) {
//			Link storedLink = null;
			// Only observe url if it is not a link to itself
			if (!url.equals(link.url)) {
				// If link has not been before, add it as new with this link as its parent and one deeper depth
				if (!linkMap.containsKey(url)) {
					//log("LinkMap does not contain the url "+url);
					linkMap.put(url, new Link(url, link, link.depth+1));
					//log(linkMap.get(url)+" put into LinkMap.");
				}
				// Else the link has been seen, so increment its "observed" count,
				// and add the current link as its parent.
				else {
					Link storedLink = linkMap.get(url);
					//log("Stored Link: "+storedLink);
					storedLink.incrObserved();
					//log(storedLink + " incremented and parent added.");
				}
				// TODO Check depths of parents?
				// If this link has not been crawled and its depth is not beyond the depth limit, crawl it.
				Link newLink = linkMap.get(url);
				if (!newLink.isCrawled() && newLink.depth <= depthLimit) {
					crawl(linkMap.get(url));
				}
			}
		}
		//log(link.url+" finished with crawling.");
		//log("LinkMap: "+linkMap);
	}
	
	/**
	 * TODO
	 * @param event
	 */
	private static void log(String event) {
		try {
			System.out.printf("[%.6f] "+event+"\n", stopwatch.seconds());
		} catch (IllegalFormatException e) {
			System.out.printf("[%.6f] [Formatting error]\n", stopwatch.seconds());
		}
	}
}
