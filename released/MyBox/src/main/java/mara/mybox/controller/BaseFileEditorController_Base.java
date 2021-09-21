package mara.mybox.controller;

import java.util.Timer;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import mara.mybox.data.FileEditInformation;
import mara.mybox.data.FileEditInformation.Edit_Type;
import mara.mybox.data.FileEditInformation.Line_Break;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class BaseFileEditorController_Base extends BaseController {

    protected Edit_Type editType;
    protected long lineLocation, objectLocation;
    protected SimpleBooleanProperty fileChanged;
    protected FileEditInformation sourceInformation;
    protected String filterConditionsString = "";
    protected Line_Break lineBreak;
    protected int defaultPageSize, lineBreakWidth, lastCursor, lastCaret, currentLine;
    protected double lastScrollTop, lastScrollLeft;
    protected String lineBreakValue;
    protected Timer autoSaveTimer;

    protected enum Action {
        None, FindFirst, FindNext, FindPrevious, FindLast, Replace, ReplaceAll,
        Filter, SetPageSize, NextPage, PreviousPage, FirstPage, LastPage, GoPage
    }

    @FXML
    protected TitledPane filePane, formatPane, savePane, saveAsPane, findPane, filterPane, locatePane, backupPane;
    @FXML
    protected TextArea mainArea, lineArea, pairArea;
    @FXML
    protected ComboBox<String> charsetSelector, targetCharsetSelector, pageSelector, pageSizeSelector;
    @FXML
    protected ToggleGroup lineBreakGroup;
    @FXML
    protected CheckBox targetBomCheck, confirmCheck, autoSaveCheck;
    @FXML
    protected ControlTimeLength autoSaveDurationController;
    @FXML
    protected Label editLabel, bomLabel, fileLabel, pageLabel, charsetLabel, selectionLabel,
            filterConditionsLabel;
    @FXML
    protected Button panesMenuButton, charactersButton, linesButton, exampleFilterButton,
            filterButton, locateObjectButton, locateLineButton;
    @FXML
    protected TextField fromInput, toInput, currentLineBreak, objectNumberInput, lineInput;
    @FXML
    protected RadioButton crlfRadio, lfRadio, crRadio;
    @FXML
    protected HBox pageBox;
    @FXML
    protected ControlFindReplace findReplaceController;
    @FXML
    protected ControlTextFilter filterController;
    @FXML
    protected ControlFileBackup backupController;

    public BaseFileEditorController_Base() {
        defaultPageSize = 50000;
    }

    protected abstract void updateInterface(boolean changed);

    @Override
    public void taskCanceled(Task task) {
        taskCanceled();
    }

    public void taskCanceled() {
        bottomLabel.setText("");
        if (backgroundTask != null && !backgroundTask.isQuit()) {
            backgroundTask.cancel();
            backgroundTask = null;
        }
    }
}
