package mara.mybox.tools;

import java.awt.MouseInfo;
import java.awt.Point;
import java.util.Map;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2019-1-3 20:51:26
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class SystemTools {

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

}
