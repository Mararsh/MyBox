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

    protected int logsMaxLines, logsTotalLines;
    protected final int logsCacheLines = 200;

    @FXML
    protected CheckBox verboseCheck;
    @FXML
    protected TextArea logsTextArea;
    @FXML
    protected TextField maxLinesinput;

    @Override
    public void initControls() {
        try {
            super.initControls();

            logsMaxLines = UserConfig.getInt("TaskMaxLinesNumber", 5000);
            if (logsMaxLines <= 0) {
                logsMaxLines = 5000;
            }
            if (maxLinesinput != null) {
                maxLinesinput.setText(logsMaxLines + "");
                maxLinesinput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> v, String ov, String nv) {
                        try {
                            int iv = Integer.parseInt(maxLinesinput.getText());
                            if (iv > 0) {
                                logsMaxLines = iv;
                                maxLinesinput.setStyle(null);
                                UserConfig.setInt("TaskMaxLinesNumber", logsMaxLines);
                            } else {
                                maxLinesinput.setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            maxLinesinput.setStyle(UserConfig.badStyle());
                        }
                    }
                });
            }

            clearLogs();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void updateLogs(final String line) {
        try {
            if (logsTextArea == null || line == null || line.isBlank()) {
                return;
            }
            Platform.runLater(() -> {
                String s = DateTools.datetimeToString(new Date()) + "  " + line + "\n";
                logsTextArea.appendText(s);
                logsTotalLines++;
                if (logsTotalLines > logsMaxLines) {
                    int pos = logsTextArea.getText().indexOf("\n");
                    if (pos > 0) {
                        logsTextArea.deleteText(0, pos);
                    }
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
            logsTextArea.setText("");
            logsTotalLines = 0;
        });
    }

}
