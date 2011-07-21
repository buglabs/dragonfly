package com.buglabs.dragonfly.model;

import com.buglabs.util.XmlNode;

public class PutServiceNode extends ServiceNode {
	private static final long serialVersionUID = 8505755261050519825L;

	public PutServiceNode(XmlNode node, String url) {

		super(node.getParent().getAttribute("name") + " [PUT]", node.getParent().getAttribute("description"), url);
	}
}
