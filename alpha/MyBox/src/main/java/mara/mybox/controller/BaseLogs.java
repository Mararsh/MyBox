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
public class BaseLogs extends BaseController {

    protected int logsMaxChars, logsTotalLines;
    protected int logsNewlines;
    protected StringBuffer newLogs;
    protected long lastLogTime;

    @FXML
    protected CheckBox verboseCheck;
    @FXML
    protected TextArea logsTextArea;
    @FXML
    protected TextField maxCharsinput;

    @Override
    public void initControls() {
        try {
            super.initControls();

            logsMaxChars = UserConfig.getInt("TaskMaxLinesNumber", 5000);
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
                            int iv = Integer.parseInt(maxCharsinput.getText());
                            if (iv > 0) {
                                logsMaxChars = iv;
                                maxCharsinput.setStyle(null);
                                UserConfig.setInt("TaskMaxLinesNumber", logsMaxChars);
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
            MyBoxLog.debug(e.toString());
        }
    }

    public void initLogs() {
        if (logsTextArea != null) {
            if (!logsTextArea.getText().isBlank()) {
                updateLogs("\n", false, true);
                return;
            }
        }
        newLogs = new StringBuffer();
        logsNewlines = 0;
        logsTotalLines = 0;
        lastLogTime = new Date().getTime();
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
        try {
            if (logsTextArea == null || line == null) {
                return;
            }
            Platform.runLater(() -> {
                try {
                    synchronized (logsTextArea) {
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
                            logsTextArea.appendText(newLogs.toString());
                            newLogs = new StringBuffer();
                            logsTotalLines += logsNewlines;
                            logsNewlines = 0;
                            lastLogTime = ctime;
                            int extra = logsTextArea.getText().length() - logsMaxChars;
                            if (extra > 0) {
                                logsTextArea.deleteText(0, extra);
                            }
                        }
                    }
                } catch (Exception e) {
                    MyBoxLog.debug(e.toString());
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void clearLogs() {
        if (logsTextArea == null) {
            return;
        }
        Platform.runLater(() -> {
            synchronized (logsTextArea) {
                logsTextArea.setText("");
                logsTotalLines = 0;
            }
        });
    }

}
