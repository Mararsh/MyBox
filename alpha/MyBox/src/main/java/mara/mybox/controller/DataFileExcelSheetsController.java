package mara.mybox.controller;

import java.util.List;
import java.util.Random;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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

    protected DataFileExcelController fileController;

    @FXML
    protected ComboBox<String> sheetSelector;
    @FXML
    protected Button plusSheetButton, renameSheetButton, deleteSheetButton,
            nextSheetButton, previousSheetButton;

    public void setParameters(DataFileExcelController parent) {
        try {
            fileController = parent;
            if (fileController == null || fileController.dataFileExcel == null) {
                close();
                return;
            }
            baseName = fileController.baseName;
            setFileType(fileController.TargetFileType);

            fileController.dataController.loadedNotify.addListener(new ChangeListener<Boolean>() {
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
    public void refreshAction() {
        try {
            setTitle(message("Sheet") + " - " + fileController.getTitle());
            sheetSelector.getItems().clear();
            List<String> sheets = fileController.dataFileExcel.getSheetNames();
            int current = -1;
            if (sheets != null && !sheets.isEmpty()) {
                sheetSelector.getItems().addAll(sheets);
                current = sheets.indexOf(fileController.dataFileExcel.getSheet());
            }
            sheetSelector.getSelectionModel().select(fileController.dataFileExcel.getSheet());
            deleteSheetButton.setDisable(sheets == null || sheets.size() <= 1);
            nextSheetButton.setDisable(sheets == null || current >= sheets.size() - 1);
            previousSheetButton.setDisable(current <= 0);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void goAction() {
        loadSheetIndex(sheetSelector.getSelectionModel().getSelectedIndex());
    }

    public void loadSheetIndex(int index) {
        List<String> sheets = sheetSelector.getItems();
        if (index > sheets.size() - 1 || index < 0) {
            return;
        }
        fileController.loadSheetName(sheets.get(index));
    }

    @FXML
    protected void plusSheet() {
        List<String> sheets = fileController.dataFileExcel.getSheetNames();
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
                fileController.dataFileExcel.setTask(this);
                return fileController.dataFileExcel.newSheet(newName);
            }

            @Override
            protected void whenSucceeded() {
                fileController.dataController.readDefinition();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                fileController.dataFileExcel.stopTask();
            }

        };
        start(task);
    }

    @FXML
    protected void renameSheet() {
        if (!fileController.checkBeforeNextAction()) {
            return;
        }
        String currentSheetName = fileController.dataFileExcel.getSheet();
        List<String> sheets = fileController.dataFileExcel.getSheetNames();
        int count = 2;
        String tryName = currentSheetName + "2";
        while (fileController.dataFileExcel.getSheetNames() != null && sheets.contains(tryName)) {
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
                fileController.dataFileExcel.setTask(this);
                return fileController.dataFileExcel.renameSheet(newName);
            }

            @Override
            protected void whenSucceeded() {
                fileController.loadController.updateName();
                fileController.dataController.attributesController.updateDataName();
                refreshAction();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                fileController.dataFileExcel.stopTask();
            }

        };
        fileController.start(fileController.task);
    }

    @FXML
    protected void deleteSheet() {
        List<String> sheets = fileController.dataFileExcel.getSheetNames();
        if (sheets == null || sheets.size() <= 1) {
            return;
        }
        String currentSheetName = fileController.dataFileExcel.getSheet();
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
                index = fileController.dataFileExcel.deleteSheet(currentSheetName);
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
    public static DataFileExcelSheetsController open(DataFileExcelController parent) {
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
