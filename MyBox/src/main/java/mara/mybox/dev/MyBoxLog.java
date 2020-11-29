package mara.mybox.dev;

import java.util.Date;
import javafx.application.Platform;
import mara.mybox.controller.MyBoxLogViewerController;
import mara.mybox.data.TableData;
import mara.mybox.db.ColumnDefinition;
import mara.mybox.db.TableBase;
import mara.mybox.db.TableMyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-11-24
 * @License Apache License Version 2.0
 */
public class MyBoxLog extends TableData {

    protected long mblid;
    protected Date time;
    protected LogType logType;
    protected String fileName, className, methodName, log, comments, callers, typeName;
    protected int line;

    public enum LogType {
        Console, Error, Debug, Info
    }

    public MyBoxLog() {
        mblid = -1;
        time = new Date();
        logType = LogType.Console;
    }

    public short logType() {
        return logType(logType);
    }

    public String print(boolean printCallers) {
        return print("  ", printCallers);
    }

    public String println(boolean printCallers) {
        return println("  ", printCallers);
    }

    public String println(String separator, boolean printCallers) {
        return print(separator, printCallers) + "\n";
    }

    public String print(String separator, boolean printCallers) {
        String s = DateTools.datetimeToString(time)
                + (logType != null && logType != LogType.Console ? separator + logType : "")
                + (fileName != null ? separator + fileName : "")
                + (className != null ? separator + className : "")
                + (methodName != null ? separator + methodName : "")
                + (line >= 0 ? separator + line : "")
                + (log != null ? separator + log : "")
                + (comments != null ? separator + comments : "");
        if (printCallers && callers != null && !callers.isBlank()) {
            String[] array = callers.split("\n");
            for (String a : array) {
                s += "\n\t\t\t\t" + a;
            }
        }
        return s;
    }

    @Override
    public TableBase getTable() {
        if (table == null) {
            table = new TableMyBoxLog();
        }
        return table;
    }

    @Override
    public boolean setValue(String column, Object value) {
        if (column == null) {
            return false;
        }
        try {
            switch (column) {
                case "mblid":
                    mblid = value == null ? -1 : (long) value;
                    return true;
                case "time":
                    time = value == null ? null : (Date) value;
                    return true;
                case "log_type":
                    logType = logType((short) value);
                    return true;
                case "file_name":
                    fileName = value == null ? null : (String) value;
                    return true;
                case "class_name":
                    className = value == null ? null : (String) value;
                    return true;
                case "method_name":
                    methodName = value == null ? null : (String) value;
                    return true;
                case "line":
                    line = (int) value;
                    return true;
                case "log":
                    log = value == null ? null : (String) value;
                    return true;
                case "comments":
                    comments = value == null ? null : (String) value;
                    return true;
                case "callers":
                    callers = value == null ? null : (String) value;
                    return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @Override
    public Object getValue(String column) {
        if (column == null) {
            return null;
        }
        switch (column) {
            case "mblid":
                return mblid;
            case "time":
                return time;
            case "log_type":
                return logType(logType);
            case "file_name":
                return fileName;
            case "class_name":
                return className;
            case "method_name":
                return methodName;
            case "line":
                return line;
            case "log":
                return log;
            case "comments":
                return comments;
            case "callers":
                return callers;
        }
        return null;
    }

    @Override
    public boolean valid() {
        return time != null && log != null && logType != null;
    }

    @Override
    protected String display(ColumnDefinition column, Object value) {
        if (column == null || value == null) {
            return null;
        }
        if ("log_type".equals(column.getName())) {
            return message(logType.name());
        }
        return super.display(column, value);
    }

    /*
        static methods;
     */
    public static MyBoxLog create() {
        return new MyBoxLog();
    }

    public static short logType(LogType logType) {
        if (logType == null) {
            return 3;
        }
        switch (logType) {
            case Console:
                return 0;
            case Error:
                return 1;
            case Debug:
                return 2;
            case Info:
            default:
                return 3;
        }
    }

    public static LogType logType(short logType) {
        switch (logType) {
            case 0:
                return LogType.Console;
            case 1:
                return LogType.Error;
            case 2:
                return LogType.Debug;
            default:
                return LogType.Info;
        }
    }

    public static MyBoxLog console(Object log) {
        return log(LogType.Console, log, null);
    }

    public static MyBoxLog console(Object log, String comments) {
        return log(LogType.Console, log, comments);
    }

    public static MyBoxLog debug(Object log) {
        return log(LogType.Debug, log, null);
    }

    public static MyBoxLog debug(Object log, String comments) {
        return log(LogType.Debug, log, comments);
    }

    public static MyBoxLog error(Object log) {
        return log(LogType.Error, log, null);
    }

    public static MyBoxLog error(Object log, String comments) {
        return log(LogType.Error, log, comments);
    }

    public static MyBoxLog info(Object log) {
        return log(LogType.Info, log, null);
    }

    public static MyBoxLog info(Object log, String comments) {
        return log(LogType.Info, log, comments);
    }

    private static MyBoxLog log(LogType type, Object log, String comments) {
        try {
            StackTraceElement[] stacks = new Throwable().getStackTrace();
            if (stacks.length <= 2) {
                return null;
            }
            StackTraceElement stack = stacks[2];
            boolean mylog = stack.getClassName().contains("MyBoxLog");
            String callers = null;
            for (int i = 3; i < stacks.length; ++i) {
                StackTraceElement s = stacks[i];
                if (s.getClassName().startsWith("mara.mybox")) {
                    if (callers != null) {
                        callers += "\n";
                    } else {
                        callers = "";
                    }
                    callers += s.getFileName() + " " + s.getClassName()
                            + " " + s.getMethodName() + " " + s.getLineNumber();
                    if (s.getClassName().contains("MyBoxLog")) {
                        mylog = true;
                    }
                }
            }
            MyBoxLog myboxLog = MyBoxLog.create()
                    .setLogType(type)
                    .setLog(log.toString())
                    .setComments(comments)
                    .setFileName(stack.getFileName())
                    .setClassName(stack.getClassName())
                    .setMethodName(stack.getMethodName())
                    .setLine(stack.getLineNumber())
                    .setCallers(callers);
            String logText = myboxLog.println(type == LogType.Error);
            System.out.print(logText);
            if (type == LogType.Error) {
                Platform.runLater(() -> {
                    MyBoxLogViewerController controller = MyBoxLogViewerController.oneOpen();
                    if (controller != null) {
                        controller.addLog(myboxLog);
                    }
                });
            }
            boolean notSave = mylog || type == LogType.Console
                    || (type == LogType.Debug && !AppVariables.devMode);
            if (!notSave) {
                new TableMyBoxLog().writeData(myboxLog);
            }
            return myboxLog;
        } catch (Exception e) {
            return null;
        }
    }

    /*
        set/get
     */
    public Date getTime() {
        return time;
    }

    public MyBoxLog setTime(Date time) {
        this.time = time;
        return this;
    }

    public String getLog() {
        return log;
    }

    public MyBoxLog setLog(String log) {
        this.log = log;
        return this;
    }

    public LogType getLogType() {
        return logType;
    }

    public MyBoxLog setLogType(LogType logType) {
        this.logType = logType;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public MyBoxLog setClassName(String className) {
        this.className = className;
        return this;
    }

    public String getMethodName() {
        return methodName;
    }

    public MyBoxLog setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public long getMblid() {
        return mblid;
    }

    public MyBoxLog setMblid(long mblid) {
        this.mblid = mblid;
        return this;
    }

    public String getComments() {
        return comments;
    }

    public MyBoxLog setComments(String comments) {
        this.comments = comments;
        return this;
    }

    public int getLine() {
        return line;
    }

    public MyBoxLog setLine(int line) {
        this.line = line;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public MyBoxLog setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getCallers() {
        return callers;
    }

    public MyBoxLog setCallers(String callers) {
        this.callers = callers;
        return this;
    }

    public String getTypeName() {
        if (logType != null) {
            typeName = message(logType.name());
        }
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

}
