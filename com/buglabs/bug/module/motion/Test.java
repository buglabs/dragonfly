package com.buglabs.bug.module.motion;

import java.io.IOException;
import java.io.InputStream;

public class Test {
	public static void main(String[] args) {
		InputStream is = MotionModlet.class.getResourceAsStream("data.log");
		int x;
	    try {
			while ((x = is.read())!= -1) {
			    System.out.print(x + "\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
