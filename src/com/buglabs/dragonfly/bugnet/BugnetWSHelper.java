/*******************************************************************************
 * Copyright (c) 2006, 2007, 2008 Bug Labs, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.buglabs.net/legal/epl_license.html
 *******************************************************************************/
package com.buglabs.dragonfly.bugnet;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

import com.buglabs.dragonfly.exception.BugnetAuthenticationException;
import com.buglabs.dragonfly.exception.BugnetException;
import com.buglabs.dragonfly.model.AuthenticationData;
import com.buglabs.dragonfly.model.BUGNetProgramReferenceNode;
import com.buglabs.dragonfly.rest.HTTPException;
import com.buglabs.dragonfly.rest.HTTPRequest;
import com.buglabs.dragonfly.rest.HTTPResponse;
import com.buglabs.dragonfly.util.UIUtils;
import com.buglabs.util.XmlNode;
import com.buglabs.util.XmlParser;
import com.buglabs.util.XpathQuery;

/**
 * 
 * 
 * @author brian
 *
 */
public class BugnetWSHelper {
	
	private static final String BUGNET_VERSION_STRING 				= "2";
	private static final String BUGNET_WS_PATH_STRING				= "v" + BUGNET_VERSION_STRING + "/";
	private static final String BUGNET_PROGRAM_STRING 				= "programs";
	private static final String BUGNET_TOKEN_STRING 				= "token";
	private static final String BUGNET_USERS_STRING					= "users";
	private static final String BUGNET_VERIFY_STRING				= "verify";
	private static final String BUGNET_PACKAGES_PARAMS_STRING 		= "packages=";
	private static final String BUGNET_COUNT_PARAM_STRING 			= "count=";
	private static final String BUGNET_TITLE_PARAM_STRING			= "title=";
	private static final String BUGNET_SORT_BY_RATING_PARAM_STRING 	= "sort=rating";
	private static final String BUGNET_SORT_BY_UPDATED_PARAM_STRING 	= "sort=date";
	private static final String BUGNET_UPDATED_SORT_ORDER_PARAM_STRING 	= "sort_order=desc";
	
	
	/**
	 * Get a program (jar file) from BUGnet and return as an InputStream
	 * 
	 * @deprecated
	 * @param username String name of owner of program
	 * @param project  String name of the project to download
	 * @return         InputStream representing program jar
	 */
	public static InputStream getProgram(String username, String project) 
			throws BugnetAuthenticationException, BugnetException, IOException {
		return getProgram(project);
	}

	/**
	 * Get a program (jar file) from BUGnet and return as an InputStream
	 * 
	 * @param username String name of owner of program
	 * @return         InputStream representing program jar
	 * @throws BugnetException 
	 * @throws BugnetAuthenticationException 
	 */
	public static InputStream getProgram(String project) 
			throws BugnetAuthenticationException, BugnetException, IOException {
		InputStream isout = null;
		try {
			String urlStr = getBugNetBaseURL() + BUGNET_WS_PATH_STRING  
				+ BUGNET_PROGRAM_STRING + "/" + URLEncoder.encode(project, "UTF-8");
			HTTPRequest request = new HTTPRequest(new BugnetConnectionProvider());
			HTTPResponse response = request.get(urlStr);
			isout = response.getStream();
		} catch (HTTPException e) {
			handleBugnetException(e);
		}
		return isout;
	}	
	
	/**
	 * Add a program.  This is deprecated to not require AuthenticationData
	 * 
	 * @deprecated
	 * @param jarfile
	 * @param name
	 * @param not_used
	 * @throws TokenInvalidError 
	 */
	public static void addProgram(File jarfile, String name, AuthenticationData not_used) 
			throws BugnetAuthenticationException, BugnetException, IOException {
		addProgram(jarfile, name);
	}
	
	/**
	 * Add a program to BUGnet
	 *  first checks if it exists via HEAD
	 *  if it exists, attempts a PUT to update program
	 *  if it doesn't, attempts a post
	 * 
	 * @param jarfile
	 * @param name
	 * @throws IOException 
	 * @throws TokenInvalidError 
	 */
	public static String addProgram(File jarfile, String name) 
			throws BugnetAuthenticationException, BugnetException, IOException {
		HTTPRequest request = new HTTPRequest(new BugnetConnectionProvider());
		String urlStr = getBugNetBaseURL() + BUGNET_WS_PATH_STRING  + BUGNET_PROGRAM_STRING;
		String title = URLEncoder.encode(name, "UTF-8");
		
		// Do a HEAD to find out if app exists
		// this tell us weather to do a POST or PUT
		HTTPResponse response = null;
		int status = HTTPResponse.HTTP_CODE_OK;
		try {
			// do a HEAD to see if it exists
			response = request.head(urlStr + "/" + title);
		} catch (HTTPException headErr) {
			status = headErr.getErrorCode();
		}
		
		// If NOT FOUND, do a post, otherwise try to update with a put
		String location = null;
		try {
			if (status == HTTPResponse.HTTP_CODE_NOT_FOUND) {
				response = request.post(urlStr + "?" + BUGNET_TITLE_PARAM_STRING + title, 
						new FileInputStream(jarfile));
				location = response.getHeaderField("Location");
			} else {
				response = request.put(urlStr + "/" + title, new FileInputStream(jarfile));
				location = response.getHeaderField("Location");
			}
		} catch (HTTPException e) {
			handleBugnetException(e);
		}
		return location;
	}

	/**
	 * Get a token based on username and password.
	 *  This method is depricated -- the password isn't even used here
	 *  because it should be stored in the AuthenticationData
	 *  which is set with the login prompt
	 *  
	 *  Call getToken() instead
	 * 
	 * @deprecated
	 * @param username
	 * @param not_used_was_pwd
	 * @return a String token
	 * @throws IOException
	 * @throws TokenInvalidError 
	 */
	public static String getToken(String username, String not_used_was_pwd) 
			throws BugnetAuthenticationException, BugnetException, IOException {
		return getToken(username);
	}
	
	/**
	 * Use this one if you want a token as the proper thing to do is
	 *  get the token for the current logged in user, which is persisted
	 *  in AuthenticationData
	 * 
	 * @return
	 * @throws BugnetAuthenticationException
	 * @throws BugnetException
	 * @throws IOException
	 */
	public static String getToken()
		throws BugnetAuthenticationException, BugnetException, IOException {
		// get current user's info
		AuthenticationData authentication_data = 
			BugnetStateProvider.getInstance().getAuthenticationData();
		String username = authentication_data.getUsername();
		if (username != null && username.length() > 0) {
			return getToken(username);
		} else {
			return null;
		}
	}
	
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public static boolean verifyCurrentUser() throws IOException {
		String url = getBugNetBaseURL() + BUGNET_WS_PATH_STRING 
			+ BUGNET_USERS_STRING + "/" + BUGNET_VERIFY_STRING;
		HTTPRequest request = new HTTPRequest(new BugnetConnectionProvider());
		int response_code = HTTPResponse.HTTP_CODE_NOT_AUTHORIZED;
		try {
			HTTPResponse response = request.get(url);
			response_code = response.getResponseCode();
		} catch (HTTPException e) {/* do nothing, check response_code later*/}

		if (response_code == HTTPResponse.HTTP_CODE_OK) return true;
		else return false;
	}
	
	/**
	 * Get programs list by querystring
	 * 
	 * @param querystring
	 * @return
	 * @throws BugnetAuthenticationException
	 * @throws BugnetException
	 * @throws IOException
	 */
	public static List getProgramsByQuerystring(String querystring) 
			throws BugnetAuthenticationException, BugnetException, IOException {
		String url = getBugNetBaseURL() + BUGNET_WS_PATH_STRING 
			+ BUGNET_PROGRAM_STRING + "?" + querystring;
		return getProgramList(url);
	}
	
	/**
	 * Get a default number of user apps
	 * 
	 * @param user
	 * @return
	 * @throws IOException
	 */
	public static List getUserApps(String user)
			throws BugnetAuthenticationException, BugnetException, IOException {
		int count = BugnetStateProvider.getInstance().getDefaultApplicationCount();
		return getUserApps(user, count);
	}

	/**
	 * @param user
	 *            username
	 * @param count
	 *            number of application to return
	 * @return Returns applications for current user
	 * @throws IOException
	 */
	public static List getUserApps(String user, int count)
			throws BugnetAuthenticationException, BugnetException, IOException {
		List apps = new ArrayList();
		if (!user.trim().equals("")) {
			// /users/' + testface.login + '/programs
			String url = getBugNetBaseURL() + BUGNET_WS_PATH_STRING  + BUGNET_USERS_STRING + "/" 
				+ user + "/" + BUGNET_PROGRAM_STRING + "?" + BUGNET_COUNT_PARAM_STRING + count;
			apps = getProgramList(url);
		}
		return apps;
	}	

	/**
	 * Get top applications from BUGNet as program xml.
	 * 
	 * @return
	 * @throws IOException
	 */
	public static List getTopApps(int count)
	 		throws BugnetAuthenticationException, BugnetException, IOException {
		// http://ticker/ws/1/program?count=25&sort=rating
		String url = getBugNetBaseURL() + BUGNET_WS_PATH_STRING  + BUGNET_PROGRAM_STRING 
			+ "?" + BUGNET_COUNT_PARAM_STRING + count
			+ "&" + BUGNET_SORT_BY_RATING_PARAM_STRING;
		return getProgramList(url);
	}	
	
	/**
	 * Get top applications from BUGNet as program xml.
	 * 
	 * @return
	 * @throws IOException
	 */
	public static List getLatestApps(int count)
	 		throws BugnetAuthenticationException, BugnetException, IOException {
		// http://ticker/ws/1/program?count=25
		String url = getBugNetBaseURL() + BUGNET_WS_PATH_STRING  + BUGNET_PROGRAM_STRING 
			+ "?" + BUGNET_COUNT_PARAM_STRING + count
			+ "&" + BUGNET_SORT_BY_UPDATED_PARAM_STRING
			+ "&" + BUGNET_UPDATED_SORT_ORDER_PARAM_STRING;
		return getProgramList(url);
	}	
		
	
	/**
	 * Given a list of package names returns XML representation of the programs in the form of 
	 * a {@link List} of {@link BUGNetProgramReferenceNode}s
	 * 
	 * @param packageList List of package names
	 * @return {@link List} of {@link BUGNetProgramReferenceNode}s
	 * @throws IOException
	 */
	public static List getProgramsForPackages(List packageList)
	 		throws BugnetAuthenticationException, BugnetException, IOException {
		String packages = "";
		
		for (Iterator i = packageList.iterator(); i.hasNext();) {
			packages += ((String)i.next()).trim() + ",";
		}
		packages = packages.substring(0, packages.lastIndexOf(","));
		String url = getBugNetBaseURL() + BUGNET_WS_PATH_STRING + BUGNET_PROGRAM_STRING
			+ "?" + BUGNET_PACKAGES_PARAMS_STRING + packages;
		return getProgramList(url);
	}	
	
	
	/**
	 * getBugNetBaseURL - this is here 'cause it's being used elsewhere
	 * 
	 * @return
	 */
	public static String getBugNetBaseURL() {
		String bugnet_url = BugnetStateProvider.getInstance().getBugnetURL();
		// make sure there's a slash at the end
		if (!bugnet_url.endsWith("/")) bugnet_url += "/";
		return bugnet_url;
	}
	
	///////////////////////////////////////////////////////////////// USEFUL

	/**
	 * Take in an HTTPException and throw BugnetException
	 * 
	 * @param e
	 * @throws BugnetException
	 * @throws BugnetAuthenticationException
	 */
	private static void handleBugnetException(HTTPException e) 
			throws BugnetAuthenticationException, BugnetException {
		if (e.getErrorCode() == HTTPResponse.HTTP_CODE_NOT_AUTHORIZED)
			throw new BugnetAuthenticationException(parseErrorXml(e.getMessage()));
		else
			throw new BugnetException(parseErrorXml(e.getMessage()));
	}	
	
	/**
	 * 
	 * Private to keep you from using it.  Get's called from getToken()
	 * Shouldn't use it because you should only be getting your own token
	 * Which means that the username should be the same as the username/password
	 * stored in AuthenticationData.
	 * 
	 * @param username
	 * @return
	 * @throws BugnetAuthenticationException
	 * @throws BugnetException
	 * @throws IOException
	 */
	private static String getToken(String username)
			throws BugnetAuthenticationException, BugnetException, IOException {
		String url = getBugNetBaseURL() + BUGNET_WS_PATH_STRING  + BUGNET_USERS_STRING 
			+ "/" + username + "/" + BUGNET_TOKEN_STRING;
		HTTPRequest request = new HTTPRequest(new BugnetConnectionProvider());
		String token = "";
		try {
			HTTPResponse response = request.get(url);
			String res = response.getString();
			XmlNode e = XmlParser.parse(res);
			token = e.getValue();
		} catch (HTTPException e) {
			handleBugnetException(e);
		}
		return token;		
		
	}
	
	/**
	 * Gets a list of programs that satisfy the request represented by url
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private static List getProgramList(String url)
	 		throws BugnetAuthenticationException, BugnetException, IOException {
		List apps = new ArrayList();
		try {
			HTTPRequest request = new HTTPRequest(new BugnetConnectionProvider());
			HTTPResponse response = request.get(url);
			XmlNode root = XmlParser.parse(response.getString());
			apps = getProgramReferenceNodes(root);
		} catch (HTTPException e) {
			handleBugnetException(e);
		}
		return apps;
	}
	
	/**
	 * helper code copied from old BugnetWSHelper to create List of programs from xml
	 * 
	 * @param root
	 * @return
	 * @throws IOException
	 */
	private static List getProgramReferenceNodes(XmlNode root) throws IOException {
		List prgList = XpathQuery.getNodes("/programs/program", root);
		List apps = new ArrayList();
		for (Iterator i = prgList.iterator(); i.hasNext();) {
			XmlNode n = (XmlNode) i.next();
			String name = XpathQuery.getNode("/program/title", n).getValue();
			String username = XpathQuery.getNode("/program/username/", n).getValue();
			//String id = XpathQuery.getNode("/program", n).getAttribute("id");
			String webid = XpathQuery.getNode("/program", n).getAttribute("web_id");
			String bugNetUrl = XpathQuery.getNode("/program/homepage", n).getAttribute("url");
			String description = XpathQuery.getNode("/program/description/", n).getValue();
			String imageUrl = XpathQuery.getNode("/program/thumbnail", n).getAttribute("url");
			String imageKey;

			try {
				imageKey = getImageThumbnail(imageUrl);
			} catch (Exception e) {
				imageKey = null;
			}

			String download_count = XpathQuery.getNode("/program/download_count", n).getValue();
			String rating = XpathQuery.getNode("/program/rating", n).getValue();
			if (name != null && webid != null && bugNetUrl != null) {

				BUGNetProgramReferenceNode node = 
					new BUGNetProgramReferenceNode(webid, name, username, bugNetUrl, 
						description, imageKey, download_count, rating);

				apps.add(node);
			}
		}
		return apps;
	}
	
	
	/**
	 * Helper code from old BugnetWSHelper, modified to NOT use WSHelper for getImage
	 * 
	 * @param location
	 * @return
	 * @throws IOException
	 */
	private static String getImageThumbnail(String location) throws IOException, HTTPException {

		Image img = BugnetStateProvider.getInstance().getImageFromRegistry(location);

		if (img == null) {
			try {
				HTTPRequest request = new HTTPRequest(new BugnetConnectionProvider());
				HTTPResponse response = request.get(location);
				ImageData imagedata = response.getImage();
				img = new Image(null, imagedata);
				if (BugnetStateProvider.getInstance().getImageFromRegistry(location) == null) {
					BugnetStateProvider.getInstance().putImageInRegistry(location, img);
				}

			} catch (SWTException e) {
				UIUtils.handleNonvisualError("Error retrieving image from BUGnet. Image URL: " + location, e);
			}
		}

		return location;
	}
	
	
	/**
	 * pulls message out of error xml
	 * 
	 * @param errorXml
	 * @return
	 */
	private static String parseErrorXml(String errorXml) {
		String error = errorXml;
		try {
			XmlNode e = XmlParser.parse(errorXml);
			error = e.getValue();
		} catch (IOException e) {
			UIUtils.handleNonvisualError("Error parsing xml", e);
		}
		return error;
	}
}

