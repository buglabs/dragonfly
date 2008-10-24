package com.buglabs.dragonfly.generators.jet;

import java.util.Iterator;
import java.util.List;

public class Application {
	protected static String nl;

	public static synchronized Application create(String lineSeparator) {
		nl = lineSeparator;
		Application result = new Application();
		nl = null;
		return result;
	}

	protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;

	protected final String TEXT_1 = "package ";

	protected final String TEXT_2 = ";" + NL + "" + NL + "import com.buglabs.application.IServiceProvider;" + NL
			+ "import com.buglabs.application.MainApplicationThread;" + NL + "import java.util.*;";

	protected final String TEXT_3 = NL + "import ";

	protected final String TEXT_4 = ";";

	protected final String TEXT_5 = NL + NL + "/**" + NL + " * ";

	protected final String TEXT_6 = " Main application thread. The run method is invoked " + NL
			+ " * by the applications service tracker when all services are accounted for." + NL + " *" + NL + " */" + NL + "public class ";

	protected final String TEXT_7 = " extends MainApplicationThread {" + NL + "" + NL + "\tprivate IServiceProvider serviceProv;" + NL
			+ "\tprivate boolean ran;" + NL + "\t" + NL + "\tpublic ";

	protected final String TEXT_8 = "(IServiceProvider serviceProv) {" + NL + "\t\tthis.serviceProv = serviceProv;" + NL
			+ "\t\tran = false;" + NL + "\t}" + NL + "\t" + NL + "\t/**" + NL + "\t * Informs the caller whether this thread ran." + NL
			+ "n.\t */" + NL + "\tpublic boolean getRan() {" + NL + "\t\treturn ran;" + NL + "\t}" + NL + "\t" + NL
			+ "\tprivate void ran() {" + NL + "\t\tran = true;" + NL + "\t}" + NL + "\t" + NL + "\t/**" + NL
			+ "     * This method is invoked as a result of all services" + NL
			+ "     * becoming available for the application. The list of services is" + NL
			+ "     * obtained from the getServices() method." + NL + "     */" + NL + "\tpublic void run() {" + NL
			+ "\t\t//Main application loop the run method will commence" + NL + "\t\t//once all service dependencies are satisfied." + NL
			+ "\t\twhile(!tearDownRequested) {" + NL + "\t\t\tSystem.out.println(\"Running: ";

	protected final String TEXT_9 = "\");" + NL + "\t\t\t" + NL + "\t\t\ttry {" + NL + "\t\t\t\tsleep(1000);" + NL
			+ "\t\t\t} catch (InterruptedException e) {" + NL + "\t\t\t\t// TODO Auto-generated catch block" + NL
			+ "\t\t\t\te.printStackTrace();" + NL + "\t\t\t}" + NL + "\t\t}" + NL + "\t\t" + NL + "\t\tSystem.out.println(\"";

	protected final String TEXT_10 = " stopped\");\t" + NL + "\t\t" + NL + "\t\t/**" + NL + "\t\t * Let the service tracker know we ran."
			+ NL + "\t\t */" + NL + "\t\tran();" + NL + "\t}" + NL + "\t" + NL + "\t/**" + NL
			+ "     * Provides a list of service names that this application depends on." + NL + "     *" + NL + "     */" + NL
			+ "\tpublic List getServices() {" + NL + "\t\tList services = new ArrayList();";

	protected final String TEXT_11 = NL + "\t\tservices.add(\"";

	protected final String TEXT_12 = "\");";

	protected final String TEXT_13 = NL + "\t\treturn services;" + NL + "\t}";

	protected final String TEXT_14 = NL + NL + "\t/**" + NL + "\t * Queries the service provider for ";

	protected final String TEXT_15 = "." + NL + "\t *" + NL + "\t * @return a handle to the a(n) ";

	protected final String TEXT_16 = " service." + NL + "\t */" + NL + "\tprivate ";

	protected final String TEXT_17 = " get";

	protected final String TEXT_18 = "() {" + NL + "\t\treturn (";

	protected final String TEXT_19 = ") serviceProv.getService(";

	protected final String TEXT_20 = ".class);" + NL + "\t}";

	protected final String TEXT_21 = NL + "}";

	public String generate(String appName, String packageName, List services) {
		final StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(TEXT_1);
		stringBuffer.append(packageName);
		stringBuffer.append(TEXT_2);

		Iterator siter = services.iterator();
		while (siter.hasNext()) {
			String sname = (String) siter.next();

			stringBuffer.append(TEXT_3);
			stringBuffer.append(sname);
			stringBuffer.append(TEXT_4);

		}

		stringBuffer.append(TEXT_5);
		stringBuffer.append(appName);
		stringBuffer.append(TEXT_6);
		stringBuffer.append(appName);
		stringBuffer.append(TEXT_7);
		stringBuffer.append(appName);
		stringBuffer.append(TEXT_8);
		stringBuffer.append(appName);
		stringBuffer.append(TEXT_9);
		stringBuffer.append(appName);
		stringBuffer.append(TEXT_10);

		Iterator si = services.iterator();
		while (si.hasNext()) {
			String sq = (String) si.next();

			stringBuffer.append(TEXT_11);
			stringBuffer.append(sq);
			stringBuffer.append(TEXT_12);

		}

		stringBuffer.append(TEXT_13);

		Iterator serviceIter = services.iterator();
		while (serviceIter.hasNext()) {
			String serviceQualified = (String) serviceIter.next();

			int i = serviceQualified.lastIndexOf(".");
			String service = "";
			if (i < 0) {
				service = serviceQualified;
			} else {
				service = serviceQualified.substring(i + 1, serviceQualified.length());
			}

			stringBuffer.append(TEXT_14);
			stringBuffer.append(service);
			stringBuffer.append(TEXT_15);
			stringBuffer.append(service);
			stringBuffer.append(TEXT_16);
			stringBuffer.append(service);
			stringBuffer.append(TEXT_17);
			stringBuffer.append(service);
			stringBuffer.append(TEXT_18);
			stringBuffer.append(service);
			stringBuffer.append(TEXT_19);
			stringBuffer.append(service);
			stringBuffer.append(TEXT_20);

		}// end of while(serviceIter.hasNext()) {

		stringBuffer.append(TEXT_21);
		return stringBuffer.toString();
	}
}