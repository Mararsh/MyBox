package mara.mybox.dev;

import java.util.Date;
import javafx.application.Platform;
import mara.mybox.controller.MyBoxLogViewerController;
import mara.mybox.db.data.BaseData;
import mara.mybox.db.table.TableFactory;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.db.table.TableMyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-11-24
 * @License Apache License Version 2.0
 */
public class MyBoxLog extends BaseData {

    protected long mblid;
    protected Date time;
    protected LogType logType;
    protected String fileName, className, methodName, log, comments, callers, typeName;
    protected int line;

    public static enum LogType {
        Console, Error, Debug, Info
    }

    public MyBoxLog() {
        mblid = -1;
        time = new Date();
        logType = LogType.Console;
    }

    /*
        static methods;
     */
    public static MyBoxLog create() {
        return new MyBoxLog();
    }

    public static boolean setValue(MyBoxLog data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "mblid":
                    data.setMblid(value == null ? -1 : (long) value);
                    return true;
                case "time":
                    data.setTime(value == null ? null : (Date) value);
                    return true;
                case "log_type":
                    data.setLogType(logType((short) value));
                    return true;
                case "file_name":
                    data.setFileName(value == null ? null : (String) value);
                    return true;
                case "class_name":
                    data.setClassName(value == null ? null : (String) value);
                    return true;
                case "method_name":
                    data.setMethodName(value == null ? null : (String) value);
                    return true;
                case "line":
                    data.setLine((int) value);
                    return true;
                case "log":
                    data.setLog(value == null ? null : (String) value);
                    return true;
                case "comments":
                    data.setComments(value == null ? null : (String) value);
                    return true;
                case "callers":
                    data.setCallers(value == null ? null : (String) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(MyBoxLog data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "mblid":
                return data.getMblid();
            case "time":
                return data.getTime();
            case "log_type":
                return logType(data.getLogType());
            case "file_name":
                return data.getFileName();
            case "class_name":
                return data.getClassName();
            case "method_name":
                return data.getMethodName();
            case "line":
                return data.getLine();
            case "log":
                return data.getLog();
            case "comments":
                return data.getComments();
            case "callers":
                return data.getCallers();
        }
        return null;
    }

    public static boolean valid(MyBoxLog data) {
        return data != null
                && data.getTime() != null && data.getLog() != null && data.getLogType() != null;
    }

    public static String displayColumn(MyBoxLog data, ColumnDefinition column, Object value) {
        if (data == null || column == null || value == null) {
            return null;
        }
        if ("log_type".equals(column.getName())) {
            return Languages.message(data.getLogType().name());
        }
        return TableFactory.displayColumnBase(data, column, value);
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
            if (type == null) {
                return null;
            }
            String logString = log == null ? "null" : log.toString();
            if (logString.contains("java.sql.SQLException: No suitable driver found")
                    || logString.contains("java.sql.SQLNonTransientConnectionException")) {
                if (AppVariables.ignoreDbUnavailable) {
                    return null;
                }
                AppVariables.ignoreDbUnavailable = true; // Record only once
            }

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
                    .setLog(logString)
                    .setComments(comments)
                    .setFileName(stack.getFileName())
                    .setClassName(stack.getClassName())
                    .setMethodName(stack.getMethodName())
                    .setLine(stack.getLineNumber())
                    .setCallers(callers);
            String logText = println(myboxLog, type == LogType.Error || (AppVariables.detailedDebugLogs && type == LogType.Console));
            System.out.print(logText);
            if (AppVariables.popErrorLogs && type == LogType.Error) {
                Platform.runLater(() -> {
                    MyBoxLogViewerController controller = MyBoxLogViewerController.oneOpen();
                    if (controller != null) {
                        controller.addLog(myboxLog);
                    }
                });
            }
            boolean notSave = mylog || type == LogType.Console
                    || (type == LogType.Debug && !AppVariables.saveDebugLogs);
            if (!notSave) {
                new TableMyBoxLog().writeData(myboxLog);
            }
            return myboxLog;
        } catch (Exception e) {
            return null;
        }
    }

    public static String print(MyBoxLog log, boolean printCallers) {
        return print(log, "  ", printCallers);
    }

    public static String println(MyBoxLog log, boolean printCallers) {
        return println(log, "  ", printCallers);
    }

    public static String println(MyBoxLog log, String separator, boolean printCallers) {
        return print(log, separator, printCallers) + "\n";
    }

    public static String print(MyBoxLog log, String separator, boolean printCallers) {
        if (log == null) {
            return "";
        }
        String s = DateTools.datetimeToString(log.getTime())
                + (log.getLogType() != null && log.getLogType() != LogType.Console ? separator + log.getLogType() : "")
                + (log.getFileName() != null ? separator + log.getFileName() : "")
                + (log.getClassName() != null ? separator + log.getClassName() : "")
                + (log.getMethodName() != null ? separator + log.getMethodName() : "")
                + (log.getLine() >= 0 ? separator + log.getLine() : "")
                + (log.getLog() != null ? separator + log.getLog() : "null")
                + (log.getComments() != null ? separator + log.getComments() : "");
        if (printCallers && log.getCallers() != null && !log.getCallers().isBlank()) {
            String[] array = log.getCallers().split("\n");
            for (String a : array) {
                s += "\n\t\t\t\t" + a;
            }
        }
        return s;
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
            typeName = Languages.message(logType.name());
        }
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

}
