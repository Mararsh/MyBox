package mara.mybox.controller;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import mara.mybox.data.FileEditInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxBackgroundTask;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class BaseTextController_File extends BaseTextController_Main {

    @Override
    public void sourceFileChanged(File file) {
        sourceFile = null;
        sourceInformation = null;
        openFile(file);
    }

    public void openFile(File file) {
        if (editType == FileEditInformation.Edit_Type.Bytes) {
            openBytesFile(file);
        } else {
            openTextFile(file);
        }
    }

    private void openTextFile(File file) {
        if (file == null) {
            return;
        }
        initPage(file);
        popInformation(message("CheckingEncoding"));
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                if (sourceInformation == null || sourceFile == null) {
                    return false;
                }
                if (!sourceInformation.isCharsetDetermined()) {
                    sourceInformation.setLineBreak(TextTools.checkLineBreak(this, sourceFile));
                    sourceInformation.setLineBreakValue(TextTools.lineBreakValue(sourceInformation.getLineBreak()));
                    return sourceInformation.checkCharset();
                } else {
                    return true;
                }
            }

            @Override
            protected void whenSucceeded() {
                sourceInformation.setCharsetDetermined(true);
                goPage();
            }

        };
        start(task);
    }

    private void openBytesFile(File file) {
        if (file == null) {
            return;
        }
        if (lineBreak == FileEditInformation.Line_Break.Value && lineBreakValue == null
                || lineBreak == FileEditInformation.Line_Break.Width && lineBreakWidth <= 0) {
            popError(message("WrongLineBreak"));
            return;
        }
        initPage(file);
        sourceInformation.setLineBreak(lineBreak);
        sourceInformation.setLineBreakValue(lineBreakValue);
        sourceInformation.setLineBreakWidth(lineBreakWidth);
        goPage();
    }

    protected void initPage(File file) {
        try {
            if (task != null) {
                task.cancel();
                task = null;
            }
            if (backgroundTask != null) {
                backgroundTask.cancel();
                backgroundTask = null;
            }

            isSettingValues = true;

            fileChanged.set(false);
            sourceFile = file;

            FileEditInformation existedInfo = sourceInformation;
            sourceInformation = FileEditInformation.create(editType, file);
            sourceInformation.pagination = pagination;
            if (existedInfo != null) {
                sourceInformation.setCharset(existedInfo.getCharset());
                sourceInformation.setWithBom(existedInfo.isWithBom());
                sourceInformation.setLineBreak(existedInfo.getLineBreak());
                sourceInformation.setLineBreakValue(TextTools.lineBreakValue(sourceInformation.getLineBreak()));
                sourceInformation.setCurrentPage(existedInfo.getCurrentPage());
                sourceInformation.setCharsetDetermined(existedInfo.isCharsetDetermined());
            } else {
                sourceInformation.setCurrentPage(0);
            }
            sourceInformation.setFindReplace(null);

            mainArea.clear();
            lineArea.clear();
            paginationController.reset();
            recoverButton.setDisable(file == null);
            clearPairArea();

            isSettingValues = false;

            mainArea.requestFocus();

            checkAutoSave();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        isSettingValues = false;
    }

    protected void checkAutoSave() {
        try {
            if (autoCheckTimer != null) {
                autoCheckTimer.cancel();
                autoCheckTimer = null;
            }
            if (sourceFile == null || !autoSave || autoCheckInterval <= 0) {
                return;
            }
            long interval = autoCheckInterval * 1000;
            autoCheckTimer = new Timer();
            autoCheckTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        if (fileChanged.get()) {
                            saveAction();
                        }
                    });
                    Platform.requestNextPulse();
                }
            }, interval, interval);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    protected void loadTotalNumbers() {
        if (sourceInformation == null || sourceFile == null || sourceInformation.isTotalNumberRead()) {
            return;
        }
        if (backgroundTask != null) {
            backgroundTask.cancel();
            backgroundTask = null;
        }
        backgroundTask = new FxBackgroundTask<Void>(this) {

            @Override
            protected boolean handle() {
                ok = sourceInformation.readTotalNumbers(this);
                return ok;
            }

            @Override
            protected void whenSucceeded() {
                updateNumbers(false);
            }

        };
        start(backgroundTask, false);
    }

    @Override
    public void loadPage(long page) {
        if (sourceInformation == null || sourceFile == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        popInformation(message("ReadingFile"));
        task = new FxSingletonTask<Void>(this) {

            private String text;

            @Override
            protected boolean handle() {
                text = sourceInformation.readPage(this, page);
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (text != null) {
                    loadText(text, false);
                    if (!sourceInformation.isTotalNumberRead()) {
                        loadTotalNumbers();
                    }
                } else {
                    popFailed();
                }
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                taskCanceled();
            }

        };
        start(task);
    }

    public void loadContents(String contents) {
        createAction();
        mainArea.setText(contents);
        updateInterface(true);
    }

}
