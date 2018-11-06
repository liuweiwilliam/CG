
package com.CG;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/*
 * use this class to handle preset keywords
 */
public class CGSystem {
	private ArrayList<String> keys = new ArrayList<String>();
	private String currentTimeYMD() {
		return new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
	}
	private String currentTimeYMDHMS() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
	}
	
	// add preset keywords 
	CGSystem() {
		this.keys.add("sys_time_ymd");
		this.keys.add("sys_time_ymdhms");
	}
	
	// get a preset keyword's value
	private String get(String key) {
		if (key.equals("sys_time_ymd")) return this.currentTimeYMD();
		if (key.equals("sys_time_ymdhms")) return this.currentTimeYMDHMS();
		
		return "";
	}

	// get all preset keywords and corresponding values
	public ArrayList<CGPair> getAll() {
		ArrayList<CGPair> pairs = new ArrayList<CGPair>();
		
		for (String key: keys) {
			pairs.add(new CGPair(key, this.get(key)));
		}
		
		return pairs;
	}
}
