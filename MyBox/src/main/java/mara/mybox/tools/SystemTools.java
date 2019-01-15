package mara.mybox.tools;

import java.util.Map;
import static mara.mybox.objects.AppVaribles.logger;

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

}
