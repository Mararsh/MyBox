package mara.mybox.tools;

import java.awt.MouseInfo;
import java.awt.Point;
import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2019-1-3 20:51:26
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

    public static long freeBytes() {
        return getAvaliableMemory() - 200 * 1024 * 1024;
    }

    public static Point getMousePoint() {
        return MouseInfo.getPointerInfo().getLocation();
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

    // https://bugs.openjdk.org/browse/JDK-8266075
    // https://docs.oracle.com/en/java/javase/18/docs/api/java.base/java/lang/Process.html#inputReader()
    public static Charset ConsoleCharset() {
        try {
            return Charset.forName(System.getProperty("native.encoding"));
        } catch (Exception e) {
            return Charset.defaultCharset();
        }
    }

    public static String run(String cmd) {
        try {
            if (cmd == null || cmd.isBlank()) {
                return null;
            }
            List<String> p = new ArrayList<>();
            p.addAll(Arrays.asList(StringTools.splitBySpace(cmd)));
            ProcessBuilder pb = new ProcessBuilder(p).redirectErrorStream(true);
            final Process process = pb.start();
            StringBuilder s = new StringBuilder();
            try ( BufferedReader inReader = process.inputReader()) {
                String line;
                while ((line = inReader.readLine()) != null) {
                    s.append(line).append("\n");
                }
            }
            process.waitFor();
            return s.toString();
        } catch (Exception e) {
            return e.toString();
        }
    }

}
