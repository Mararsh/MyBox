package mara.mybox.tools;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @Author Mara
 * @CreateDate 2022-11-2
 * @License Apache License Version 2.0
 */
public class ScheduleTools {

    public final static ScheduledExecutorService service = Executors.newScheduledThreadPool(2);

}
