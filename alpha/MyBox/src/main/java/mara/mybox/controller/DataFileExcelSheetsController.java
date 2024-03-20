package mara.mybox.controller;

import java.io.File;
import java.util.List;
import java.util.Random;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import mara.mybox.data2d.DataFileExcel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-7-19
 * @License Apache License Version 2.0
 */
public class DataFileExcelSheetsController extends BaseChildController {

    protected BaseData2DLoadController fileController;

    @FXML
    protected ComboBox<String> sheetSelector;
    @FXML
    protected Button plusSheetButton, renameSheetButton, deleteSheetButton,
            nextSheetButton, previousSheetButton;

    public void setParameters(BaseData2DLoadController parent) {
        try {
            fileController = parent;
            if (fileController == null
                    || fileController.data2D == null
                    || !(fileController.data2D instanceof DataFileExcel)) {
                close();
                return;
            }

            baseName = fileController.baseName;
            setFileType(fileController.TargetFileType);

            sheetSelector.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> v, Number oldV, Number newV) {
                    if (isSettingValues || newV == null) {
                        return;
                    }
                    loadSheetIndex((int) newV);
                }
            });

            fileController.loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    refreshAction();
                }
            });

            refreshAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public synchronized void refreshAction() {
        try {
            isSettingValues = true;
            DataFileExcel dataFileExcel = (DataFileExcel) fileController.data2D;
            setTitle(message("Sheet") + " - " + fileController.getTitle());
            List<String> sheets = dataFileExcel.getSheetNames();
            String sheet = dataFileExcel.getSheet();
            int index = -1;
            int num = sheets != null ? sheets.size() : 0;
            if (num > 0) {
                sheetSelector.getItems().setAll(sheets);
                index = sheets.indexOf(sheet);
            } else {
                sheetSelector.getItems().clear();
            }
            sheetSelector.getSelectionModel().select(sheet);
            deleteSheetButton.setDisable(num <= 1);
            nextSheetButton.setDisable(num < 1 || index >= num - 1);
            previousSheetButton.setDisable(index <= 0);
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadSheetIndex(int index) {
        if (fileController == null || !fileController.isShowing()) {
            close();
            return;
        }
        if (!fileController.checkBeforeNextAction()) {
            return;
        }
        File file = fileController.data2D.getFile();
        if (file == null || !file.exists()) {
            close();
            return;
        }
        List<String> sheets = sheetSelector.getItems();
        if (index < 0) {
            index = sheets.size() - 1;
        } else if (index > sheets.size() - 1) {
            index = 0;
        }
        fileController.loadExcelFile(file, sheets.get(index),
                fileController.data2D.isHasHeader());
    }

    @FXML
    protected void plusSheet() {
        if (fileController == null || !fileController.isShowing()) {
            close();
            return;
        }
        DataFileExcel dataFileExcel = (DataFileExcel) fileController.data2D;
        List<String> sheets = dataFileExcel.getSheetNames();
        if (sheets == null) {
            return;
        }
        if (!fileController.checkBeforeNextAction()) {
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
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                dataFileExcel.setTask(this);
                return dataFileExcel.newSheet(newName);
            }

            @Override
            protected void whenSucceeded() {
                fileController.readDefinition();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                dataFileExcel.stopTask();
            }

        };
        start(task);
    }

    @FXML
    protected void renameSheet() {
        if (fileController == null || !fileController.isShowing()) {
            close();
            return;
        }
        if (!fileController.checkBeforeNextAction()) {
            return;
        }
        DataFileExcel dataFileExcel = (DataFileExcel) fileController.data2D;
        String currentSheetName = dataFileExcel.getSheet();
        List<String> sheets = dataFileExcel.getSheetNames();
        int count = 2;
        String tryName = currentSheetName + "2";
        while (dataFileExcel.getSheetNames() != null && sheets.contains(tryName)) {
            tryName = currentSheetName + ++count;
        }
        String newName = PopTools.askValue(null, message("CurrentName") + ": " + currentSheetName, message("NewName"), tryName);
        if (newName == null || newName.isBlank() || newName.equals(currentSheetName)
                || (sheets != null && sheets.contains(newName))) {
            popError(message("InvalidData"));
            return;
        }
        if (fileController.task != null) {
            fileController.task.cancel();
        }
        fileController.task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                dataFileExcel.setTask(this);
                return dataFileExcel.renameSheet(newName);
            }

            @Override
            protected void whenSucceeded() {
                fileController.updateTitle();
                refreshAction();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                dataFileExcel.stopTask();
            }

        };
        fileController.start(fileController.task);
    }

    @FXML
    protected void deleteSheet() {
        if (fileController == null || !fileController.isShowing()) {
            close();
            return;
        }
        DataFileExcel dataFileExcel = (DataFileExcel) fileController.data2D;
        List<String> sheets = dataFileExcel.getSheetNames();
        if (sheets == null || sheets.size() <= 1) {
            return;
        }
        String currentSheetName = dataFileExcel.getSheet();
        if (!PopTools.askSure(getTitle(), currentSheetName, message("SureDelete"))) {
            return;
        }
        if (fileController.task != null) {
            fileController.task.cancel();
        }
        fileController.task = new FxSingletonTask<Void>(this) {
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
        fileController.start(fileController.task);
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


    /*
        static methods
     */
    public static DataFileExcelSheetsController open(BaseData2DLoadController parent) {
        try {
            if (parent == null) {
                return null;
            }
            DataFileExcelSheetsController controller = (DataFileExcelSheetsController) WindowTools.branchStage(
                    parent, Fxmls.DataFileExcelSheetsFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
