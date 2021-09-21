package mara.mybox.controller;

import java.io.File;
import java.io.FileOutputStream;
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
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * @Author Mara
 * @CreateDate 2021-1-17
 * @License Apache License Version 2.0
 */
public class DataFileExcelController extends BaseDataFileController {

    @FXML
    protected TitledPane sheetsPane;
    @FXML
    protected ComboBox<String> sheetSelector;
    @FXML
    protected CheckBox sourceWithNamesCheck, targetWithNamesCheck, currentOnlyCheck;
    @FXML
    protected Button okSheetButton, plusSheetButton, renameSheetButton, deleteSheetButton2;
    @FXML
    protected VBox sheetsBox;
    @FXML
    protected ControlSheetExcel sheetController;

    public DataFileExcelController() {
        baseTitle = message("EditExcel");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Excel);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            dataController = sheetController;
            dataController.setParent(this);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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
    public void pickOptions() {
        sheetController.sourceWithNames = sourceWithNamesCheck.isSelected();
    }

    public void setFile(File file, boolean withName) {
        sourceFile = file;
        sourceWithNamesCheck.setSelected(withName);
        sheetController.initCurrentPage();
        sheetController.userSavedDataDefinition = false;
        loadFile();
    }

    @Override
    protected void fileLoaded() {
        sheetSelector.getItems().clear();
        if (sheetController.sheetNames != null) {
            sheetSelector.getItems().setAll(sheetController.sheetNames);
        }
        sheetSelector.setValue(sheetController.currentSheetName);
        deleteSheetButton2.setDisable(sheetController.sheetNames == null || sheetController.sheetNames.size() <= 1);
        int current = sheetSelector.getSelectionModel().getSelectedIndex();
        nextButton.setDisable(current >= sheetSelector.getItems().size() - 1);
        previousButton.setDisable(current <= 0);
        sheetsPane.setExpanded(true);
        updateStatus();
    }

    @Override
    protected void updateInfoLabel() {
        String info = "";
        if (sourceFile != null) {
            info = message("FileSize") + ": " + FileTools.showFileSize(sourceFile.length()) + "\n"
                    + message("FileModifyTime") + ": " + DateTools.datetimeToString(sourceFile.lastModified()) + "\n"
                    + message("CurrentSheet") + ": " + (sheetController.currentSheetName == null ? "" : sheetController.currentSheetName + "\n")
                    + message("FirstLineAsNames") + ": " + (sheetController.sourceWithNames ? message("Yes") : message("No")) + "\n";
        }
        if (sheetController.pagesNumber <= 1) {
            info += message("RowsNumber") + ":" + (sheetController.sheetInputs == null ? 0 : sheetController.sheetInputs.length) + "\n";
        } else {
            info += message("LinesNumberInFile") + ":" + sheetController.totalSize + "\n";
        }
        info += message("ColumnsNumber") + ": " + (sheetController.columns == null ? "0" : sheetController.columns.size()) + "\n"
                + message("CurrentPage") + ": " + StringTools.format(sheetController.currentPage)
                + " / " + StringTools.format(sheetController.pagesNumber) + "\n";
        if (sheetController.pagesNumber > 1 && sheetController.sheetInputs != null) {
            info += message("RowsRangeInPage")
                    + ": " + StringTools.format(sheetController.currentPageStart) + " - "
                    + StringTools.format(sheetController.currentPageStart + sheetController.sheetInputs.length - 1)
                    + " ( " + StringTools.format(sheetController.sheetInputs.length) + " )\n";
        }
        info += message("PageModifyTime") + ": " + DateTools.nowString();
        fileInfoLabel.setText(info);
    }

    @FXML
    protected void plusSheet() {
        if (sourceFile == null || sheetController.sheetNames == null || !checkBeforeNextAction()) {
            return;
        }
        String newName = message("Sheet") + (sheetController.sheetNames.size() + 1);
        while (sheetController.sheetNames != null && sheetController.sheetNames.contains(newName)) {
            newName += "m";
        }
        String value = PopTools.askValue(null, message("Create"), message("SheetName"), newName);
        if (value == null || value.isBlank()) {
            popError(message("InvalidData"));
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    File tmpFile = TmpFileTools.getTempFile();
                    File tmpDataFile = TmpFileTools.getTempFile();
                    FileCopyTools.copyFile(sourceFile, tmpDataFile);
                    try ( Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
                        targetBook.createSheet(value);

                        try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                            targetBook.write(fileOut);
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    FileDeleteTools.delete(tmpDataFile);
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    sheetController.loadSheet(value);
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
        String newName = sheetController.currentSheetName + "m";
        while (sheetController.sheetNames != null && sheetController.sheetNames.contains(newName)) {
            newName += "m";
        }
        String value = PopTools.askValue(null, message("CurrentName") + ": " + sheetController.currentSheetName, message("NewName"), newName);
        if (value == null || value.isBlank() || value.equals(sheetController.currentSheetName)
                || (sheetController.sheetNames != null && sheetController.sheetNames.contains(value))) {
            popError(message("InvalidData"));
            return;
        }
        sheetController.targetSheetName = value;
        sheetController.saveFile();
    }

    @FXML
    protected void deleteSheet() {
        if (sourceFile == null || sheetController.sheetNames == null || sheetController.sheetNames.size() <= 1) {
            return;
        }
        if (!PopTools.askSure(baseTitle, sheetController.currentSheetName, message("SureDelete"))) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private int index;

                @Override
                protected boolean handle() {
                    File tmpFile = TmpFileTools.getTempFile();
                    File tmpDataFile = TmpFileTools.getTempFile();
                    FileCopyTools.copyFile(sourceFile, tmpDataFile);
                    try ( Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
                        index = targetBook.getSheetIndex(sheetController.currentSheetName);
                        targetBook.removeSheetAt(index);

                        try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                            targetBook.write(fileOut);
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    FileDeleteTools.delete(tmpDataFile);
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    if (sheetController.sheetNames == null || index >= sheetController.sheetNames.size() - 1) {
                        loadSheet(0);
                    } else {
                        loadSheet(index + 1);
                    }
                }

            };
            start(task);
        }
    }

    @FXML
    @Override
    public void nextAction() {
        int current = sheetSelector.getSelectionModel().getSelectedIndex();
        if (current >= sheetSelector.getItems().size() - 1) {
            popError(message("NoMore"));
            return;
        }
        loadSheet(current + 1);
    }

    @FXML
    @Override
    public void previousAction() {
        int current = sheetSelector.getSelectionModel().getSelectedIndex();
        if (current == 0) {
            popError(message("NoMore"));
            return;
        }
        loadSheet(current - 1);
    }

    @FXML
    public void loadSheet() {
        loadSheet(sheetSelector.getSelectionModel().getSelectedIndex());
    }

    @FXML
    public void loadSheet(int index) {
        if (sourceFile == null || !checkBeforeNextAction()) {
            return;
        }
        if (index > sheetSelector.getItems().size() - 1 || index < 0) {
            return;
        }
        String name = sheetSelector.getItems().get(index);
        sheetSelector.getSelectionModel().select(name);
        sheetController.loadSheet(name);

        nextButton.setDisable(index >= sheetSelector.getItems().size() - 1);
        previousButton.setDisable(index <= 0);
    }

    @FXML
    @Override
    public void saveAsAction() {
        sheetController.sourceFile = sourceFile;
        sheetController.targetWithNames = targetWithNamesCheck.isSelected();
        sheetController.currentSheetOnly = currentOnlyCheck.isSelected();
        sheetController.saveAsType = saveAsType;
        sheetController.saveAs();
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
