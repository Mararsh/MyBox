package mara.mybox.tools;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.image.ImageManufacture;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-1-3 20:51:26
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class SystemTools {

    public static float jreVersion() {
        return Float.parseFloat(System.getProperty("java.version").substring(0, 3));
    }

    public static String os() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            return "win";

        } else if (os.contains("linux")) {
            return "linux";

        } else if (os.contains("mac")) {
            return "mac";

        } else {
            return "other";
        }
    }

    public static boolean isLinux() {
        return os().contains("linux");
    }

    public static boolean isMac() {
        return os().contains("mac");
    }

    public static boolean isWindows() {
        return os().contains("win");
    }

    public static File myboxCacerts() {
        return new File(AppVariables.MyboxDataPath + File.separator + "security"
                + File.separator + "cacerts_mybox");
    }

    public static void resetKeystore() {
        FileTools.delete(myboxCacerts());
        keystore();
    }

    public static String keystore() {
        String jvm_cacerts = System.getProperty("java.home") + File.separator + "lib"
                + File.separator + "security" + File.separator + "cacerts";
        try {
            File file = myboxCacerts();
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                FileTools.copyFile(new File(jvm_cacerts), file);
            }
            if (file.exists()) {
                return file.getAbsolutePath();
            } else {
                return jvm_cacerts;
            }
        } catch (Exception e) {
            return jvm_cacerts;
        }
    }

    public static String keystorePassword() {
        return "changeit";
    }

    // https://blog.csdn.net/sdtvyyb_007/article/details/77160239
    public static void listAllThreads() {
        ThreadGroup currentThreadGroup = Thread.currentThread().getThreadGroup();
        ThreadGroup root = currentThreadGroup;
        ThreadGroup parent = root.getParent();
        while (parent != null) {
            root = parent;
            parent = root.getParent();
        }
        showThreadGroup(root, "");
    }

    public static void showThreadGroup(ThreadGroup group, String index) {//显示线程组信息
        if (group == null) {
            return;
        }
        int count = group.activeCount();
        int countGroup = group.activeGroupCount();
        Thread[] threads = new Thread[count];
        ThreadGroup[] groups = new ThreadGroup[countGroup];
        group.enumerate(threads, false);
        group.enumerate(groups, false);
        System.out.println(index + "线程组的名称- " + group.getName() + " 最高优先级- "
                + group.getMaxPriority() + (group.isDaemon() ? " 守护" : " "));
        for (int i = 0; i < count; ++i) {
            showThread(threads[i], index + "  ");
        }
        for (int i = 0; i < countGroup; ++i) {
            showThreadGroup(groups[i], index + "  ");
        }
    }

    public static void showThread(Thread thread, String index) {
        if (thread == null) {
            return;
        }
        System.out.println(index + "线程的名称-" + thread.getName() + " 最高优先级- "
                + thread.getPriority() + (thread.isDaemon() ? " 守护" : " ") + (thread.isAlive() ? " 活动" : " 不活动"));
    }

    public static void threadsStackTrace() {
        for (Map.Entry<Thread, StackTraceElement[]> entry
                : Thread.getAllStackTraces().entrySet()) {
            Thread thread = entry.getKey();
            StackTraceElement[] stackTraceElements = entry.getValue();
            if (thread.equals(Thread.currentThread())) {
                continue;
            }
            System.out.println("\n线程： " + thread.getName() + "\n");
            for (StackTraceElement element : stackTraceElements) {
                System.out.println("\t" + element + "\n");
            }
        }
    }

    public static void currentThread() {
        Thread thread = Thread.currentThread();
        MyBoxLog.debug(thread.getId() + " " + thread.getName() + " " + thread.getState());
        for (StackTraceElement element : thread.getStackTrace()) {
            MyBoxLog.debug(element.toString());
        }
    }

    public static long getAvaliableMemory() {
        Runtime r = Runtime.getRuntime();
        return r.maxMemory() - (r.totalMemory() - r.freeMemory());
    }

    public static long getAvaliableMemoryMB() {
        return getAvaliableMemory() / (1024 * 1024L);
    }

    public static Point getMousePoint() {
        return MouseInfo.getPointerInfo().getLocation();
    }

    public static Image fetchImageInClipboard(boolean clear) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (!clipboard.hasImage()) {
            return null;
        }
        Image image = clipboard.getImage();
        if (clear) {
            clipboard.clear();
        }
        return image;
    }

    public static String fetchTextInClipboard(boolean clear) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (!clipboard.hasString()) {
            return null;
        }
        String text = clipboard.getString();
        if (clear) {
            clipboard.clear();
        }
        return text;
    }

    public static String IccProfilePath() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            return "C:\\Windows\\System32\\spool\\drivers\\color";

        } else if (os.contains("linux")) {
            // /usr/share/color/icc
            // /usr/local/share/color/icc
            // /home/USER_NAME/.color/icc
            return "/usr/share/color/icc";

        } else if (os.contains("mac")) {
            // /Library/ColorSync/Profiles
            // /Users/USER_NAME/Library/ColorSync/Profile
            return "/Library/ColorSync/Profiles";

        } else {
            return null;
        }
    }

    public static void SignatureAlgorithms() {
        try {
            for (Provider provider : Security.getProviders()) {
                for (Provider.Service service : provider.getServices()) {
                    if (service.getType().equals("Signature")) {
                        MyBoxLog.debug(service.getAlgorithm());
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public static byte[] MD5(byte[] bytes) {
        return messageDigest(bytes, "MD5");
    }

    public static byte[] SHA1(byte[] bytes) {
        return messageDigest(bytes, "SHA-1");
    }

    public static byte[] SHA256(byte[] bytes) {
        return messageDigest(bytes, "SHA-256");
    }

    public static byte[] MD5(File file) {
        return messageDigest(file, "MD5");
    }

    public static byte[] SHA1(File file) {
        return messageDigest(file, "SHA-1");
    }

    public static byte[] SHA256(File file) {
        return messageDigest(file, "SHA-256");
    }

    public static byte[] MD5(BufferedImage image) {
        return messageDigest(ImageManufacture.bytes(image), "MD5");
    }

    public static byte[] SHA1(BufferedImage image) {
        return messageDigest(ImageManufacture.bytes(image), "SHA-1");
    }

    public static byte[] SHA256(BufferedImage image) {
        return messageDigest(ImageManufacture.bytes(image), "SHA-256");
    }

    // https://docs.oracle.com/javase/10/docs/specs/security/standard-names.html#messagedigest-algorithms
    public static byte[] messageDigest(byte[] bytes, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] digest = md.digest(bytes);
            return digest;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static byte[] messageDigest(File file, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            try ( BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
                byte[] buf = new byte[CommonValues.IOBufferLength];
                int len;
                while ((len = in.read(buf)) > 0) {
                    md.update(buf, 0, len);
                }
            }
            byte[] digest = md.digest();
            return digest;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }

    }

    public static void SSLServerSocketInfo() {
        try {
            SSLServerSocket sslServerSocket;
            SSLServerSocketFactory ssl = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            sslServerSocket = (SSLServerSocket) ssl.createServerSocket();

            String[] cipherSuites = sslServerSocket.getSupportedCipherSuites();
            for (String suite : cipherSuites) {
                MyBoxLog.debug(suite);
            }

            String[] protocols = sslServerSocket.getSupportedProtocols();
            for (String protocol : protocols) {
                MyBoxLog.debug(protocol);
            }
        } catch (Exception e) {

        }

    }

    public static List<String> ttfList() {
        List<String> names = new ArrayList<>();
        try {
            String os = os();
            File ttfPath;
            switch (os) {
                case "win":
                    ttfPath = new File("C:/Windows/Fonts/");
                    names.addAll(ttfList(ttfPath));
                    break;
                case "linux":
                    ttfPath = new File("/usr/share/fonts/");
                    names.addAll(ttfList(ttfPath));
                    ttfPath = new File("/usr/lib/kbd/consolefonts/");
                    names.addAll(ttfList(ttfPath));
                    break;
                case "mac":
                    ttfPath = new File("/Library/Fonts/");
                    names.addAll(ttfList(ttfPath));
                    ttfPath = new File("/System/Library/Fonts/");
                    names.addAll(ttfList(ttfPath));
                    break;
            }

            // http://wenq.org/wqy2/
            File wqy_microhei = FxmlControl.getInternalFile("/data/wqy-microhei.ttf", "data", "wqy-microhei.ttf", false);
            String wqy_microhei_name = wqy_microhei.getAbsolutePath() + "      " + message("wqy_microhei");
            if (!names.isEmpty() && names.get(0).contains("    ")) {
                names.add(1, wqy_microhei_name);
            } else {
                names.add(0, wqy_microhei_name);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return names;
    }

    public static List<String> ttfList(File path) {
        List<String> names = new ArrayList<>();
        try {
            if (path == null || !path.exists() || !path.isDirectory()) {
                return names;
            }
            File[] fontFiles = path.listFiles();
            if (fontFiles == null || fontFiles.length == 0) {
                return names;
            }
            for (File file : fontFiles) {
                String filename = file.getAbsolutePath();
                if (!filename.toLowerCase().endsWith(".ttf")) {
                    continue;
                }
                names.add(filename);
            }
            String pathname = path.getAbsolutePath() + File.separator;
            List<String> cnames = new ArrayList<>();
            if (names.contains(pathname + "STSONG.TTF")) {
                cnames.add(pathname + "STSONG.TTF" + "      华文宋体");
            }
            if (names.contains(pathname + "simfang.ttf")) {
                cnames.add(pathname + "simfang.ttf" + "      仿宋");
            }
            if (names.contains(pathname + "simkai.ttf")) {
                cnames.add(pathname + "simkai.ttf" + "      楷体");
            }

            if (names.contains(pathname + "STKAITI.TTF")) {
                cnames.add(pathname + "STKAITI.TTF" + "      华文楷体");
            }
            if (names.contains(pathname + "SIMLI.TTF")) {
                cnames.add(pathname + "SIMLI.TTF" + "      隶书");
            }
            if (names.contains(pathname + "STXINWEI.TTF")) {
                cnames.add(pathname + "STXINWEI.TTF" + "      华文新魏");
            }
            if (names.contains(pathname + "SIMYOU.TTF")) {
                cnames.add(pathname + "SIMYOU.TTF" + "      幼圆");
            }
            if (names.contains(pathname + "FZSTK.TTF")) {
                cnames.add(pathname + "FZSTK.TTF" + "      方正舒体");
            }

            if (names.contains(pathname + "STXIHEI.TTF")) {
                cnames.add(pathname + "STXIHEI.TTF" + "      华文细黑");
            }
            if (names.contains(pathname + "simhei.ttf")) {
                cnames.add(pathname + "simhei.ttf" + "      黑体");
            }

            for (String name : names) {
                if (name.contains(pathname + "华文")) {
                    cnames.add(name);
                }
            }

            names.addAll(0, cnames);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return names;
    }

    public static String ttf(String item) {
        if (item == null) {
            return null;
        }
        int pos = item.indexOf("    ");
        if (pos > 0) {
            return item.substring(0, pos);
        }
        return item;
    }

}
