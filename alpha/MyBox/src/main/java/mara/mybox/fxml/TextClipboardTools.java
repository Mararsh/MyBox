package mara.mybox.fxml;

import java.io.File;
import java.sql.Connection;
import javafx.application.Platform;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.TextInMyBoxClipboardController;
import mara.mybox.controller.TextInSystemClipboardController;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.table.TableTextClipboard;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.TextClipboardMonitor.DefaultInterval;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.AppVariables.textClipboardMonitor;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class TextClipboardTools {

    /*
        monitor
     */
    public static void stopTextClipboardMonitor() {
        if (textClipboardMonitor != null) {
            textClipboardMonitor.stop();
            textClipboardMonitor = null;
        }
    }

    public static void startTextClipboardMonitor() {
        startTextClipboardMonitor(getMonitorInterval());
    }

    public static void startTextClipboardMonitor(int interval) {
        if (textClipboardMonitor != null) {
            textClipboardMonitor.cancel();
            textClipboardMonitor = null;
        }
        textClipboardMonitor = new TextClipboardMonitor().start(interval);
    }

    public static int getMonitorInterval() {
        int v = UserConfig.getInt("TextClipboardMonitorInterval", DefaultInterval);
        if (v <= 0) {
            v = DefaultInterval;
        }
        return v;
    }

    public static int setMonitorInterval(int v) {
        if (v <= 0) {
            v = DefaultInterval;
        }
        UserConfig.setInt("TextClipboardMonitorInterval", v);
        return v;
    }

    public static boolean isMonitoring() {
        return textClipboardMonitor != null;
    }

    public static boolean isCopy() {
        return UserConfig.getBoolean("CopyTextInSystemClipboard", false);
    }

    public static boolean isMonitoringCopy() {
        return isMonitoring() && isCopy();
    }

    public static void setCopy(boolean value) {
        UserConfig.setBoolean("CopyTextInSystemClipboard", value);
    }

    public static boolean isStartWhenBoot() {
        return UserConfig.getBoolean("StartTextClipboardMonitorWhenBoot", false);
    }

    public static void setStartWhenBoot(boolean value) {
        UserConfig.setBoolean("StartTextClipboardMonitorWhenBoot", value);
    }

    /*
        System Clipboard
     */
    public static boolean systemClipboardHasString() {
        return Clipboard.getSystemClipboard().hasString();
    }

    public static String getSystemClipboardString() {
        return Clipboard.getSystemClipboard().getString();
    }

    public static boolean stringToSystemClipboard(String string) {
        try {
            if (string == null || string.isBlank()) {
                return false;
            }
            ClipboardContent cc = new ClipboardContent();
            cc.putString(string);
            Clipboard.getSystemClipboard().setContent(cc);
            TextInSystemClipboardController.updateSystemClipboardStatus();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public static void copyToSystemClipboard(BaseController controller, TextInputControl textInput) {
        if (controller == null || textInput == null) {
            return;
        }
        String text = textInput.getSelectedText();
        if (text == null || text.isEmpty()) {
            text = textInput.getText();
        }
        copyToSystemClipboard(controller, text);
    }

    public static void copyToSystemClipboard(BaseController controller, String text) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (text == null || text.isEmpty()) {
                    controller.popError(message("SelectToHandle"));
                    return;
                }
                if (stringToSystemClipboard(text)) {
                    int len = text.length();
                    String info = "\n" + message("CharactersNumber") + ":" + len
                            + "\n----------------------\n"
                            + (len > 100 ? text.substring(0, 100) + "\n......" : text);
                    if (TextClipboardTools.isMonitoringCopy()) {
                        controller.popInformation(message("CopiedInClipBoards") + info);
                    } else {
                        controller.popInformation(message("CopiedInSystemClipBoard") + info);
                    }
                } else {
                    controller.popFailed();
                }
            }
        });
    }

    public static void copyFileToSystemClipboard(BaseController controller, File file) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (file == null || !file.exists()) {
                    controller.popError(message("SelectToHandle"));
                    return;
                }
                copyToSystemClipboard(controller, TextFileTools.readTexts(file));
            }
        });
    }

    /*
         MyBox Clipboard
     */
    public static void copyToMyBoxClipboard(BaseController controller, TextInputControl textInput) {
        if (controller == null || textInput == null) {
            return;
        }
        String text = textInput.getSelectedText();
        if (text == null || text.isEmpty()) {
            text = textInput.getText();
        }
        copyToMyBoxClipboard(controller, text);
    }

    public static void copyToMyBoxClipboard(BaseController controller, String text) {
        if (controller == null) {
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (text == null || text.isEmpty()) {
                    controller.popError(message("SelectToHandle"));
                    return;
                }
                if (stringToMyBoxClipboard(text)) {
                    String info = text.length() > 200 ? text.substring(0, 200) + "\n......" : text;
                    controller.popInformation(message("CopiedInMyBoxClipBoard") + "\n----------------------\n" + info);
                } else {
                    controller.popFailed();
                }
            }
        });
    }

    public static boolean stringToMyBoxClipboard(String string) {
        if (string == null || string.isBlank()) {
            return false;
        }
        return stringToMyBoxClipboard(null, null, string);
    }

    public static boolean stringToMyBoxClipboard(TableTextClipboard inTable, Connection inConn, String string) {
        try {
            if (string == null || string.isBlank()) {
                return false;
            }
            new Thread() {
                @Override
                public void run() {
                    try {
                        TableTextClipboard table = inTable;
                        if (table == null) {
                            table = new TableTextClipboard();
                        }
                        Connection conn = inConn;
                        if (conn == null || conn.isClosed()) {
                            conn = DerbyBase.getConnection();
                        }
                        table.save(conn, string);
                        conn.commit();
                        TextInMyBoxClipboardController.updateMyBoxClipboard();
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                    }
                }
            }.start();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

}
