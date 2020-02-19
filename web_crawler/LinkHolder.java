package web_crawler;

public class LinkHolder {
	public final String path;
	public final int depth;
	public LinkHolder(String path, int depth) {
		this.path = path;
		this.depth = depth;
	}
	
	@Override
	public String toString() {
		return depth + " " + path;
	}
}
