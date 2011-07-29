package com.buglabs.dragonfly.generators.jet;

import java.util.*;
import com.buglabs.dragonfly.model.BugProjectInfo;

public class Application
 {
  protected static String nl;
  public static synchronized Application create(String lineSeparator)
  {
    nl = lineSeparator;
    Application result = new Application();
    nl = null;
    return result;
  }

  public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = "package ";
  protected final String TEXT_2 = ";" + NL + "" + NL + "import java.util.Map;" + NL;
  protected final String TEXT_3 = NL + "import ";
  protected final String TEXT_4 = ";";
  protected final String TEXT_5 = NL + NL + "import com.buglabs.util.osgi.ServiceTrackerUtil.ManagedRunnable;";
  protected final String TEXT_6 = NL + "import org.osgi.service.log.LogService;";
  protected final String TEXT_7 = NL + "/**" + NL + " * This class represents the running application when all service dependencies are fulfilled." + NL + " * " + NL + " * The run() method will be called with a map containing all the services specified in ServiceTrackerHelper.openServiceTracker()." + NL + " * The application will run in a separate thread than the caller of start() in the Activator.  See " + NL + " * ManagedInlineRunnable for a thread-less application." + NL + " * " + NL + " * By default, the application will only be started when all service dependencies are fulfilled.  For " + NL + " * finer grained service binding logic, see ServiceTrackerHelper.openServiceTracker(BundleContext context, String[] services, Filter filter, ServiceTrackerCustomizer customizer)" + NL + " */" + NL + "public class ";
  protected final String TEXT_8 = " implements ManagedRunnable {" + NL + "" + NL + "\t@Override" + NL + "\tpublic void run(Map<String, Object> services) {";
  protected final String TEXT_9 = "\t\t\t" + NL + "\t\t";
  protected final String TEXT_10 = " ";
  protected final String TEXT_11 = " = (";
  protected final String TEXT_12 = ") services.get(";
  protected final String TEXT_13 = ".class.getName());";
  protected final String TEXT_14 = NL + "\t\t// TODO Use services here.";
  protected final String TEXT_15 = NL + "\t\t" + NL + "\t\tActivator.getLogger().log(LogService.LOG_INFO, this.getClass().getName() + \" has started!\");";
  protected final String TEXT_16 = NL + "\t\tSystem.out.println(\"DEBUG (\" + this.getClass().getName() + \"): Application Run\");";
  protected final String TEXT_17 = NL + "\t}" + NL + "" + NL + "\t@Override" + NL + "\tpublic void shutdown() {" + NL + "\t\t// TODO Add shutdown code here if necessary.";
  protected final String TEXT_18 = NL + "\t\tSystem.out.println(\"DEBUG (\" + this.getClass().getName() + \"): Application Shutdown\");";
  protected final String TEXT_19 = NL + "\t}" + NL + "}";

   public String generate(String appName, String packageName, List services, BugProjectInfo projInfo)
  {
    final StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(TEXT_1);
    stringBuffer.append(packageName);
    stringBuffer.append(TEXT_2);
    
	Iterator siter = services.iterator();
	while(siter.hasNext()) {
		String sname = (String) siter.next();

    stringBuffer.append(TEXT_3);
    stringBuffer.append(sname);
    stringBuffer.append(TEXT_4);
    
	}

    stringBuffer.append(TEXT_5);
     if (projInfo.getGenerateLogMethod()) { 
    stringBuffer.append(TEXT_6);
     } 
    stringBuffer.append(TEXT_7);
    stringBuffer.append(appName);
    stringBuffer.append(TEXT_8);
    
	siter = services.iterator();
	while(siter.hasNext()) {
		String sname = (String) siter.next();
		String serviceQualified = new String(sname);
		
		int i = serviceQualified.lastIndexOf(".");
		String service = "";
		if(i < 0) {
			service = serviceQualified;
		} else {
			service = serviceQualified.substring(i+1, serviceQualified.length());
		}

    stringBuffer.append(TEXT_9);
    stringBuffer.append(service);
    stringBuffer.append(TEXT_10);
    stringBuffer.append( service.toLowerCase());
    stringBuffer.append(TEXT_11);
    stringBuffer.append(service );
    stringBuffer.append(TEXT_12);
    stringBuffer.append(service);
    stringBuffer.append(TEXT_13);
    
	}

    stringBuffer.append(TEXT_14);
     if (projInfo.getGenerateLogMethod()) { 
    stringBuffer.append(TEXT_15);
     } else if (projInfo.getGenerateDebugStatements()) { 
    stringBuffer.append(TEXT_16);
     } 
    stringBuffer.append(TEXT_17);
     if (projInfo.getGenerateDebugStatements()) { 
    stringBuffer.append(TEXT_18);
     } 
    stringBuffer.append(TEXT_19);
    return stringBuffer.toString();
  }
}