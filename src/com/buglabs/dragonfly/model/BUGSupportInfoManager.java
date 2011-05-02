package com.buglabs.dragonfly.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.buglabs.dragonfly.util.BugWSHelper;
import com.buglabs.util.XmlNode;

/**
 * This class helps manage the SDK's view of the BUG support info The support
 * information is used to determine the compatibility between a BUG and the BUG
 * Apps. Currently, this class is used when attempting to upload a BUG app to a
 * BUG.
 * 
 * @author bballantine
 * 
 */
public class BUGSupportInfoManager {

	/**
	 * Tells us that BUG is 1.2 or 1.3. This is important because in 1.4.x and
	 * on, a different method of uploading apps is used.
	 */
	public static final String BUG_VERSION_PRE_R14 = "Pre-1.4";
	// Currently don't need these to be public, but they could be made public if needed
	// if BUG is 1.4.3 and above, it info will have the exact version (gets it from /support)
	private static final String BUG_VERSION_R14 = "R1.4";
	private static final String BUG_VERSION_NOT_KNOWN = "UNKNOWN";
	private static final String VIRTUAL_BUG = "VIRTUAL BUG";

	/**
	 * Concierge reports JRE-1.1 as it's execution environment when running
	 * PhoneME.
	 */
	public static final String PHONEME_EXECUTION_ENV = "JRE-1.1";
	public static final String DEFAULT_EXECUTION_ENV = "JRE-1.6";

	// keys used for parsing xml
	private static final String ROOTFS_VERSION_NODE_NAME = "rootfs_version";
	private static final String JVM_PROPERTIES_NODE_NAME = "jvm_properties";
	private static final String JVM_PROPERTY_KEY = "key";
	private static final String JVM_PROPERTY_VALUE = "value";
	private static final Object JVM_EXECUTION_ENVIRONMENT_KEY = "org.osgi.framework.executionenvironment";
	private static final String BUNDLES_NODE_NAME = "bundle_versions";
	private static final String BUNDLE_NAME_KEY = "name";

	private String version = null;
	private String execution_environment = null;
	private List<String> bundle_list = new ArrayList<String>();

	/**
	 * What rootfs version? : 1.4.x (1.4.3 and beyond) : R1.4 (1.4.1, 1.4.2) :
	 * Pre-1.4 (1.2, 1.3)
	 * 
	 * @return
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * set the version, see getVersion()
	 * 
	 * @param version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * OSGi Execution Environment We mostly care if it's J2SE-1.1 (PhoneME) or
	 * Java2SE-1.6 (OpenJDK 6)
	 * 
	 * @return
	 */
	public String getExecutionEnvironment() {
		return execution_environment;
	}

	/**
	 * Set EE, see getExecutionEnvironment()
	 * 
	 * @param executionEnvironment
	 */
	public void setExecutionEnvironment(String executionEnvironment) {
		execution_environment = executionEnvironment;
	}

	/**
	 * See getBundleList
	 * 
	 * @param bundles
	 */
	public void setBundleList(List<String> bundles) {
		bundle_list = bundles;
	}

	/**
	 * List of bundles on bug (includes system and application)
	 * 
	 * @return
	 */
	public List<String> getBundleList() {
		return bundle_list;
	}

	/**
	 * Static method to create a new BUGSupportInfoManager from a connected BUG
	 * 
	 * @param bug
	 * @return
	 */
	public static BUGSupportInfoManager load(Bug bug) {
		BUGSupportInfoManager info = null;
		try {
			info = parseInfoXml(BugWSHelper.getBUGSupportInfo(bug.getSupportURL()));
		} catch (IOException e) {
			info = null;
		}

		if (info == null)
			info = getSupportInfoForOldBUG(bug);

		return info;
	}

	/**
	 * Helper, parse the support info xml
	 * 
	 * @param infoRoot
	 * @return
	 */
	private static BUGSupportInfoManager parseInfoXml(XmlNode infoRoot) {
		BUGSupportInfoManager info = new BUGSupportInfoManager();

		// Get rootfs version
		XmlNode versionNode = infoRoot.getChild(ROOTFS_VERSION_NODE_NAME);
		info.setVersion(BUGSupportInfoManager.parseRootfsVersionText(versionNode.getValue()));
		if (info.getVersion() == null)
			info.setVersion(BUG_VERSION_NOT_KNOWN);

		// Get ExecutionEnvironment String
		XmlNode propertiesNode = infoRoot.getChild(JVM_PROPERTIES_NODE_NAME);
		XmlNode node;
		for (Iterator<XmlNode> itr = propertiesNode.getChildren().iterator(); itr.hasNext();) {
			node = (XmlNode) itr.next();
			if (node.getAttribute(JVM_PROPERTY_KEY) != null && node.getAttribute(JVM_PROPERTY_KEY).equals(JVM_EXECUTION_ENVIRONMENT_KEY)) {
				info.setExecutionEnvironment(node.getAttribute(JVM_PROPERTY_VALUE));
			}
		}

		if (info.getExecutionEnvironment() == null)
			info.setExecutionEnvironment(DEFAULT_EXECUTION_ENV);

		// Get Bundle List
		List<String> bundles = new ArrayList<String>();
		XmlNode bundlesNode = infoRoot.getChild(BUNDLES_NODE_NAME);
		for (Iterator<XmlNode> itr = bundlesNode.getChildren().iterator(); itr.hasNext();) {
			node = itr.next();
			if (node.getAttribute(BUNDLE_NAME_KEY) != null)
				bundles.add(node.getAttribute(BUNDLE_NAME_KEY));
		}
		info.setBundleList(bundles);

		return info;
	}

	/**
	 * Older Bugs don't have support xml, so we try to get the data a different
	 * way
	 * 
	 * @param bug
	 * @return
	 */
	private static BUGSupportInfoManager getSupportInfoForOldBUG(Bug bug) {
		BUGSupportInfoManager info = new BUGSupportInfoManager();
		info.setExecutionEnvironment(PHONEME_EXECUTION_ENV);

		List<ProgramNode> pkgs = null;
		try {
			pkgs = BugWSHelper.getPrograms(bug.getProgramURL());
		} catch (Exception e) {
			pkgs = null;
		}
		// probably unable to connect
		if (pkgs == null)
			return null;

		IPackage pkg;
		for (Iterator<ProgramNode> i = pkgs.iterator(); i.hasNext();) {
			pkg = i.next().getPackage();
			//Somewhat of a hack.  If we find the BUG Audio bundle, we assume it's R1.4
			if (pkg.getName().toUpperCase().equals("BUG AUDIO")) {
				info.setVersion(BUG_VERSION_R14);
			}
		}

		if (info.getVersion() == null)
			info.setVersion(BUG_VERSION_PRE_R14);

		return info;
	}

	/**
	 * Helper to parse the rootfs version text which is /etc/buildinfo on the
	 * bug
	 * 
	 * @param versionText
	 * @return
	 */
	private static String parseRootfsVersionText(String versionText) {
		if (versionText.toLowerCase().contains("virtual bug"))
			return VIRTUAL_BUG;

		String[] lines = versionText.split("\n");
		String version = null;
		for (String line : lines) {
			if (line.startsWith("Version:")) {
				version = line.trim();
				break;
			}
		}
		if (version == null)
			return null;

		Pattern p = Pattern.compile("Version:\\s*(\\S+)");
		Matcher m = p.matcher(version);
		m.find();
		return m.group(1);
	}
}
