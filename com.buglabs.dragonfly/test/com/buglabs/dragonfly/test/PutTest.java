package com.buglabs.dragonfly.test;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.buglabs.util.xml.XmlNode;

public class PutTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testPut() throws HttpException, IOException {
		HttpClient c = new HttpClient();

		XmlNode root = new XmlNode("events");
		XmlNode n1 = new XmlNode(root, "event");
		n1.addAttribute("subscriber", "http://perunga:8082/tester/kenken");
		n1.addAttribute("topic", "com/buglabs/event/module");

		PutMethod m = new PutMethod("http://localhost:8082/events");

		m.setRequestEntity(new StringRequestEntity(root.toString()));

		c.executeMethod(m);

		System.out.println(m.getResponseBodyAsString());

	}
}
