package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2DEditTable extends ControlData2DLoad {

    protected SimpleBooleanProperty columnChangedNotify;

    @FXML
    protected Button headerButton, verifyButton;
    @FXML
    protected CheckBox verifyCheck;

    public ControlData2DEditTable() {
        TipsLabelKey = "Data2DTips";
        readOnly = false;
        columnChangedNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(headerButton, new Tooltip(message("FirstLineDefineNames")));
            NodeStyleTools.setTooltip(verifyButton, new Tooltip(message("VerifyPageData")));
            NodeStyleTools.setTooltip(verifyCheck, new Tooltip(message("VerifyDataWhenSave")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void setParameters(ControlData2DEdit editController) {
        try {
            dataController = editController.dataController;
            baseTitle = dataController.baseTitle;
            baseName = dataController.baseName;

            paginationPane = dataController.paginationPane;
            pageSizeSelector = dataController.pageSizeSelector;
            pageSelector = dataController.pageSelector;
            pageLabel = dataController.pageLabel;
            dataSizeLabel = dataController.dataSizeLabel;
            selectedLabel = dataController.selectedLabel;
            pagePreviousButton = dataController.pagePreviousButton;
            pageNextButton = dataController.pageNextButton;
            pageFirstButton = dataController.pageFirstButton;
            pageLastButton = dataController.pageLastButton;
            saveButton = dataController.saveButton;

            initPagination();

            verifyCheck.setSelected(UserConfig.getBoolean("Data2DVerifyBeforeSave", false));
            verifyCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean("Data2DVerifyBeforeSave", verifyCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setData(Data2D data) {
        try {
            super.setData(data);

            headerButton.setDisable(!data2D.isTmpData() && !data2D.isDataFile() && !data.isClipboard());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void notifyColumnChanged() {
        columnChangedNotify.set(!columnChangedNotify.get());
    }

    public void dataSaved() {
        try {
            popInformation(message("Saved"));
            if (data2D.getFile() != null) {
                recordFileWritten(data2D.getFile());
            }
            notifySaved();
            readDefinition();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void headerAction() {
        try {
            if (data2D == null || tableData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            List<String> row = tableData.get(0);
            if (row == null || row.size() < 2) {
                popError(message("InvalidData"));
                return;
            }
            List<String> names = new ArrayList<>();
            for (int i = 1; i < row.size(); i++) {
                String name = row.get(i);
                if (name == null || name.isBlank()) {
                    name = message("Column") + i;
                }
                DerbyBase.checkIdentifier(names, name, true);
            }
            dataController.columnsController.setNames(names);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        table
     */
    @Override
    public synchronized void tableChanged(boolean changed) {
        if (isSettingValues || data2D == null) {
            return;
        }
        data2D.setTableChanged(changed);
        validateData();
        notifyStatus();

        dataController.csvController.loadData();
        dataController.viewController.loadData();
    }

    @Override
    public boolean checkBeforeLoadingTableData() {
        return checkBeforeNextAction();
    }

    @Override
    public boolean checkBeforeNextAction() {
        return dataController.checkBeforeNextAction();
    }

    @Override
    public List<String> newData() {
        return data2D.newRow();
    }

    @Override
    public List<String> dataCopy(List<String> data) {
        return data2D.copyRow(data);
    }

    /*
        action
     */
    @FXML
    @Override
    public void addAction() {
        if (!validateData()) {
            return;
        }
        addRowsAction();
    }

    @FXML
    @Override
    public void addRowsAction() {
        Data2DAddRowsController.open(this);
    }

    @FXML
    @Override
    public void editAction() {
        int index = selectedIndix();
        if (index < 0) {
            return;
        }
        Data2DRowEditController.open(this, index);
    }

    @FXML
    @Override
    public void deleteAction() {
        deleteRowsAction();
    }

    @FXML
    @Override
    public void clearAction() {
        if (data2D.isTmpData()) {
            deleteAllRows();
        } else {
            super.clearAction();
        }
    }

    @Override
    protected long clearData() {
        return data2D.clearData();
    }

    @FXML
    @Override
    public void pasteContentInSystemClipboard() {
        try {
            if (data2D == null) {
                return;
            }
            String text = Clipboard.getSystemClipboard().getString();
            if (text == null || text.isBlank()) {
                popError(message("NoTextInClipboard"));
            }
            Data2DPasteContentInSystemClipboardController.open(this, text);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void pasteContentInMyboxClipboard() {
        try {
            if (data2D == null) {
                return;
            }
            Data2DPasteContentInMyBoxClipboardController.open(this);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void verifyAction() {
        StringTable results = verifyResults();
        if (results.isEmpty()) {
            popInformation(message("AllValuesValid"), 5000);
            return;
        }
        results.htmlTable();
    }

    public StringTable verifyResults() {
        try {
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(message("Row"), message("Column"), message("Invalid")));
            StringTable stringTable = new StringTable(names, data2D.displayName());
            for (int r = 0; r < tableData.size(); r++) {
                List<String> dataRow = tableData.get(r);
                for (int c = 0; c < data2D.columnsNumber(); c++) {
                    Data2DColumn column = data2D.column(c);
                    if (column.isAuto()) {
                        continue;
                    }
                    String value = column.savedValue(dataRow.get(c + 1));
                    String item = null;
                    if (column.isNotNull() && (value == null || value.isBlank())) {
                        item = message("Null");
                    } else if (!column.validValue(value)) {
                        item = message("DataType");
                    } else if (!data2D.validValue(value)) {
                        item = message("TextDataComments");
                    }
                    if (item == null) {
                        continue;
                    }
                    List<String> invalid = new ArrayList<>();
                    invalid.addAll(Arrays.asList((r + 1) + "", column.getColumnName(), item));
                    stringTable.add(invalid);
                }
            }
            return stringTable;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public boolean verifyData() {
        if (!verifyCheck.isSelected()) {
            return true;
        }
        StringTable results = verifyResults();
        if (results.isEmpty()) {
            return true;
        }
        results.htmlTable();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getMyStage().getTitle());
        alert.setHeaderText(getMyStage().getTitle());
        alert.setContentText(message("IgnoreInvalidAndSave"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonSave = new ButtonType(message("Save"));
        ButtonType buttonCancel = new ButtonType(message("Cancel"));
        alert.getButtonTypes().setAll(buttonSave, buttonCancel);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        Optional<ButtonType> result = alert.showAndWait();
        return result != null && result.isPresent() && result.get() == buttonSave;
    }

    @Override
    public void cleanPane() {
        try {
            columnChangedNotify = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
