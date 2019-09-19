package mara.mybox.tools;

import java.awt.Desktop;
import java.awt.MouseInfo;
import java.awt.Point;
import java.io.File;
import java.net.URI;
import java.security.MessageDigest;
import java.util.Map;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import static mara.mybox.value.AppVariables.logger;

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
        for (int i = 0; i < count; i++) {
            showThread(threads[i], index + "  ");
        }
        for (int i = 0; i < countGroup; i++) {
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
        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
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
        logger.debug(thread.getId() + " " + thread.getName() + " " + thread.getState());
        for (StackTraceElement element : thread.getStackTrace()) {
            logger.debug(element);
        }
    }

    public static long getAvaliableMemory() {
        Runtime r = Runtime.getRuntime();
        return r.maxMemory() - (r.totalMemory() - r.freeMemory());
    }

    public static long getAvaliableMemoryMB() {
        return getAvaliableMemory() / (1024 * 1024);
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

    public static void browseURL(String url) {
        try {
            browseURI(new URI(url));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void browseURL(File file) {
        try {
            browseURI(file.toURI());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void browseURI(URI url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(url);
            } else {
                logger.error("Desktop is not supported");
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
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

    public static byte[] MD5(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(bytes);
            return digest;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

}
