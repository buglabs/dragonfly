package com.buglabs.osgi.concierge.templates;

import com.buglabs.dragonfly.model.BugProjectInfo;

 public class GeneratorActivator
 {
  protected static String nl;
  public static synchronized GeneratorActivator create(String lineSeparator)
  {
    nl = lineSeparator;
    GeneratorActivator result = new GeneratorActivator();
    nl = null;
    return result;
  }

  public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = "package ";
  protected final String TEXT_2 = ";";
  protected final String TEXT_3 = NL + NL + "import org.osgi.framework.BundleActivator;" + NL + "import org.osgi.framework.BundleContext;" + NL;
  protected final String TEXT_4 = NL + "import org.osgi.service.log.LogService;\t\t" + NL + "import com.buglabs.util.LogServiceUtil;";
  protected final String TEXT_5 = NL + NL + "public class ";
  protected final String TEXT_6 = " implements BundleActivator {";
  protected final String TEXT_7 = NL + "\tprivate static LogService logger = null;" + NL + "\t";
  protected final String TEXT_8 = NL + "\tpublic void start(BundleContext context) throws Exception {" + NL + "\t\t// TODO Auto-generated method stub";
  protected final String TEXT_9 = NL + "\t\tlogger = LogServiceUtil.getLogService(context);";
  protected final String TEXT_10 = NL + "\t\tSystem.out.println(\"DEBUG (\" + this.getClass().getName() + \"): Bundle Start\");";
  protected final String TEXT_11 = NL + "\t}" + NL + "" + NL + "\tpublic void stop(BundleContext context) throws Exception {" + NL + "\t\t// TODO Auto-generated method stub";
  protected final String TEXT_12 = NL + "\t\tSystem.out.println(\"DEBUG (\" + this.getClass().getName() + \"): Bundle Stop\");";
  protected final String TEXT_13 = NL + "\t}";
  protected final String TEXT_14 = NL + "    /**" + NL + "\t * @return an instance of the LogService." + NL + "\t */" + NL + "\tpublic static LogService getLogger() {" + NL + "\t\treturn logger;" + NL + "\t}";
  protected final String TEXT_15 = NL + "}";

   public String generate(BugProjectInfo projInfo)
  {
    final StringBuffer stringBuffer = new StringBuffer();
    
if(projInfo.getActivatorPackage().length() > 0) {

    stringBuffer.append(TEXT_1);
    stringBuffer.append(projInfo.getActivatorPackage());
    stringBuffer.append(TEXT_2);
    
}

    stringBuffer.append(TEXT_3);
     if (projInfo.getGenerateLogMethod()) { 
    stringBuffer.append(TEXT_4);
     } 
    stringBuffer.append(TEXT_5);
    stringBuffer.append(projInfo.getActivatorName());
    stringBuffer.append(TEXT_6);
     if (projInfo.getGenerateLogMethod()) { 
    stringBuffer.append(TEXT_7);
     } 
    stringBuffer.append(TEXT_8);
     if (projInfo.getGenerateLogMethod()) { 
    stringBuffer.append(TEXT_9);
     } 
     if (projInfo.getGenerateDebugStatements()) { 
    stringBuffer.append(TEXT_10);
     } 
    stringBuffer.append(TEXT_11);
     if (projInfo.getGenerateDebugStatements()) { 
    stringBuffer.append(TEXT_12);
     } 
    stringBuffer.append(TEXT_13);
     if (projInfo.getGenerateLogMethod()) { 
    stringBuffer.append(TEXT_14);
     } 
    stringBuffer.append(TEXT_15);
    return stringBuffer.toString();
  }
}
