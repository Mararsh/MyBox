package mara.mybox.dev;

import java.util.Date;
import javafx.application.Platform;
import mara.mybox.controller.MyBoxLogViewerController;
import mara.mybox.db.data.BaseData;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.table.TableMyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.ErrorNotify;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-11-24
 * @License Apache License Version 2.0
 */
public class MyBoxLog extends BaseData {

    protected long mblid, count;
    protected Date time;
    protected LogType logType;
    protected String fileName, className, methodName, log, comments, callers, typeName;
    protected int line;
    public static MyBoxLog LastMyBoxLog;
    public static long LastRecordTime;
    public static int MinInterval = 1;  // ms

    public static enum LogType {
        Console, Error, Debug, Info
    }

    public MyBoxLog() {
        mblid = -1;
        time = new Date();
        logType = LogType.Console;
    }

    @Override
    public boolean valid() {
        return valid(this);
    }

    @Override
    public boolean setValue(String column, Object value) {
        return setValue(this, column, value);
    }

    @Override
    public Object getValue(String column) {
        return getValue(this, column);
    }

    public boolean equalTo(MyBoxLog myMyboxLog) {
        return myMyboxLog != null && logType == myMyboxLog.getLogType()
                && log != null && log.equals(myMyboxLog.getLog())
                && fileName != null && fileName.equals(myMyboxLog.getFileName())
                && className != null && className.equals(myMyboxLog.getClassName())
                && methodName != null && methodName.equals(myMyboxLog.getMethodName())
                && callers != null && callers.equals(myMyboxLog.getCallers())
                && line == myMyboxLog.getLine();
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
            MyBoxLog.debug(e);
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
        if ("log_type".equals(column.getColumnName())) {
            return Languages.message(data.getLogType().name());
        }
        return column.formatValue(value);
    }

    public static short logType(LogType logType) {
        if (logType == null) {
            return (short) LogType.Info.ordinal();
        }
        return (short) logType.ordinal();
    }

    public static LogType logType(short logType) {
        try {
            return LogType.values()[logType];
        } catch (Exception e) {
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

    public static boolean isFlooding() {
        return new Date().getTime() - LastRecordTime < MinInterval;
    }

    private static MyBoxLog log(LogType type, Object log, String comments) {
        try {
            if (type == null) {
                return null;
            }
//            if (isFlooding()) {
//                return null;
//            }
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
            boolean isMylog = stack.getClassName().contains("MyBoxLog");
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
                        isMylog = true;
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
            String logText = println(myboxLog, type == LogType.Error || (AppVariables.detailedDebugLogs && type == LogType.Debug));
            System.out.print(logText);
            if (LastMyBoxLog != null && LastMyBoxLog.equalTo(myboxLog)) {
                return myboxLog;
            }
            if (logString.contains("java.sql.SQLNonTransientConnectionException")) {
                type = LogType.Debug;
            }
            if (AppVariables.popErrorLogs && type == LogType.Error) {
                Platform.runLater(() -> {
                    MyBoxLogViewerController controller = MyBoxLogViewerController.oneOpen();
                    if (controller != null) {
                        controller.addLog(myboxLog);
                    }
                });
            }

            boolean notSave = isMylog || type == LogType.Console
                    || (type == LogType.Debug && !AppVariables.saveDebugLogs);
            if (!notSave) {
                new TableMyBoxLog().writeData(myboxLog);
            }
            if (type == LogType.Error) {
                ErrorNotify.set(!ErrorNotify.get());
            }
            LastMyBoxLog = myboxLog;
            LastRecordTime = new Date().getTime();
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
