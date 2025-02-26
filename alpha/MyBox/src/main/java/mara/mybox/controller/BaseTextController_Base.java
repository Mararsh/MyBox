package mara.mybox.controller;

import java.util.Timer;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import mara.mybox.data.FileEditInformation;
import mara.mybox.data.FileEditInformation.Edit_Type;
import mara.mybox.data.FileEditInformation.Line_Break;
import mara.mybox.data.Pagination;
import mara.mybox.fxml.FxTask;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class BaseTextController_Base extends BaseFileController {

    protected Edit_Type editType;
    protected final SimpleBooleanProperty fileChanged;
    protected FileEditInformation sourceInformation;
    protected Line_Break lineBreak;
    protected int lineBreakWidth;
    protected String lineBreakValue;
    protected long lastPageFrom, lastPageTo;  // 0-based
    protected boolean autoSave;
    protected long autoCheckInterval;
    protected Timer autoCheckTimer;
    protected FxTask pairTask;

    @FXML
    protected TextArea mainArea, lineArea, pairArea;
    @FXML
    protected Label charsetLabel;
    @FXML
    protected FlowPane buttonsPane;

    public BaseTextController_Base() {
        fileChanged = new SimpleBooleanProperty(false);
        pagination = new Pagination();
    }

    protected abstract void updateInterface(boolean changed);

    @Override
    public void taskCanceled(Task task) {
        taskCanceled();
    }

    @Override
    public void taskCanceled() {
        if (backgroundTask != null && !backgroundTask.isQuit()) {
            backgroundTask.cancel();
            backgroundTask = null;
        }
    }
}
