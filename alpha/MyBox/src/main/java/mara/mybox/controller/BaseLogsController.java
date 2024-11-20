package mara.mybox.controller;

import java.util.Date;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-11-4
 * @License Apache License Version 2.0
 */
public class BaseLogsController extends BaseController {

    protected long logsMaxChars, logsTotalLines, logsTotalchars, logsNewlines;
    protected StringBuffer newLogs;
    protected long lastLogTime;
    protected final Object lock = new Object();

    @FXML
    protected TextArea logsTextArea;
    @FXML
    protected TextField maxCharsinput;
    @FXML
    protected CheckBox verboseCheck;
    @FXML
    protected BaseLogsController logsController;

    @Override
    public void initValues() {
        try {
            super.initValues();

            if (logsController != null) {
                if (logsTextArea == null) {
                    logsTextArea = logsController.logsTextArea;
                }
                if (maxCharsinput == null) {
                    maxCharsinput = logsController.maxCharsinput;
                }
                if (verboseCheck == null) {
                    verboseCheck = logsController.verboseCheck;
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            logsMaxChars = UserConfig.getLong("TaskMaxLinesNumber", 5000);
            if (logsMaxChars <= 0) {
                logsMaxChars = 5000;
            }
            if (maxCharsinput != null) {
                maxCharsinput.setText(logsMaxChars + "");
                maxCharsinput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                        try {
                            if (nv) {
                                return;
                            }
                            long iv = Long.parseLong(maxCharsinput.getText());
                            if (iv > 0) {
                                logsMaxChars = iv;
                                maxCharsinput.setStyle(null);
                                UserConfig.setLong("TaskMaxLinesNumber", logsMaxChars);
                            } else {
                                maxCharsinput.setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            maxCharsinput.setStyle(UserConfig.badStyle());
                        }
                    }
                });
            }

            clearLogs();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public boolean isLogsVerbose() {
        return verboseCheck != null ? verboseCheck.isSelected() : false;
    }

    @FXML
    public void clearLogs() {
        if (logsTextArea == null) {
            return;
        }
        try {
            synchronized (lock) {
                newLogs = new StringBuffer();
                logsNewlines = 0;
                logsTotalLines = 0;
                logsTotalchars = 0;
                lastLogTime = new Date().getTime();
                logsTextArea.setText("");
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void initLogs() {
        if (logsTextArea == null) {
            return;
        }
        if (logsTotalchars > 0) {
            updateLogs("\n", false, true);
        } else {
            clearLogs();
        }
    }

    public void showLogs(String line) {
        updateLogs(line, true, true);
    }

    public void updateLogs(String line) {
        updateLogs(line, true, false);
    }

    protected void updateLogs(String line, boolean immediate) {
        updateLogs(line, true, immediate);
    }

    public void updateLogs(String line, boolean showTime, boolean immediate) {
        if (line == null) {
            return;
        }
        if (Platform.isFxApplicationThread()) {
            if (logsTextArea == null) {
                popInformation(line);
            } else {
                writeLogs(line, showTime, immediate);
            }
        } else {
            Platform.runLater(() -> {
                if (logsTextArea == null) {
                    popInformation(line);
                } else {
                    writeLogs(line, showTime, immediate);
                }
            });
        }
    }

    public void writeLogs(String line, boolean showTime, boolean immediate) {
        try {
            synchronized (lock) {
                if (newLogs == null) {
                    newLogs = new StringBuffer();
                }
                if (showTime) {
                    newLogs.append(DateTools.datetimeToString(new Date())).append("  ");
                }
                newLogs.append(line).append("\n");
                logsNewlines++;
                long ctime = new Date().getTime();
                if (immediate || logsNewlines > 50 || ctime - lastLogTime > 3000) {
                    String s = newLogs.toString();
                    logsTotalchars += s.length();
                    logsTextArea.appendText(s);
                    newLogs = new StringBuffer();
                    logsTotalLines += logsNewlines;
                    logsNewlines = 0;
                    lastLogTime = ctime;
                    int extra = (int) (logsTotalchars - logsMaxChars);
                    if (extra > 0) {
                        logsTextArea.deleteText(0, extra);
                        logsTotalchars = logsMaxChars;
                    }
                    logsTextArea.selectEnd();
                    logsTextArea.deselect();
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

}
