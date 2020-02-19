package web_crawler.async_crawler;

public class URLNode {
	private boolean isCrawled;	// Whether or not the web page at this URL has been crawled
	private int numTimesObserved;	// Number of times this URL was seen on a web page
	private URLNode parent;	// URL of web page with lowest depth that has a link to this URL
	public final String url;  		// This URL
	public final int depth;	// Depth from starting URL
	
	public URLNode (String url, URLNode parent, int depth) {
		this.url = url;
		this.parent = parent;
		this.depth = depth;
		isCrawled = false;
		numTimesObserved = 1;
	}
	
	public void setCrawled() { isCrawled = true; }
	public void incrObserved() { numTimesObserved++; }
	public boolean isCrawled() { return isCrawled; }
	public int numTimesObserved() { return numTimesObserved; }
	public void setParent(URLNode parent) { this.parent = parent; }
	public URLNode getParent() { return parent; }
	
	@Override
	public String toString() {
		String parentURL;
		if (parent == null)
			parentURL = "null";
		else parentURL = parent.url;
		return String.format("[Seen %d times, crawled: %d, depth: %d, URL: %s, parent URL: %s]",
				numTimesObserved, isCrawled, depth, url, parentURL);
	}
}
