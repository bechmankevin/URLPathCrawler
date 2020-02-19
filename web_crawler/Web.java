package web_crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Object representing the results of a web crawl.
 * Contains methods for traversing or searching for the path to a specific URL.
 */
public class Web {
	private HashMap<String, Link> linkMap;
	public Web (HashMap<String, Link> linkMap) {
		this.linkMap = linkMap;
	}
	
	public void printMap() {
		System.out.println("Link Map: ");
		for (Iterator<Link> i = linkMap.values().iterator(); i.hasNext();) {
			System.out.println(i.next());
		}
	}
	
	public void printPathToStart(String url) {
		//ArrayList<Link> path = new ArrayList<Link>();
		Link target = linkMap.get(url);
		if (target == null) {
			System.out.println("URL Not found.");
			return;
		}
		//path.add(target);
		System.out.println(target);
		while (target.getParent() != null) {
			//path.add(target.getParent());
			System.out.println(target.getParent());
			target = target.getParent();
		}
	}
}
