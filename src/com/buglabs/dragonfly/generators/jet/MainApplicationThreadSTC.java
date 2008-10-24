package com.buglabs.dragonfly.generators.jet;

import java.util.Iterator;
import java.util.List;

public class MainApplicationThreadSTC {
	protected static String nl;

	public static synchronized MainApplicationThreadSTC create(String lineSeparator) {
		nl = lineSeparator;
		MainApplicationThreadSTC result = new MainApplicationThreadSTC();
		nl = null;
		return result;
	}

	protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;

	protected final String TEXT_1 = "/**" + NL + " *\tGenerated by Dragonfly SDK" + NL + " *" + NL + " */" + NL + "package ";

	protected final String TEXT_2 = ";" + NL + "" + NL + "import org.osgi.framework.BundleContext;" + NL
			+ "import org.osgi.framework.ServiceReference;" + NL + "import org.osgi.framework.Constants;" + NL
			+ "import org.osgi.util.tracker.ServiceTrackerCustomizer;" + NL
			+ "import com.buglabs.application.MainApplicationServiceTracker;" + NL + "import java.util.*;";

	protected final String TEXT_3 = NL + "import ";

	protected final String TEXT_4 = ";";

	protected final String TEXT_5 = NL + "import ";

	protected final String TEXT_6 = ".*;" + NL + "" + NL + "/**" + NL + " *\tService tracker for the ";

	protected final String TEXT_7 = " Bundle;" + NL + " *" + NL + " */" + NL + "public class ";

	protected final String TEXT_8 = "ServiceTracker extends MainApplicationServiceTracker {" + NL + "" + NL + "\t";

	protected final String TEXT_9 = "Application application;" + NL + "\tBundleContext context;" + NL + "\tList services;" + NL + "\t" + NL
			+ "\t" + NL + "\tpublic ";

	protected final String TEXT_10 = "ServiceTracker(BundleContext context) {" + NL + "\t\tsuper(context, new ";

	protected final String TEXT_11 = "Application(this));" + NL + "\t}" + NL + "\t" + NL + "\t/**" + NL
			+ "     * Used by OSGi to signal that a service was added." + NL + "     *" + NL + "     */" + NL
			+ "\tpublic Object addingService(ServiceReference reference) {" + NL + "\t\tObject obj = context.getService(reference);\t\t"
			+ NL + "\t\tString[] objClassName = (String[]) reference.getProperty(Constants.OBJECTCLASS);" + NL
			+ "\t\tObject retval = null;" + NL + "\t\t" + NL + "\t\tif(!servicesMap.containsKey(objClassName[0])) {" + NL
			+ "\t\t\tservicesMap.put(objClassName[0], obj);" + NL + "\t\t\tretval = obj;" + NL + "\t\t" + NL + "\t\t\tif(canStart()) {"
			+ NL + "\t\t\t\ttry {" + NL + "\t\t\t\t\tif(!application.isAlive()) {" + NL + "\t\t\t\t\t\tapplication = new ";

	protected final String TEXT_12 = "Application(this);" + NL + "\t\t\t\t\t\tapplication.start();" + NL
			+ "\t\t\t\t\t\tappHasStarted = true;" + NL + "\t\t\t\t\t}" + NL + "\t\t\t\t} catch (Exception e) {" + NL
			+ "\t\t\t\t\te.printStackTrace();" + NL + "\t\t\t\t}" + NL + "\t\t\t}" + NL + "\t\t}" + NL + "\t\treturn retval;" + NL + "\t}"
			+ NL + "" + NL + "\tpublic void modifiedService(ServiceReference reference, Object service) {" + NL + "" + NL + "\t}" + NL + ""
			+ NL + "\t/**" + NL + "     * Used by OSGi to signal that a service was removed." + NL + "     *" + NL + "     */" + NL
			+ "\tpublic void removedService(ServiceReference reference, Object service) {" + NL
			+ "\t\tObject obj = context.getService(reference);" + NL + "\t\tboolean serviceRemoved = false;" + NL + "\t\t" + NL
			+ "\t\tString[] objClassName = (String[]) reference.getProperty(Constants.OBJECTCLASS);" + NL + "\t\t" + NL
			+ "\t\tif(servicesMap.get(objClassName[0]).equals(obj)) {" + NL + "\t\t\tservicesMap.remove(objClassName[0]);" + NL
			+ "\t\t\tserviceRemoved = true;" + NL + "\t\t}" + NL + "" + NL + "\t\tif(appHasStarted && serviceRemoved) {" + NL
			+ "\t\t\ttry {" + NL + "\t\t\t\tapplication.tearDown();" + NL + "\t\t\t} catch (Exception e) {" + NL
			+ "\t\t\t\t// TODO Auto-generated catch block" + NL + "\t\t\t\te.printStackTrace();" + NL + "\t\t\t}" + NL
			+ "\t\t\tappHasStarted = false;" + NL + "\t\t}" + NL + "\t}" + NL + "\t" + NL + "\t/**" + NL
			+ "     * Determines if the application can be started." + NL + "     * @returns true when the application is not running and"
			+ NL + "     * there's a handle for each service." + NL + "     */" + NL + "\tprotected boolean canStart() {" + NL
			+ "\t\tif(appHasStarted) {" + NL + "\t\t\treturn false;" + NL + "\t\t}" + NL + "\t\t" + NL
			+ "\t\tIterator servicesIter = services.iterator();" + NL + "\t\t" + NL + "\t\twhile(servicesIter.hasNext()) {" + NL
			+ "\t\t\tif(servicesMap.get((String) servicesIter.next()) == null) {" + NL + "\t\t\t\treturn false;" + NL + "\t\t\t}" + NL
			+ "\t\t}" + NL + "\t\t" + NL + "\t\treturn true;" + NL + "\t}" + NL + "\t" + NL + "\t/**" + NL
			+ "     * Returns the main application thread." + NL + "     */" + NL
			+ "\tpublic MainApplicationThread getMainApplicationThread() {" + NL + "\t\treturn application;" + NL + "\t}" + NL + "\t" + NL
			+ "\t/**" + NL + "     * Helper method to retrieve a service of type class." + NL + "     *" + NL + "     */" + NL
			+ "\tpublic Object getService(Class clazz) {" + NL + "\t\treturn servicesMap.get(clazz.getName());" + NL + "\t}" + NL + "\t"
			+ NL + "\t/**" + NL + "\t * Used to retrieve a list of qualified service names." + NL
			+ "\t * If you want your application to depend on another " + NL + "     * service, simply add the fully qualified name of the"
			+ NL + "     * service to this list." + NL + " \t *" + NL + "\t * @return a list of Strings containing the fully qualified"
			+ NL + "     *         name of each service." + NL + "\t *" + NL + "\t */" + NL + "\tpublic List getServices() {" + NL
			+ "\t\tif(services == null) {" + NL + "\t\t\tservices = new ArrayList();";

	protected final String TEXT_13 = NL + "\t\t\tservices.add(\"";

	protected final String TEXT_14 = "\");";

	protected final String TEXT_15 = NL + "\t\t} " + NL + "\t\t" + NL + "\t\treturn services;" + NL + "\t}" + NL + "}";

	protected final String TEXT_16 = NL;

	public String generate(List serviceNames, String appName, String packageName, String appPackageName) {
		final StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(TEXT_1);
		stringBuffer.append(packageName);
		stringBuffer.append(TEXT_2);

		Iterator serviceIterator = serviceNames.iterator();
		while (serviceIterator.hasNext()) {
			String serviceQualified = (String) serviceIterator.next();

			stringBuffer.append(TEXT_3);
			stringBuffer.append(serviceQualified);
			stringBuffer.append(TEXT_4);

		}// end of while(seriviceIterator.hasNext())

		stringBuffer.append(TEXT_5);
		stringBuffer.append(appPackageName);
		stringBuffer.append(TEXT_6);
		stringBuffer.append(appName);
		stringBuffer.append(TEXT_7);
		stringBuffer.append(appName);
		stringBuffer.append(TEXT_8);
		stringBuffer.append(appName);
		stringBuffer.append(TEXT_9);
		stringBuffer.append(appName);
		stringBuffer.append(TEXT_10);
		stringBuffer.append(appName);
		stringBuffer.append(TEXT_11);
		stringBuffer.append(appName);
		stringBuffer.append(TEXT_12);

		Iterator serviceIterator2 = serviceNames.iterator();
		while (serviceIterator2.hasNext()) {
			String serviceQualified = (String) serviceIterator2.next();

			stringBuffer.append(TEXT_13);
			stringBuffer.append(serviceQualified);
			stringBuffer.append(TEXT_14);

		}// end of while(seriviceIterator2.hasNext())

		stringBuffer.append(TEXT_15);
		stringBuffer.append(TEXT_16);
		return stringBuffer.toString();
	}
}