package web_crawler.async_crawler;

import java.util.HashMap;

/**
 * Keeps track of all the URLNodes based on the String representing their url.
 * Each node contains numerous details about how that URL was encountered, such as its depth level, 
 * how many times it has been observed, its parent, etc.
 */
public class URLArchive {
	private HashMap<String, URLNode> archives;
	
	public URLArchive() {
		this.archives = new HashMap<String, URLNode>();
	}
}
