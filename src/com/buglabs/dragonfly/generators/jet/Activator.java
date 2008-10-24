package com.buglabs.dragonfly.generators.jet;

public class Activator
 {
  protected static String nl;
  public static synchronized Activator create(String lineSeparator)
  {
    nl = lineSeparator;
    Activator result = new Activator();
    nl = null;
    return result;
  }

  public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = "/**" + NL + " * Generated by DragonFly." + NL + " *" + NL + " */" + NL + "package ";
  protected final String TEXT_2 = ";" + NL + "" + NL + "import ";
  protected final String TEXT_3 = ".*;" + NL + "import com.buglabs.util.ServiceFilterGenerator;" + NL + "import org.osgi.framework.BundleActivator;" + NL + "import org.osgi.framework.BundleContext;" + NL + "import org.osgi.framework.Filter;" + NL + "import org.osgi.util.tracker.ServiceTracker;" + NL + "" + NL + "/**" + NL + " * BundleActivator for ";
  protected final String TEXT_4 = "." + NL + " *" + NL + " */" + NL + "public class Activator implements BundleActivator {" + NL + "\tprivate ";
  protected final String TEXT_5 = "ServiceTracker stc;" + NL + "\tprivate ServiceTracker st;" + NL + "\t/*" + NL + "\t * (non-Javadoc)" + NL + "\t * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)" + NL + "\t */" + NL + "\tpublic void start(BundleContext context) throws Exception {\t\t" + NL + "\t\t//Create the service tracker and run it." + NL + "\t\tstc = new ";
  protected final String TEXT_6 = "ServiceTracker(context);" + NL + "\t\tFilter f = context.createFilter(ServiceFilterGenerator.generateServiceFilter(stc.getServices()));" + NL + "\t\tst = new ServiceTracker(context, f, stc);" + NL + "\t\tst.open();" + NL + "\t}" + NL + "" + NL + "\t/*" + NL + "\t * (non-Javadoc)" + NL + "\t * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)" + NL + "\t */" + NL + "\tpublic void stop(BundleContext context) throws Exception {" + NL + "\t\tstc.stop();" + NL + "\t\tst.close();" + NL + "\t}" + NL + "}";
  protected final String TEXT_7 = NL;

   public String generate(String appName, String packageName, String moduleTrackerPackageName)
  {
    final StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(TEXT_1);
    stringBuffer.append(packageName);
    stringBuffer.append(TEXT_2);
    stringBuffer.append(moduleTrackerPackageName);
    stringBuffer.append(TEXT_3);
    stringBuffer.append(appName);
    stringBuffer.append(TEXT_4);
    stringBuffer.append(appName);
    stringBuffer.append(TEXT_5);
    stringBuffer.append(appName);
    stringBuffer.append(TEXT_6);
    stringBuffer.append(TEXT_7);
    return stringBuffer.toString();
  }
}