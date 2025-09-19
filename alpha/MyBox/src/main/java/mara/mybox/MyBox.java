package mara.mybox;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import mara.mybox.dev.BaseMacro;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.CertificateTools;
import mara.mybox.tools.ConfigTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2019-1-22 14:35:50
 * @License Apache License Version 2.0
 */
public class MyBox {

    public static String InternalRestartFlag = "MyBoxInternalRestarting";

    // To pass arguments to JavaFx GUI
    // https://stackoverflow.com/questions/33549820/javafx-not-calling-mainstring-args-method/33549932#33549932
    public static void main(String[] args) {
        if (args == null) {
            AppVariables.AppArgs = null;
            AppVariables.appMacro = null;
        } else {
            AppVariables.AppArgs = new String[args.length];
            System.arraycopy(args, 0, AppVariables.AppArgs, 0, args.length);
            AppVariables.appMacro = BaseMacro.create(AppVariables.AppArgs);
        }

        initBaseValues();

        if (AppVariables.appMacro != null) {
            runMacro();
        } else {
            launchApp();
        }
    }

    public static boolean initBaseValues() {
        MyBoxLog.console("Checking configuration parameters...");
        if (AppVariables.AppArgs != null) {

            for (String arg : AppVariables.AppArgs) {
                if (arg.startsWith("config=")) {
                    String config = arg.substring("config=".length());
                    File configFile = new File(config);
                    String dataPath = ConfigTools.readValue(configFile, "MyBoxDataPath");
                    if (dataPath != null) {
                        try {
                            File dataPathFile = new File(dataPath);
                            if (!dataPathFile.exists()) {
                                dataPathFile.mkdirs();
                            } else if (!dataPathFile.isDirectory()) {
                                FileDeleteTools.delete(null, dataPathFile);
                                dataPathFile.mkdirs();
                            }
                            if (dataPathFile.exists() && dataPathFile.isDirectory()) {
                                AppVariables.MyboxConfigFile = configFile;
                                AppVariables.MyboxDataPath = dataPathFile.getAbsolutePath();
                                return true;
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
        AppVariables.MyboxConfigFile = ConfigTools.defaultConfigFile();
        MyBoxLog.console("MyBox Config file:" + AppVariables.MyboxConfigFile);
        String dataPath = ConfigTools.readValue("MyBoxDataPath");
        if (dataPath != null) {
            try {
                File dataPathFile = new File(dataPath);
                if (!dataPathFile.exists()) {
                    dataPathFile.mkdirs();
                } else if (!dataPathFile.isDirectory()) {
                    FileDeleteTools.delete(null, dataPathFile);
                    dataPathFile.mkdirs();
                }
                if (dataPathFile.exists() && dataPathFile.isDirectory()) {
                    AppVariables.MyboxDataPath = dataPathFile.getAbsolutePath();
                    MyBoxLog.console("MyBox Data Path:" + AppVariables.MyboxDataPath);
                    return true;
                }
            } catch (Exception e) {
            }
        }
        return true;
    }

    public static void runMacro() {
        MyBoxLog.console("Running Mybox Macro...");
        MyBoxLog.console("JVM path: " + System.getProperty("java.home"));
        AppVariables.appMacro.info();
        AppVariables.appMacro.run();
    }

    public static void launchApp() {
        MyBoxLog.console("Starting Mybox...");
        MyBoxLog.console("JVM path: " + System.getProperty("java.home"));

        if (AppVariables.MyboxDataPath != null && setJVMmemory() && !internalRestart()) {
            restart();

        } else {
            initEnv();
            Application.launch(MainApp.class, AppVariables.AppArgs);
        }
    }

    public static boolean internalRestart() {
        return AppVariables.AppArgs != null
                && AppVariables.AppArgs.length > 0
                && InternalRestartFlag.equals(AppVariables.AppArgs[0]);
    }

    public static boolean setJVMmemory() {
        String JVMmemory = ConfigTools.readValue("JVMmemory");
        if (JVMmemory == null) {
            return false;
        }
        long jvmM = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        if (JVMmemory.equals("-Xms" + jvmM + "m")) {
            return false;
        }
        if (AppVariables.AppArgs == null || AppVariables.AppArgs.length == 0) {
            return true;
        }
        for (String s : AppVariables.AppArgs) {
            if (s.startsWith("-Xms")) {
                return false;
            }
        }
        return true;
    }

    // Set properties before JavaFx starting to make sure they take effect against JavaFX
    public static void initEnv() {
        try {

            // https://pdfbox.apache.org/2.0/getting-started.html
//            System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
            System.setProperty("org.apache.pdfbox.rendering.UsePureJavaCMYKConversion", "true");

            // https://blog.csdn.net/iteye_3493/article/details/82060349
            // https://stackoverflow.com/questions/1004327/getting-rid-of-derby-log/1933310#1933310
            if (AppVariables.MyboxDataPath != null) {
                System.setProperty("javax.net.ssl.keyStore", CertificateTools.keystore());
                System.setProperty("javax.net.ssl.keyStorePassword", CertificateTools.keystorePassword());
                System.setProperty("javax.net.ssl.trustStore", CertificateTools.keystore());
                System.setProperty("javax.net.ssl.trustStorePassword", CertificateTools.keystorePassword());
                MyBoxLog.console(System.getProperty("javax.net.ssl.keyStore"));
            }
//            System.setProperty("derby.language.logQueryPlan", "true");

//            System.setProperty("jdk.tls.client.protocols", "TLSv1.1,TLSv1.2");
//            System.setProperty("jdk.tls.server.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3");
//            System.setProperty("https.protocol", "TLSv1");
//            System.setProperty("com.sun.security.enableAIAcaIssuers", "true");
//            System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
//            System.setProperty("javax.net.debug", "ssl,record, plaintext, handshake,session,trustmanager,sslctx");
//            System.setProperty("javax.net.debug", "ssl,handshake,session,trustmanager,sslctx");
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    // Restart with  parameters. Use "ProcessBuilder", instead of "Runtime.getRuntime().exec" which is not safe
    // https://stackoverflow.com/questions/4159802/how-can-i-restart-a-java-application?r=SearchResults
    public static void restart() {
        try {
            String javaHome = System.getProperty("java.home");
            File boundlesJar;
            if (SystemTools.isMac()) {
                boundlesJar = new File(javaHome.substring(0, javaHome.length() - "runtime/Contents/Home".length())
                        + "Java" + File.separator + "MyBox-" + AppValues.AppVersion + ".jar");
            } else {
                boundlesJar = new File(javaHome.substring(0, javaHome.length() - "runtime".length())
                        + "app" + File.separator + "MyBox-" + AppValues.AppVersion + ".jar");

            }
            if (boundlesJar.exists()) {
                restartBundles(boundlesJar);
            } else {
                restartJar();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void restartBundles(File jar) {
        try {
            MyBoxLog.console("Restarting Mybox bundles...");

            List<String> commands = new ArrayList<>();
            commands.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");

            String JVMmemory = ConfigTools.readValue("JVMmemory");
            if (JVMmemory != null) {
                commands.add(JVMmemory);
            }

            commands.add("-jar");
            commands.add(jar.getAbsolutePath());

            commands.add(InternalRestartFlag);
            if (AppVariables.AppArgs != null) {
                for (String arg : AppVariables.AppArgs) {
                    if (arg != null) {
                        commands.add(arg);
                    }
                }
            }

            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.start();

            System.exit(0);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void restartJar() {
        try {
            MyBoxLog.console("Restarting Mybox Jar package...");
            List<String> commands = new ArrayList<>();
            commands.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");

            List<String> jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
            for (String jvmArg : jvmArgs) {
                if (jvmArg != null) {
                    commands.add(jvmArg);
                }
            }

            commands.add("-cp");
            commands.add(ManagementFactory.getRuntimeMXBean().getClassPath());

            String JVMmemory = ConfigTools.readValue("JVMmemory");
            if (JVMmemory != null) {
                commands.add(JVMmemory);
            }

            commands.add(MyBox.class.getName());

            commands.add(InternalRestartFlag);
            if (AppVariables.AppArgs != null) {
                for (String arg : AppVariables.AppArgs) {
                    if (arg != null) {
                        commands.add(arg);
                    }
                }
            }

            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.start();

            System.exit(0);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
