package web_crawler.async_crawler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class HostQueueDepot {
	private HashMap<String, LinkedList<URLNode>> hostQueueMap;
	private LinkedList<String> availHosts;
	
	public HostQueueDepot() {
		hostQueueMap = new HashMap<String, LinkedList<URLNode>>();
		availHosts = new LinkedList<String>();
	}
	
	public void add(List<URLNode> nodesToAdd) {
		for (URLNode node : nodesToAdd) {
			String host;
			try {
				host = new URI(node.url).getHost();
			} catch (URISyntaxException e) {
				host = null;
			}
			if (host != null) {
				// If the host has not yet been encountered, create a new queue for it
				if (hostQueueMap.get(host) == null) {
					hostQueueMap.put(host, new LinkedList<URLNode>());
				}
				// Add the node under the host's queue and add the host to the available host
				hostQueueMap.get(host).add(node);
				availHosts.add(host);
			}
		}
	}
	
	public URLNode getNextURL(String host) {
		if (!queueIsEmpty(host) && hasQueue(host)) {
			return hostQueueMap.get(host).remove();
		}
		throw new NoSuchElementException();
	}
	
	public String getNewHost() {
		if (availHosts.isEmpty())
			throw new NoSuchElementException();
		return availHosts.remove();
	}
	
	public boolean hasQueue(String host) {
		if (hostQueueMap.get(host) != null)
			return true;
		return false;
	}
	
	public boolean hasAvailHosts() {
		if (availHosts.isEmpty())
			return false;
		return true;
	}
	
	public boolean queueIsEmpty(String host) {
		if (hasQueue(host))
			return hostQueueMap.get(host).isEmpty();
		return true;
	}
	
}
