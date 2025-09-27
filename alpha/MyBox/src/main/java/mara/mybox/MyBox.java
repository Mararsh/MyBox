package mara.mybox;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javax.imageio.ImageIO;
import mara.mybox.controller.MyBoxLoadingController;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.migration.DataMigration;
import mara.mybox.dev.BaseMacro;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.image.data.ImageColorSpace;
import mara.mybox.tools.CertificateTools;
import mara.mybox.tools.ConfigTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

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
        } else {
            AppVariables.AppArgs = new String[args.length];
            System.arraycopy(args, 0, AppVariables.AppArgs, 0, args.length);
        }

        initConfigValues();

        BaseMacro macro = BaseMacro.create(AppVariables.AppArgs);
        if (macro != null) {
            runMacro(macro);
        } else {
            launchApp();
        }
    }

    public static boolean initConfigValues() {
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

    public static void runMacro(BaseMacro macro) {
        if (macro == null) {
            return;
        }
        MyBoxLog.console("Running Mybox Macro...");
        MyBoxLog.console("JVM path: " + System.getProperty("java.home"));
        setSystemProperty();
        initEnv(null, Languages.embedLangName());
        macro.info();
        if (macro.readParameters()) {
            macro.run();
            macro.displayEnd();
            if (macro.isOpenResult()) {
                File file = macro.getOutputFile();
                if (file != null && file.exists()) {
                    AppVariables.AppArgs = new String[1];
                    AppVariables.AppArgs[0] = file.getAbsolutePath();
                    launchApp();
                    return;
                }
            }
        }
        WindowTools.doExit();
    }

    public static void launchApp() {
        MyBoxLog.console("Starting Mybox...");
        MyBoxLog.console("JVM path: " + System.getProperty("java.home"));

        if (AppVariables.MyboxDataPath != null && setJVMmemory() && !internalRestart()) {
            restart();

        } else {
            setSystemProperty();
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
    public static void setSystemProperty() {
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

    public static boolean initEnv(MyBoxLoadingController controller, String lang) {
        try {
            if (controller != null) {
                controller.info(MessageFormat.format(message(lang, "InitializeDataUnder"), AppVariables.MyboxDataPath));
            }
            if (!initFiles(controller, lang)) {
                return false;
            }
            if (controller != null) {
                controller.info(MessageFormat.format(message(lang, "LoadingDatabase"), AppVariables.MyBoxDerbyPath));
            }
            DerbyBase.status = DerbyBase.DerbyStatus.NotConnected;
            String initDB = DerbyBase.startDerby();
            if (!DerbyBase.isStarted()) {
                if (controller != null) {
                    Platform.runLater(() -> {
                        PopTools.alertWarning(null, initDB);
                        MyBoxLog.console(initDB);
                    });
                }
                AppVariables.initAppVaribles();
            } else {
                // The following statements should be executed in this order
                if (controller != null) {
                    controller.info(message(lang, "InitializingTables"));
                }
                DerbyBase.initTables(null);

                if (controller != null) {
                    controller.info(message(lang, "InitializingVariables"));
                }
                AppVariables.initAppVaribles();

                if (controller != null) {
                    controller.info(message(lang, "CheckingMigration"));
                }
                MyBoxLog.console(message(lang, "CheckingMigration"));
                if (!DataMigration.checkUpdates(controller, lang)) {
                    return false;
                }
                if (controller != null) {
                    controller.info(message(lang, "InitializingTableValues"));
                }
            }

            try {
                if (controller != null) {
                    controller.info(message(lang, "InitializingEnv"));
                }

                ImageColorSpace.registrySupportedImageFormats();
                ImageIO.setUseCache(true);
                ImageIO.setCacheDirectory(AppVariables.MyBoxTempPath);

                MicrosoftDocumentTools.registryFactories();
//                        AlarmClock.scheduleAll();

            } catch (Exception e) {
                if (controller != null) {
                    controller.info(e.toString());
                }
                MyBoxLog.console(e.toString());
            }

            MyBoxLog.info(message(lang, "Load") + " " + AppValues.AppVersion);
            return true;
        } catch (Exception e) {
            if (controller != null) {
                controller.info(e.toString());
            }
            MyBoxLog.console(e.toString());
            return false;
        }
    }

    public static boolean initRootPath(MyBoxLoadingController controller, String lang) {
        try {
            File currentDataPath = new File(AppVariables.MyboxDataPath);
            if (!currentDataPath.exists()) {
                if (!currentDataPath.mkdirs()) {
                    if (controller != null) {
                        Platform.runLater(() -> {
                            PopTools.alertError(null, MessageFormat.format(message(lang,
                                    "UserPathFail"), AppVariables.MyboxDataPath));
                        });
                    }
                    return false;
                }
            }
            MyBoxLog.console("MyBox Data Path:" + AppVariables.MyboxDataPath);

            String oldPath = ConfigTools.readValue("MyBoxOldDataPath");
            if (oldPath != null) {
                if (oldPath.equals(ConfigTools.defaultDataPath())) {
                    FileDeleteTools.deleteDirExcept(null,
                            new File(oldPath), ConfigTools.defaultConfigFile());
                } else {
                    FileDeleteTools.deleteDir(new File(oldPath));
                }
                ConfigTools.writeConfigValue("MyBoxOldDataPath", null);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean initFiles(MyBoxLoadingController controller, String lang) {
        try {
            if (!initRootPath(controller, lang)) {
                return false;
            }

            AppVariables.MyBoxLogsPath = new File(AppVariables.MyboxDataPath + File.separator + "logs");
            if (!AppVariables.MyBoxLogsPath.exists()) {
                if (!AppVariables.MyBoxLogsPath.mkdirs()) {
                    if (controller != null) {
                        Platform.runLater(() -> {
                            PopTools.alertError(null, MessageFormat.format(message(lang, "UserPathFail"), AppVariables.MyBoxLogsPath));
                        });
                    }
                    return false;
                }
            }

            AppVariables.MyBoxDerbyPath = new File(AppVariables.MyboxDataPath + File.separator + "mybox_derby");
            System.setProperty("derby.stream.error.file", AppVariables.MyBoxLogsPath + File.separator + "derby.log");

            AppVariables.MyBoxLanguagesPath = new File(AppVariables.MyboxDataPath + File.separator + "mybox_languages");
            if (!AppVariables.MyBoxLanguagesPath.exists()) {
                if (!AppVariables.MyBoxLanguagesPath.mkdirs()) {
                    if (controller != null) {
                        Platform.runLater(() -> {
                            PopTools.alertError(null, MessageFormat.format(message(lang, "UserPathFail"), AppVariables.MyBoxLanguagesPath));
                        });
                    }
                    return false;
                }
            }

            AppVariables.MyBoxTempPath = new File(AppVariables.MyboxDataPath + File.separator + "AppTemp");
            if (!AppVariables.MyBoxTempPath.exists()) {
                if (!AppVariables.MyBoxTempPath.mkdirs()) {
                    if (controller != null) {
                        Platform.runLater(() -> {
                            PopTools.alertError(null, MessageFormat.format(message(lang, "UserPathFail"), AppVariables.MyBoxTempPath));
                        });
                    }
                    return false;
                }
            }

            AppVariables.AlarmClocksFile = AppVariables.MyboxDataPath + File.separator + ".alarmClocks";

            AppVariables.MyBoxReservePaths = new ArrayList<File>() {
                {
                    add(AppVariables.MyBoxTempPath);
                    add(AppVariables.MyBoxDerbyPath);
                    add(AppVariables.MyBoxLanguagesPath);
                    add(new File(AppPaths.getDownloadsPath()));
                    add(AppVariables.MyBoxLogsPath);
                }
            };

            String prefix = AppPaths.getGeneratedPath() + File.separator;
            new File(prefix + "png").mkdirs();
            new File(prefix + "jpg").mkdirs();
            new File(prefix + "pdf").mkdirs();
            new File(prefix + "htm").mkdirs();
            new File(prefix + "xml").mkdirs();
            new File(prefix + "json").mkdirs();
            new File(prefix + "txt").mkdirs();
            new File(prefix + "csv").mkdirs();
            new File(prefix + "md").mkdirs();
            new File(prefix + "xlsx").mkdirs();
            new File(prefix + "docx").mkdirs();
            new File(prefix + "pptx").mkdirs();
            new File(prefix + "svg").mkdirs();
            new File(prefix + "js").mkdirs();
            new File(prefix + "mp4").mkdirs();

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    /*
        restart
     */
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
