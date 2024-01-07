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
import mara.mybox.data.LongRange;
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
    protected String filterConditionsString = "";
    protected Line_Break lineBreak;
    protected int defaultPageSize, lineBreakWidth;
    protected long lastPageFrom, lastPageTo, locateLine, locateObject;  // 0-based
    protected String lineBreakValue;
    protected Timer autoSaveTimer;
    protected LongRange linesRange, objectsRange;
    protected FxTask pairTask;

    protected enum Action {
        OpenFile, LoadPage, FindReplace, FindFirst, FindNext, FindPrevious, FindLast, Replace, ReplaceAll,
        LocateLine, LocateObject, Unknown
    }

    @FXML
    protected TitledPane infoPane, formatPane, savePane, saveAsPane, findPane, filterPane, locatePane, backupPane;
    @FXML
    protected TextArea mainArea, lineArea, pairArea;
    @FXML
    protected ComboBox<String> charsetSelector, targetCharsetSelector, pageSelector, pageSizeSelector;
    @FXML
    protected ToggleGroup lineBreakGroup;
    @FXML
    protected CheckBox targetBomCheck, autoSaveCheck;
    @FXML
    protected ControlTimeLength autoSaveDurationController;
    @FXML
    protected Label bomLabel, pageLabel, charsetLabel, selectionLabel,
            filterConditionsLabel;
    @FXML
    protected Button charactersButton, linesButton, exampleFilterButton,
            filterButton, goObjectButton, goLineButton, goLinesRangeButton, goObjectsRangeButton;
    @FXML
    protected TextField objectNumberInput, lineInput, lineFromInput, lineToInput, objectFromInput, objectToInput;
    @FXML
    protected RadioButton crlfRadio, lfRadio, crRadio;
    @FXML
    protected HBox pageBox;
    @FXML
    protected ControlFindReplace findReplaceController;
    @FXML
    protected ControlTextFilter filterController;

    public BaseTextController_Base() {
        fileChanged = new SimpleBooleanProperty(false);
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
