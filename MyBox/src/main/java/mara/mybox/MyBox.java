package mara.mybox;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import mara.mybox.tools.ConfigTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-1-22 14:35:50
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class MyBox {

    public static String InternalRestartFlag = "MyBoxInternalRestarting";

    // To pass arguments to JavaFx GUI
    // https://stackoverflow.com/questions/33549820/javafx-not-calling-mainstring-args-method/33549932#33549932
    public static void main(String[] args) {
        AppVariables.appArgs = args.clone();

        launchApp();
    }

    public static void launchApp() {
        logger.info("Starting Mybox...");

        if (!internalRestart() && (restoreJVMmemory() || isBundles())) {
            restart();

        } else {
            initEnv();
            Application.launch(MainApp.class, AppVariables.appArgs);
        }
    }

    public static boolean internalRestart() {
        return AppVariables.appArgs != null
                && AppVariables.appArgs.length > 0
                && InternalRestartFlag.equals(AppVariables.appArgs[0]);
    }

    public static boolean restoreJVMmemory() {
        String JVMmemory = ConfigTools.readConfigValue("JVMmemory");
        if (JVMmemory == null) {
            return false;
        }
        if (AppVariables.appArgs == null || AppVariables.appArgs.length == 0) {
            return true;
        }
        for (String s : AppVariables.appArgs) {
            if (s.startsWith("-Xms")) {
                return false;
            }
        }
        return true;
    }

    // All exe are missed in  jre path when jabapackager generates self-contain bundles.
    // Only one exception: "java.exe" is copied  to "bundles\MyBox\runtime\bin" of MyBox.exe by me.
    // https://stackoverflow.com/questions/16669122/include-java-exe-in-the-runtime-built
    public static boolean isBundles() {
        try {
            String javaHome = System.getProperty("java.home");
            File jar = new File(javaHome.substring(0, javaHome.length() - 7) + "app" + File.separator + "MyBox-" + CommonValues.AppVersion + ".jar");
            return jar.exists();
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    // Set properties before JavaFx starting to make sure they take effect against JavaFX
    public static void initEnv() {
        try {

            // https://pdfbox.apache.org/2.0/getting-started.html
//            System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
            System.setProperty("org.apache.pdfbox.rendering.UsePureJavaCMYKConversion", "true");

            // https://blog.csdn.net/weixin_42156742/article/details/81386226
//            System.setProperty("java.awt.headless", "false");
            // https://stackoverflow.com/questions/47613006/how-to-disable-scaling-the-ui-on-windows-for-java-9-applications?r=SearchResults
//            System.setProperty("sun.java2d.uiScale", "1.0");
            System.setProperty("prism.allowhidpi", "true".equals(ConfigTools.readConfigValue("DisableHidpi")) ? "false" : "true");

            String rootPath = ConfigTools.readConfigValue("MyBoxDataRoot");
            if (rootPath != null) {
                File path = new File(rootPath);
                if (path.exists() && path.isDirectory()) {
                    AppVariables.MyBoxDataRoot = path.getAbsolutePath();
                } else {
                    AppVariables.MyBoxDataRoot = CommonValues.DefaultDataRoot;
                }
            } else {
                AppVariables.MyBoxDataRoot = CommonValues.DefaultDataRoot;
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    // Restart with  parameters. Use "ProcessBuilder", instead of "Runtime.getRuntime().exec" which is not safe
    // https://stackoverflow.com/questions/4159802/how-can-i-restart-a-java-application?r=SearchResults
    public static void restart() {
        try {
            String javaHome = System.getProperty("java.home");
            File exeJar = new File(javaHome.substring(0, javaHome.length() - 7) + "app" + File.separator + "MyBox-" + CommonValues.AppVersion + ".jar");

            if (exeJar.exists()) {
                restartExe(exeJar);
            } else {
                restartJar();
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void restartExe(File jar) {
        try {
            logger.info("Restarting Mybox.exe...");

            List<String> commands = new ArrayList<>();
            commands.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");

            String JVMmemory = ConfigTools.readConfigValue("JVMmemory");
            if (JVMmemory != null) {
                commands.add(JVMmemory);
            }

            commands.add("-jar");
            commands.add(jar.getAbsolutePath());

            commands.add(InternalRestartFlag);
            if (AppVariables.appArgs != null) {
                for (String arg : AppVariables.appArgs) {
                    if (arg != null) {
                        commands.add(arg);
                    }
                }
            }

            ProcessBuilder pb = new ProcessBuilder(commands);
            Map<String, String> env = pb.environment();
            env.clear();                // Bypass env of "MyBox.exe",  to take pure env from java.exe
            pb.start();

            System.exit(0);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void restartJar() {
        try {
            logger.info("Restarting Mybox Jar package...");
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

            String JVMmemory = ConfigTools.readConfigValue("JVMmemory");
            if (JVMmemory != null) {
                commands.add(JVMmemory);
            }

            commands.add(MyBox.class.getName());

            commands.add(InternalRestartFlag);
            if (AppVariables.appArgs != null) {
                for (String arg : AppVariables.appArgs) {
                    if (arg != null) {
                        commands.add(arg);
                    }
                }
            }

            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.start();

            System.exit(0);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
