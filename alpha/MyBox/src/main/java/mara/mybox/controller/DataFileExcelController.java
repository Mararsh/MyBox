package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataFile;
import mara.mybox.data.DataFileExcel;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-1-17
 * @License Apache License Version 2.0
 */
public class DataFileExcelController extends BaseData2DFileController {

    protected DataFileExcel dataFileExcel;

    @FXML
    protected TitledPane sheetsPane;
    @FXML
    protected ComboBox<String> sheetSelector;
    @FXML
    protected CheckBox sourceWithNamesCheck, targetWithNamesCheck, currentOnlyCheck;
    @FXML
    protected Button okSheetButton, plusSheetButton, renameSheetButton, deleteSheetButton,
            nextSheetButton, previousSheetButton;
    @FXML
    protected VBox sheetsBox;

    public DataFileExcelController() {
        baseTitle = message("EditExcel");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            setDataType(Data2D.Type.DataFileExcel);
            dataFileExcel = (DataFileExcel) dataController.data2D;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Excel);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            sourceWithNamesCheck.setSelected(UserConfig.getBoolean(baseName + "SourceWithNames", true));
            sourceWithNamesCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                if (!isSettingValues) {
                    UserConfig.setBoolean(baseName + "SourceWithNames", newValue);
                }
            });

            targetWithNamesCheck.setSelected(UserConfig.getBoolean(baseName + "TargetWithNames", true));
            targetWithNamesCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                if (!isSettingValues) {
                    UserConfig.setBoolean(baseName + "TargetWithNames", newValue);
                }
            });

            currentOnlyCheck.setSelected(UserConfig.getBoolean(baseName + "CurrentOnly", false));
            currentOnlyCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                if (!isSettingValues) {
                    UserConfig.setBoolean(baseName + "CurrentOnly", newValue);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initFile(File file) {
        File cfile = dataFileExcel.getFile();
        String sheet = dataFileExcel.getCurrentSheetName();
        List<String> sheetNames = dataFileExcel.getSheetNames();
        dataFileExcel.initFile(file);
        if (file != null && file.equals(cfile)) {
            dataFileExcel.setCurrentSheetName(sheet);
            dataFileExcel.setSheetNames(sheetNames);
        } else {
            dataFileExcel.setCurrentSheetName(null);
            dataFileExcel.setSheetNames(null);
        }
    }

    @Override
    public void pickOptions() {
        dataFileExcel.setHasHeader(sourceWithNamesCheck.isSelected());
    }

    @Override
    protected void afterFileLoaded() {
        super.afterFileLoaded();
        sheetSelector.getItems().clear();
        List<String> sheets = dataFileExcel.getSheetNames();
        sheetSelector.getItems().setAll(sheets);
        sheetSelector.setValue(dataFileExcel.getCurrentSheetName());
        deleteSheetButton.setDisable(sheets == null || sheets.size() <= 1);
        int current = sheetSelector.getSelectionModel().getSelectedIndex();
        nextSheetButton.setDisable(sheets == null || current >= sheets.size() - 1);
        previousSheetButton.setDisable(current <= 0);
        sheetsPane.setExpanded(true);
    }

    @Override
    protected void updateStatus() {
        super.updateStatus();
        if (dataFileExcel.isTmpFile() || dataController.isChanged()) {
            sheetsBox.setDisable(true);
        } else {
            sheetsBox.setDisable(false);
        }
    }

    @Override
    protected void updateInfoLabel() {
        String info = "";
        if (sourceFile != null) {
            info = message("FileSize") + ": " + FileTools.showFileSize(sourceFile.length()) + "\n"
                    + message("FileModifyTime") + ": " + DateTools.datetimeToString(sourceFile.lastModified()) + "\n"
                    + message("CurrentSheet") + ": " + (dataFileExcel.getCurrentSheetName() == null ? "" : dataFileExcel.getCurrentSheetName() + "\n")
                    + message("FirstLineAsNames") + ": " + (dataFileExcel.isHasHeader() ? message("Yes") : message("No")) + "\n";
        }
        if (!dataFileExcel.isMutiplePages()) {
            info += message("RowsNumber") + ":" + dataFileExcel.tableRowsNumber() + "\n";
        } else {
            info += message("LinesNumberInFile") + ":" + dataFileExcel.getDataSize() + "\n";
        }
        info += message("ColumnsNumber") + ": " + dataFileExcel.columnsNumber() + "\n"
                + message("CurrentPage") + ": " + StringTools.format(dataFileExcel.getCurrentPage() + 1)
                + " / " + StringTools.format(dataFileExcel.getPagesNumber()) + "\n";
        if (dataFileExcel.isMutiplePages() && dataFileExcel.hasData()) {
            info += message("RowsRangeInPage")
                    + ": " + StringTools.format(dataFileExcel.getStartRowOfCurrentPage() + 1) + " - "
                    + StringTools.format(dataFileExcel.getStartRowOfCurrentPage() + dataFileExcel.tableRowsNumber())
                    + " ( " + StringTools.format(dataFileExcel.tableRowsNumber()) + " )\n";
        }
        info += message("PageModifyTime") + ": " + DateTools.nowString();
        fileInfoLabel.setText(info);
    }

    @Override
    public DataFile makeTargetDataFile(File file) {
        DataFileExcel targetCSVFile = (DataFileExcel) dataFileExcel.cloneAll();
        targetCSVFile.setFile(file);
        targetCSVFile.setD2did(-1);
        targetCSVFile.setHasHeader(targetWithNamesCheck.isSelected());
        targetCSVFile.setCurrentSheetOnly(currentOnlyCheck.isSelected());
        return targetCSVFile;
    }

    @FXML
    public void loadSheet() {
        loadSheetIndex(sheetSelector.getSelectionModel().getSelectedIndex());
    }

    public void loadSheetIndex(int index) {
        if (sourceFile == null || !checkBeforeNextAction()) {
            return;
        }
        List<String> sheets = sheetSelector.getItems();
        if (index > sheets.size() - 1 || index < 0) {
            return;
        }
        loadSheetName(sheets.get(index));
    }

    public void loadSheetName(String name) {
        if (sourceFile == null || !checkBeforeNextAction() || name == null) {
            return;
        }
        dataFileExcel.initFile(sourceFile);
        dataFileExcel.setCurrentSheetName(name);
        dataFileExcel.setUserSavedDataDefinition(true);
        loadFile();
    }

    @FXML
    protected void plusSheet() {
        List<String> sheets = dataFileExcel.getSheetNames();
        if (sourceFile == null || !checkBeforeNextAction() || sheets == null) {
            return;
        }
        String tryName = message("Sheet") + (sheets.size() + 1);
        while (sheets.contains(tryName)) {
            tryName += "m";
        }
        String newName = PopTools.askValue(null, message("Create"), message("SheetName"), tryName);
        if (newName == null || newName.isBlank()) {
            popError(message("InvalidData"));
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    dataFileExcel.setTask(task);
                    return dataFileExcel.newSheet(newName, true);
                }

                @Override
                protected void whenSucceeded() {
                    dataController.loadDefinition();
                }

                @Override
                protected void finalAction() {
                    dataFileExcel.setTask(null);
                    task = null;
                }

            };
            start(task);
        }
    }

    @FXML
    protected void renameSheet() {
        if (sourceFile == null || !checkBeforeNextAction()) {
            return;
        }
        String currentSheetName = dataFileExcel.getCurrentSheetName();
        List<String> sheets = dataFileExcel.getSheetNames();
        String tryName = currentSheetName + "m";
        while (dataFileExcel.getSheetNames() != null && sheets.contains(tryName)) {
            tryName += "m";
        }
        String newName = PopTools.askValue(null, message("CurrentName") + ": " + currentSheetName, message("NewName"), tryName);
        if (newName == null || newName.isBlank() || newName.equals(currentSheetName)
                || (sheets != null && sheets.contains(newName))) {
            popError(message("InvalidData"));
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    dataFileExcel.setTask(task);
                    return dataFileExcel.renameSheet(newName);
                }

                @Override
                protected void whenSucceeded() {
                    dataController.loadDefinition();
                }

                @Override
                protected void finalAction() {
                    dataFileExcel.setTask(null);
                    task = null;
                }

            };
            start(task);
        }
    }

    @FXML
    protected void deleteSheet() {
        List<String> sheets = dataFileExcel.getSheetNames();
        if (sourceFile == null || sheets == null || sheets.size() <= 1) {
            return;
        }
        String currentSheetName = dataFileExcel.getCurrentSheetName();
        if (!PopTools.askSure(baseTitle, currentSheetName, message("SureDelete"))) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                private int index;

                @Override
                protected boolean handle() {
                    index = dataFileExcel.deleteSheet(currentSheetName);
                    return index >= 0;
                }

                @Override
                protected void whenSucceeded() {
                    if (sheets == null || index >= sheets.size() - 1) {
                        loadSheetIndex(0);
                    } else {
                        loadSheetIndex(index + 1);
                    }
                }

            };
            start(task);
        }
    }

    @FXML
    public void nextSheetAction() {
        int current = sheetSelector.getSelectionModel().getSelectedIndex();
        if (current >= sheetSelector.getItems().size() - 1) {
            popError(message("NoMore"));
            return;
        }
        loadSheetIndex(current + 1);
    }

    @FXML
    public void previousSheetAction() {
        int current = sheetSelector.getSelectionModel().getSelectedIndex();
        if (current == 0) {
            popError(message("NoMore"));
            return;
        }
        loadSheetIndex(current - 1);
    }

    public void setFile(File file, boolean withName) {
        if (file == null || !checkBeforeNextAction()) {
            return;
        }
        sourceFile = file;
        dataFileExcel.initFile(sourceFile);
        dataFileExcel.setHasHeader(withName);
        loadFile();
    }


    /*
        static
     */
    public static DataFileExcelController oneOpen() {
        DataFileExcelController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof DataFileExcelController) {
                try {
                    controller = (DataFileExcelController) object;
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (DataFileExcelController) WindowTools.openStage(Fxmls.DataFileExcelFxml);
        }
        return controller;
    }

    public static DataFileExcelController open(File file, boolean withNames) {
        DataFileExcelController controller = (DataFileExcelController) WindowTools.openStage(Fxmls.DataFileExcelFxml);
        controller.setFile(file, withNames);
        controller.toFront();
        return controller;
    }

}
