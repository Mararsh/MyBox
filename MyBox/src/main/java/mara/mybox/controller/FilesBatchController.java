package mara.mybox.controller;

import java.util.Date;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import mara.mybox.data.FileInformation;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2019-4-28
 * @Description
 * @License Apache License Version 2.0
 */
public class FilesBatchController extends BatchController<FileInformation> {

    protected StringBuffer newLogs;
    protected int newlines, maxLines, totalLines, cacheLines = 200;
    protected Date startTime;

    @FXML
    protected CheckBox verboseCheck;
    @FXML
    protected TextArea logsTextArea;
    @FXML
    protected TextField maxLinesinput;

    public FilesBatchController() {

    }

    @Override
    public boolean makeBatchParameters() {
        initLogs();
        startTime = new Date();
        return super.makeBatchParameters();
    }

    protected void initLogs() {
        if (logsTextArea != null) {
            logsTextArea.setText("");
            newLogs = new StringBuffer();
            newlines = 0;
            totalLines = 0;
            if (maxLinesinput != null) {
                try {
                    maxLines = Integer.parseInt(maxLinesinput.getText());
                } catch (Exception e) {
                    maxLines = 5000;
                }
            }
        }
    }

    protected void updateLogs(final String line) {
        updateLogs(line, true, false);
    }

    protected void updateLogs(final String line, boolean immediate) {
        updateLogs(line, true, immediate);
    }

    protected void updateLogs(final String line, boolean showTime, boolean immediate) {
        try {
            if (logsTextArea == null) {
                return;
            }
            if (showTime) {
                newLogs.append(DateTools.datetimeToString(new Date())).append("  ");
            }
            newLogs.append(line).append("\n");
            long past = new Date().getTime() - startTime.getTime();
            if (immediate || newlines++ > cacheLines || past > 5000) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        logsTextArea.appendText(newLogs.toString());
                        totalLines += newlines;
                        if (totalLines > maxLines + cacheLines) {
                            logsTextArea.deleteText(0, newLogs.length());
                        }
                        newLogs = new StringBuffer();
                        newlines = 0;
                    }
                });
            }
        } catch (Exception e) {

        }
    }

    @FXML
    protected void clearLogs() {
        logsTextArea.setText("");
    }

}
