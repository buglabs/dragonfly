package com.buglabs.dragonfly.bugnet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import com.buglabs.dragonfly.exception.BugnetAuthenticationException;
import com.buglabs.dragonfly.exception.BugnetException;


/**
 * Manages the results from BUGnet searches
 * keeps track of our current search term, count, and page number
 * Used by BugnetSearchSectionHelper
 * 
 * @author brian
 *
 */
public class BugnetResultManager {
	
	private String search;
	private int count;
	private int page;
	private List applications;
	
	private static BugnetResultManager _instance;
	
	public static BugnetResultManager getInstance() {
		if(_instance == null) {
			synchronized(BugnetResultManager.class) {
				if(_instance == null) {
					_instance = new BugnetResultManager();
				}
			}
		}
		return _instance;
	}
	
	private BugnetResultManager() {
		reset();
	}
	
	public void reset() {
		this.search = null;
		this.count = BugnetStateProvider.getInstance().getDefaultApplicationCount();
		this.page = 1;
		this.applications = null;		
	}
	
	public String getSearch() {
		return search;
	}
	public void setSearch(String search) {
		this.search = search;
	}
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}

	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}	
	
	private String toQueryString() {
		String querystring = "";
		if (search!=null) querystring = addValue(querystring, "search", search);
		if (count>0) querystring = addValue(querystring, "count", count + "");
		if (page>0) querystring = addValue(querystring, "current_page", page + "");
		return querystring;
	}
	
	private String addValue(String source, String key, String value) {
		if (value!=null && value.length() > 0) {
			try {
				value = URLEncoder.encode(value, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				value="";
			}
		}
		if (source != null && source.length() > 0) {
			source += "&" + key + "=" + value;
		} else {
			source = key + "=" + value;
		}
		return source;
	}
	
	public void doQuery() throws BugnetAuthenticationException, BugnetException, IOException {
		applications = BugnetWSHelper.getProgramsByQuerystring(toQueryString());
	}
	
	public List getApplications() {
		return applications;
	}
}
