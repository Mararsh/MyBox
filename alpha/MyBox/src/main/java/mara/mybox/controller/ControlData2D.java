package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataClipboard;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.StyleTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2D extends BaseController {

    protected Data2D data2D;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected ControlData2DEditTable tableController;
    protected ControlData2DEditText textController;
    protected final SimpleBooleanProperty statusNotify, loadedNotify, savedNotify;
    protected ControlFileBackup backupController;

    @FXML
    protected Tab editTab, viewTab, attributesTab, columnsTab;
    @FXML
    protected ControlData2DEdit editController;
    @FXML
    protected ControlData2DView viewController;
    @FXML
    protected ControlData2DAttributes attributesController;
    @FXML
    protected ControlData2DColumns columnsController;
    @FXML
    protected FlowPane paginationPane;
    @FXML
    protected ComboBox<String> pageSizeSelector, pageSelector;
    @FXML
    protected Label pageLabel, dataSizeLabel, selectedLabel;
    @FXML
    protected Button functionsButton;

    public ControlData2D() {
        statusNotify = new SimpleBooleanProperty(false);
        loadedNotify = new SimpleBooleanProperty(false);
        savedNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            tableController = editController.tableController;
            textController = editController.textController;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            StyleTools.setIconTooltips(functionsButton, "iconFunction.png", "");
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    /*
        database
     */
    public void setDataType(BaseController parent, Data2D.Type type) {
        try {
            parentController = parent;
            if (parent != null) {
                saveButton = parent.saveButton;
                recoverButton = parent.recoverButton;
                baseTitle = parent.baseTitle;
            }

            editController.setParameters(this);
            viewController.setParameters(this);
            attributesController.setParameters(this);
            columnsController.setParameters(this);

            setData(Data2D.create(type));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setData(Data2D data) {
        try {
            data2D = data;
            data2D.setLoadController(tableController);
            tableData2DDefinition = data2D.getTableData2DDefinition();
            tableData2DColumn = data2D.getTableData2DColumn();

            editController.setData(data2D);
            viewController.setData(data2D);
            attributesController.setData(data2D);
            columnsController.setData(data2D);

            checkStatus();

            switch (data2D.getType()) {
                case CSV:
                case MyBoxClipboard:
                    setFileType(VisitHistory.FileType.CSV);
                    break;
                case Excel:
                    setFileType(VisitHistory.FileType.Excel);
                    break;
                case Texts:
                    setFileType(VisitHistory.FileType.Text);
                    break;
                default:
                    setFileType(VisitHistory.FileType.CSV);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void readDefinition() {
        tableController.readDefinition();
    }

    public void recover() {
        if (data2D.isMatrix()) {
            recoverMatrix();
        } else {
            recoverFile();
        }
    }

    /*
        file
     */
    @Override
    public void sourceFileChanged(File file) {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            data2D.initFile(file);
            readDefinition();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void recoverFile() {
        resetStatus();
        data2D.initFile(data2D.getFile());
        readDefinition();
    }


    /*
        matrix
     */
    public void loadMatrix(double[][] matrix) {
        data2D.initMatrix(matrix);
        readDefinition();
    }

    public void recoverMatrix() {
        resetStatus();
        readDefinition();
    }

    /*
        data
     */
    public void loadDef(Data2DDefinition data) {
        if (!checkBeforeNextAction()) {
            return;
        }
        data2D.initData();
        data2D.cloneAll(data);
        readDefinition();
    }

    public synchronized void loadData() {
        tableController.loadData();
        attributesController.loadData();
        columnsController.loadData();
    }

    public void loadNull() {
        tableController.loadNull();
    }

    public boolean isChanged() {
        return editController.isChanged()
                || attributesController.isChanged()
                || columnsController.isChanged();
    }

    public void notifyStatus() {
        statusNotify.set(!statusNotify.get());
    }

    public void notifyLoaded() {
        if (backupController != null) {
            if (data2D.isTmpData()) {
                backupController.loadBackups(null);
            } else {
                backupController.loadBackups(data2D.getFile());
            }
        }
        loadedNotify.set(!loadedNotify.get());
    }

    public void notifySaved() {
        savedNotify.set(!savedNotify.get());
    }

    public synchronized void checkStatus() {
        String title = message("Table");
        if (data2D != null && data2D.isTableChanged()) {
            title += "*";
        }
        editController.tableTab.setText(title);

        title = message("Text");
        if (textController.status == ControlData2DEditText.Status.Applied) {
            title += "*";
        } else if (textController.status == ControlData2DEditText.Status.Modified) {
            title += "**";
        }
        editController.textTab.setText(title);

        title = message("Edit");
        if (editController.isChanged()) {
            title += "*";
        }
        editTab.setText(title);

        title = message("Attributes");
        if (attributesController.status == ControlData2DAttributes.Status.Applied) {
            title += "*";
        } else if (attributesController.status == ControlData2DAttributes.Status.Modified) {
            title += "**";
        }
        attributesTab.setText(title);

        title = message("Columns");
        if (columnsController.status == ControlData2DColumns.Status.Applied) {
            title += "*";
        } else if (columnsController.status == ControlData2DColumns.Status.Modified) {
            title += "**";
        }
        columnsTab.setText(title);

        if (recoverButton != null) {
            recoverButton.setDisable(data2D == null || data2D.isTmpData());
        }
        if (saveButton != null) {
            saveButton.setDisable(data2D == null || !tableController.dataSizeLoaded);
        }

        notifyStatus();
    }

    public synchronized void resetStatus() {
        if (task != null) {
            task.cancel();
        }
        if (backgroundTask != null) {
            backgroundTask.cancel();
        }

        tableController.resetStatus();

        if (textController.task != null) {
            textController.task.cancel();
        }
        if (textController.backgroundTask != null) {
            textController.backgroundTask.cancel();
        }
        textController.status = null;

        if (attributesController.task != null) {
            attributesController.task.cancel();
        }
        if (attributesController.backgroundTask != null) {
            attributesController.backgroundTask.cancel();
        }
        attributesController.status = null;

        if (columnsController.task != null) {
            columnsController.task.cancel();
        }
        if (columnsController.backgroundTask != null) {
            columnsController.backgroundTask.cancel();
        }
        columnsController.status = null;
    }

    public synchronized int checkBeforeSave() {
        if (!tableController.dataSizeLoaded) {
            popError(message("CountingTotalNumber"));
            return -1;
        }
        if (attributesController.status == ControlData2DAttributes.Status.Modified
                || columnsController.status == ControlData2DColumns.Status.Modified
                || textController.status == ControlData2DEditText.Status.Modified) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setHeaderText(getMyStage().getTitle());
            alert.setContentText(Languages.message("DataModifiedNotApplied"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonApply = new ButtonType(Languages.message("ApplyModificationAndSave"));
            ButtonType buttonDiscard = new ButtonType(Languages.message("DiscardModificationAndSave"));
            ButtonType buttonCancel = new ButtonType(Languages.message("Cancel"));
            alert.getButtonTypes().setAll(buttonApply, buttonDiscard, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonApply) {
                if (textController.status == ControlData2DEditText.Status.Modified) {
                    textController.okAction();
                    if (textController.status != ControlData2DEditText.Status.Applied) {
                        return -2;
                    }
                }
                if (attributesController.status == ControlData2DAttributes.Status.Modified) {
                    attributesController.okAction();
                    if (attributesController.status != ControlData2DAttributes.Status.Applied) {
                        return -3;
                    }
                }
                if (columnsController.status == ControlData2DColumns.Status.Modified) {
                    columnsController.okAction();
                    if (columnsController.status != ControlData2DColumns.Status.Applied) {
                        return -4;
                    }
                }
                return 1;
            } else if (result.get() == buttonDiscard) {
                return 2;
            } else {
                return -5;
            }
        } else {
            return 0;
        }
    }

    public synchronized void save() {
        if (task != null && !task.isQuit()) {
            return;
        }
        if (checkBeforeSave() < 0) {
            return;
        }
        Data2D targetData = data2D.cloneAll();
        if (targetData.isTmpData() && !targetData.isMatrix()) {
            File file = null;
            if (targetData.isDataFile()) {
                file = chooseSaveFile();
            } else if (targetData.isClipboard()) {
                file = DataClipboard.newFile();
            }
            if (file == null) {
                return;
            }
            targetData.setFile(file);
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                if (backupController != null && backupController.isBack() && !data2D.isTmpData()) {
                    backupController.addBackup(task, data2D.getFile());
                }
                try ( Connection conn = DerbyBase.getConnection()) {
                    data2D.setTask(task);
                    data2D.savePageData(targetData);
                    data2D.cloneAll(targetData);
                    data2D.setTask(task);
                    data2D.saveDefinition(conn);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popInformation(message("Saved"));
                if (data2D.getFile() != null) {
                    recordFileWritten(data2D.getFile());
                }
                resetStatus();
                notifySaved();
                readDefinition();
            }

            @Override
            protected void finalAction() {
                data2D.setTask(null);
                task = null;

            }
        };
        start(task);
    }

    public synchronized void saveAs(Data2D targetData, SaveAsType saveAsType) {
        if (targetData == null || targetData.getFile() == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try ( Connection conn = DerbyBase.getConnection()) {
                    data2D.setTask(task);
                    data2D.savePageData(targetData);
                    Data2D.save(conn, targetData, data2D.getColumns());
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popInformation(message("Done"));
                if (targetData.getFile() != null) {
                    recordFileWritten(targetData.getFile());
                }
                if (saveAsType == SaveAsType.Load) {
                    data2D.cloneAll(targetData);
                    resetStatus();
                    readDefinition();
                } else if (saveAsType == SaveAsType.Open) {
                    Data2DDefinition.open(targetData);
                }
            }

            @Override
            protected void finalAction() {
                data2D.setTask(null);
                targetData.setTask(null);
                task = null;
            }
        };
        start(task);
    }

    public void renameAction(BaseTableViewController parent, int index, Data2DDefinition targetData) {
        tableController.renameAction(parent, index, targetData);
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        try {
            if (data2D == null || !checkBeforeNextAction()) {
                return;
            }
            String text = Clipboard.getSystemClipboard().getString();
            if (text == null || text.isBlank()) {
                popError(message("NoTextInClipboard"));
            }
            Data2DLoadContentInSystemClipboardController.open(tableController, text);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void create() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            loadTmpData(data2D.tmpColumns(3), data2D.tmpData(3, 3));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public synchronized void loadTmpData(List<Data2DColumn> cols, List<List<String>> data) {
        tableController.loadTmpData(cols, data);
    }

    /*
        paigination
     */
    @FXML
    public void goPage() {
        tableController.goPage();
    }

    @FXML
    @Override
    public void pageNextAction() {
        tableController.pageNextAction();
    }

    @FXML
    @Override
    public void pagePreviousAction() {
        tableController.pagePreviousAction();
    }

    @FXML
    @Override
    public void pageFirstAction() {
        tableController.pageFirstAction();
    }

    @FXML
    @Override
    public void pageLastAction() {
        tableController.pageLastAction();
    }

    @FXML
    public void refreshAction() {
        goPage();
    }


    /*
        interface
     */
    @FXML
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            boolean invalidData = data2D == null || !data2D.isValid();
            boolean empty = invalidData || tableController.tableData.isEmpty();
            MenuItem menu;

            menu = new MenuItem(message("Save"), StyleTools.getIconImage("iconSave.png"));
            menu.setOnAction((ActionEvent event) -> {
                save();
            });
            menu.setDisable(invalidData || !tableController.dataSizeLoaded);
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Recover"), StyleTools.getIconImage("iconRecover.png"));
            menu.setOnAction((ActionEvent event) -> {
                if (data2D.isMatrix()) {
                    recoverMatrix();
                } else {
                    recoverFile();
                }
            });
            menu.setDisable(invalidData || data2D.isTmpData());
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Refresh"), StyleTools.getIconImage("iconRefresh.png"));
            menu.setOnAction((ActionEvent event) -> {
                refreshAction();
            });
            menu.setDisable(invalidData || data2D.isTmpData());
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("SetValues"), StyleTools.getIconImage("iconEqual.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSetValuesController.open(tableController);
            });
            menu.setDisable(empty);
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Copy"), StyleTools.getIconImage("iconCopy.png"));
            menu.setOnAction((ActionEvent event) -> {
                tableController.copyAction();
            });
            menu.setDisable(empty);
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("PasteContentInSystemClipboard"), StyleTools.getIconImage("iconPasteSystem.png"));
            menu.setOnAction((ActionEvent event) -> {
                tableController.pasteContentInSystemClipboard();
            });
            menu.setDisable(invalidData);
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("PasteContentInMyBoxClipboard"), StyleTools.getIconImage("iconPaste.png"));
            menu.setOnAction((ActionEvent event) -> {
                tableController.pasteContentInMyboxClipboard();
            });
            menu.setDisable(invalidData);
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Sort"), StyleTools.getIconImage("iconSort.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSortController.open(tableController);
            });
            menu.setDisable(empty);
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Statistic"), StyleTools.getIconImage("iconStatistic.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DStatisticController.open(tableController);
            });
            menu.setDisable(empty);
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Percentage"), StyleTools.getIconImage("iconPercentage.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DPercentageController.open(tableController);
            });
            menu.setDisable(empty);
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Normalize"), StyleTools.getIconImage("iconBinary.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DNormalizeController.open(tableController);
            });
            menu.setDisable(empty);
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Transpose"), StyleTools.getIconImage("iconRotateRight.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DTransposeController.open(tableController);
            });
            menu.setDisable(empty);
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Export"), StyleTools.getIconImage("iconExport.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DExportController.open(tableController);
            });
            menu.setDisable(empty);
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            if (data2D.isDataFile()) {
                menu = new MenuItem(message("Open"), StyleTools.getIconImage("iconOpen.png"));
                menu.setOnAction((ActionEvent event) -> {
                    selectSourceFile();
                });
                popMenu.getItems().add(menu);
            }

            menu = new MenuItem(message("CreateData"), StyleTools.getIconImage("iconAdd.png"));
            menu.setOnAction((ActionEvent event) -> {
                create();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("LoadContentInSystemClipboard"), StyleTools.getIconImage("iconImageSystem.png"));
            menu.setOnAction((ActionEvent event) -> {
                loadContentInSystemClipboard();
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"), StyleTools.getIconImage("iconCancel.png"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            if (editTab.isSelected()) {
                return editController.keyEventsFilter(event);

            } else if (viewTab.isSelected()) {
                return viewController.keyEventsFilter(event);

            } else if (attributesTab.isSelected()) {
                return attributesController.keyEventsFilter(event);

            } else if (columnsTab.isSelected()) {
                return columnsController.keyEventsFilter(event);

            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean controlAltC() {
        if (targetIsTextInput()) {
            return false;
        }
        if (editTab.isSelected()) {
            if (editController.tableTab.isSelected()) {
                tableController.copyAction();

            } else if (editController.textTab.isSelected()) {
                TextClipboardTools.copyToMyBoxClipboard(myController, textController.textArea);

            }
            return true;
        }
        return false;
    }

    @Override
    public boolean controlAltV() {
        if (targetIsTextInput()) {
            return false;
        }
        if (editTab.isSelected() && editController.tableTab.isSelected()) {
            Data2DPasteContentInMyBoxClipboardController.open(tableController);
            return true;

        }
        return false;
    }

    @FXML
    @Override
    public void myBoxClipBoard() {
        if (editTab.isSelected() && editController.tableTab.isSelected()) {
            Data2DPasteContentInMyBoxClipboardController.open(tableController);
        } else {
            TextInMyBoxClipboardController.oneOpen();
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        boolean goOn;
        if (!isChanged()) {
            goOn = true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setHeaderText(getMyStage().getTitle());
            alert.setContentText(Languages.message("NeedSaveBeforeAction"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(Languages.message("Save"));
            ButtonType buttonNotSave = new ButtonType(Languages.message("NotSave"));
            ButtonType buttonCancel = new ButtonType(Languages.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                save();
                goOn = false;
            } else {
                goOn = result.get() == buttonNotSave;
            }
        }
        if (goOn) {
            resetStatus();
        }
        return goOn;
    }

    @Override
    public void cleanPane() {
        try {
            tableController = null;
            data2D = null;
            tableData2DDefinition = null;
            tableData2DColumn = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
