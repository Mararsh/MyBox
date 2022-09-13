package mara.mybox.controller;

import java.io.File;
import java.util.List;
import java.util.Random;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFileExcel;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
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
    public void initData() {
        try {
            setDataType(Data2D.Type.Excel);
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

            dataController.loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    afterFileLoaded();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void pickRefreshOptions() {
        dataFileExcel.setOptions(sourceWithNamesCheck.isSelected());
    }

    @Override
    public Data2D saveAsTarget() {
        File file = chooseSaveFile();
        if (file == null) {
            return null;
        }
        DataFileExcel targetData = new DataFileExcel();
        targetData.setCurrentSheetOnly(currentOnlyCheck.isSelected())
                .initFile(file).setSheet(dataFileExcel.getSheet())
                .setHasHeader(targetWithNamesCheck.isSelected());
        return targetData;
    }

    protected synchronized void afterFileLoaded() {
        sheetSelector.getItems().clear();
        List<String> sheets = dataFileExcel.getSheetNames();
        int current = -1;
        if (sheets != null && !sheets.isEmpty()) {
            sheetSelector.getItems().addAll(sheets);
            current = sheets.indexOf(dataFileExcel.getSheet());
        }
        sheetSelector.getSelectionModel().select(dataFileExcel.getSheet());
        deleteSheetButton.setDisable(sheets == null || sheets.size() <= 1);
        nextSheetButton.setDisable(sheets == null || current >= sheets.size() - 1);
        previousSheetButton.setDisable(current <= 0);
    }

    @Override
    protected void checkStatus() {
        super.checkStatus();
        sheetsPane.setDisable(dataFileExcel.isTmpData() || dataController.isChanged());
    }

    @FXML
    public void loadSheet() {
        loadSheetIndex(sheetSelector.getSelectionModel().getSelectedIndex());
    }

    public void loadSheetIndex(int index) {
        List<String> sheets = sheetSelector.getItems();
        if (index > sheets.size() - 1 || index < 0) {
            return;
        }
        loadSheetName(sheets.get(index));
    }

    public void loadSheetName(String name) {
        try {
            if (!checkBeforeNextAction() || name == null) {
                return;
            }
            dataFileExcel.initFile(dataFileExcel.getFile(), name);
            dataController.readDefinition();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void plusSheet() {
        List<String> sheets = dataFileExcel.getSheetNames();
        if (!checkBeforeNextAction() || sheets == null) {
            return;
        }
        String tryName = message("Sheet") + (sheets.size() + 1);
        Random random = new Random();
        while (sheets.contains(tryName)) {
            tryName += random.nextInt(10);
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
                    return dataFileExcel.newSheet(newName);
                }

                @Override
                protected void whenSucceeded() {
                    dataController.readDefinition();
                }

                @Override
                protected void finalAction() {
                    dataFileExcel.stopTask();
                    task = null;
                }

            };
            start(task);
        }
    }

    @FXML
    protected void renameSheet() {
        if (!checkBeforeNextAction()) {
            return;
        }
        String currentSheetName = dataFileExcel.getSheet();
        List<String> sheets = dataFileExcel.getSheetNames();
        Random random = new Random();
        String tryName = currentSheetName + random.nextInt(10);
        while (dataFileExcel.getSheetNames() != null && sheets.contains(tryName)) {
            tryName += random.nextInt(10);
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
                    afterFileLoaded();
                }

                @Override
                protected void finalAction() {
                    dataFileExcel.stopTask();
                    task = null;
                }

            };
            start(task);
        }
    }

    @FXML
    protected void deleteSheet() {
        List<String> sheets = dataFileExcel.getSheetNames();
        if (sheets == null || sheets.size() <= 1) {
            return;
        }
        String currentSheetName = dataFileExcel.getSheet();
        if (!PopTools.askSure(this, baseTitle, currentSheetName, message("SureDelete"))) {
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
        dataFileExcel.initFile(file);
        dataFileExcel.setOptions(withName);
        dataController.readDefinition();
    }


    /*
        static
     */
    public static DataFileExcelController open(File file, boolean withNames) {
        DataFileExcelController controller = (DataFileExcelController) WindowTools.openStage(Fxmls.DataFileExcelFxml);
        controller.setFile(file, withNames);
        controller.requestMouse();
        return controller;
    }

    public static DataFileExcelController open(String name, List<Data2DColumn> cols, List<List<String>> data) {
        DataFileExcelController controller = (DataFileExcelController) WindowTools.openStage(Fxmls.DataFileExcelFxml);
        controller.dataController.loadTmpData(name, cols, data);
        controller.requestMouse();
        return controller;
    }

    public static DataFileExcelController open(Data2DDefinition def) {
        DataFileExcelController controller = (DataFileExcelController) WindowTools.openStage(Fxmls.DataFileExcelFxml);
        controller.loadDef(def);
        controller.requestMouse();
        return controller;
    }

    public static DataFileExcelController loadCSV(DataFileCSV csvData) {
        DataFileExcelController controller = (DataFileExcelController) WindowTools.openStage(Fxmls.DataFileExcelFxml);
        controller.loadCSVData(csvData);
        controller.requestMouse();
        return controller;
    }

    public static DataFileExcelController loadTable(DataTable dataTable) {
        DataFileExcelController controller = (DataFileExcelController) WindowTools.openStage(Fxmls.DataFileExcelFxml);
        controller.loadTableData(dataTable);
        controller.requestMouse();
        return controller;
    }

}
