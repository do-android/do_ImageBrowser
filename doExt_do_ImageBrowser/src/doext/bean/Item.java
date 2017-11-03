package doext.bean;

import java.io.Serializable;

public class Item implements Serializable {
	private static final long serialVersionUID = 1L;
	public String source;
	public String init;
	public boolean isHttpUrl;

	public Item(String s, String i, boolean ihu) {
		this.source = s;
		this.init = i;
		this.isHttpUrl = ihu;
	}
}