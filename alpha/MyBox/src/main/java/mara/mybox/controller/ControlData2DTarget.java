package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-11
 * @License Apache License Version 2.0
 */
public class ControlData2DTarget extends BaseController {

    protected BaseData2DLoadController tableController;
    protected TargetType target;
    protected boolean notInTable;
    protected ChangeListener<Boolean> tableStatusListener;
    protected SimpleBooleanProperty formatNotify;

    @FXML
    protected ToggleGroup targetGroup;
    @FXML
    protected RadioButton csvRadio, excelRadio, textsRadio, matrixRadio, databaseRadio,
            jsonRadio, xmlRadio, htmlRadio, pdfRadio,
            systemClipboardRadio, myBoxClipboardRadio, replaceRadio, insertRadio, appendRadio;
    @FXML
    protected ComboBox<String> rowSelector, colSelector;
    @FXML
    protected VBox externalBox, externalDefBox, fileBox, inTableBox;
    @FXML
    protected HBox dataNameBox, locationBox;
    @FXML
    protected TextField nameInput;

    public void setParameters(BaseController parent, BaseData2DLoadController controller) {
        try {
            baseName = parent.baseName + "_" + baseName;
            tableController = controller;
            formatNotify = new SimpleBooleanProperty(false);

            checkControls();
            targetGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkTarget();
                }
            });

            if (tableController != null) {
                refreshControls();
                tableStatusListener = new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        refreshControls();
                    }
                };
                tableController.statusNotify.addListener(tableStatusListener);
            }
            initTarget();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void checkControls() {
        if (inTableBox != null) {
            if (notInTable) {
                if (thisPane.getChildren().contains(inTableBox)) {
                    thisPane.getChildren().remove(inTableBox);
                }
            } else {
                if (!thisPane.getChildren().contains(inTableBox)) {
                    thisPane.getChildren().add(2, inTableBox);
                }
            }
        }
        if (inTable()) {
            if (!inTableBox.getChildren().contains(locationBox)) {
                inTableBox.getChildren().add(locationBox);
            }
            if (externalBox != null) {
                if (externalBox.getChildren().contains(externalDefBox)) {
                    externalBox.getChildren().remove(externalDefBox);
                }
            }
        } else {
            if (inTableBox != null && inTableBox.getChildren().contains(locationBox)) {
                inTableBox.getChildren().remove(locationBox);
            }
            if (externalBox != null) {
                if (externalBox.getChildren().contains(externalDefBox)) {
                    externalBox.getChildren().remove(externalDefBox);
                }
            }
            if (matrixRadio.isSelected()
                    || systemClipboardRadio.isSelected()
                    || myBoxClipboardRadio.isSelected()
                    || databaseRadio.isSelected()) {
                if (externalDefBox.getChildren().contains(fileBox)) {
                    externalDefBox.getChildren().remove(fileBox);
                }
            } else {
                if (!externalDefBox.getChildren().contains(fileBox)) {
                    externalDefBox.getChildren().add(fileBox);
                }
            }
        }
    }

    public void initTarget() {
        try {
            target = TargetType.valueOf(UserConfig.getString(baseName + "DataTarget", "CSV"));
            isSettingValues = true;
            if (target == null) {
                csvRadio.setSelected(true);
            } else {
                switch (target) {
                    case CSV:
                        csvRadio.setSelected(true);
                        break;
                    case Excel:
                        excelRadio.setSelected(true);
                        break;
                    case Text:
                        textsRadio.setSelected(true);
                        break;
                    case Matrix:
                        matrixRadio.setSelected(true);
                        break;
                    case SystemClipboard:
                        systemClipboardRadio.setSelected(true);
                        break;
                    case MyBoxClipboard:
                        myBoxClipboardRadio.setSelected(true);
                        break;
                    case DatabaseTable:
                        databaseRadio.setSelected(true);
                        break;
                    case JSON:
                        jsonRadio.setSelected(true);
                        break;
                    case XML:
                        xmlRadio.setSelected(true);
                        break;
                    case HTML:
                        htmlRadio.setSelected(true);
                        break;
                    case PDF:
                        pdfRadio.setSelected(true);
                        break;
                    case Append:
                        if (notInTable && inTableBox != null) {
                            appendRadio.setSelected(true);
                        } else {
                            csvRadio.setSelected(true);
                        }
                        break;
                    case Insert:
                        if (notInTable && inTableBox != null) {
                            insertRadio.setSelected(true);
                        } else {
                            csvRadio.setSelected(true);
                        }
                        break;
                    case Replace:
                        if (notInTable && inTableBox != null) {
                            replaceRadio.setSelected(true);
                        } else {
                            csvRadio.setSelected(true);
                        }
                        break;
                    default:
                        csvRadio.setSelected(true);
                }
            }
            isSettingValues = false;
            checkTarget();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public TargetType checkTarget() {
        try {
            target = TargetType.CSV;
            if (isSettingValues || tableController == null || tableController.data2D == null) {
                return target;
            }
            String name = name();
            if (name == null || name.isBlank()) {
                name = tableController.data2D.getDataName();
            }
            if (name == null || name.isBlank()) {
                name = tableController.getTitle();
            }
            if (csvRadio.isSelected()) {
                target = TargetType.CSV;
                targetFileController.setFile(FileType.CSV,
                        baseName + "TargetType" + FileType.CSV, name, "csv");
            } else if (excelRadio.isSelected()) {
                target = TargetType.Excel;
                targetFileController.setFile(FileType.Excel,
                        baseName + "TargetType" + FileType.Excel, name, "xlsx");
            } else if (textsRadio.isSelected()) {
                target = TargetType.Text;
                targetFileController.setFile(FileType.Text,
                        baseName + "TargetType" + FileType.Text, name, "txt");
            } else if (matrixRadio.isSelected()) {
                target = TargetType.Matrix;
            } else if (systemClipboardRadio.isSelected()) {
                target = TargetType.SystemClipboard;
            } else if (myBoxClipboardRadio.isSelected()) {
                target = TargetType.MyBoxClipboard;
            } else if (databaseRadio.isSelected()) {
                target = TargetType.DatabaseTable;
            } else if (jsonRadio.isSelected()) {
                target = TargetType.JSON;
                targetFileController.setFile(FileType.JSON,
                        baseName + "TargetType" + FileType.JSON, name, "json");
            } else if (xmlRadio.isSelected()) {
                target = TargetType.XML;
                targetFileController.setFile(FileType.XML,
                        baseName + "TargetType" + FileType.XML, name, "xml");
            } else if (htmlRadio.isSelected()) {
                target = TargetType.HTML;
                targetFileController.setFile(FileType.Html,
                        baseName + "TargetType" + FileType.Html, name, "html");
            } else if (pdfRadio.isSelected()) {
                target = TargetType.PDF;
                targetFileController.setFile(FileType.PDF,
                        baseName + "TargetType" + FileType.PDF, name, "pdf");
            } else if (inTableBox != null) {
                if (replaceRadio.isSelected()) {
                    if (!notInTable) {
                        target = TargetType.Replace;
                    } else {
                        target = TargetType.CSV;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                csvRadio.setSelected(true);
                            }
                        });
                    }
                } else if (insertRadio.isSelected()) {
                    if (!notInTable) {
                        target = TargetType.Insert;
                    } else {
                        target = TargetType.CSV;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                csvRadio.setSelected(true);
                            }
                        });
                    }
                } else if (appendRadio.isSelected()) {
                    if (!notInTable) {
                        target = TargetType.Append;
                    } else {
                        target = TargetType.CSV;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                csvRadio.setSelected(true);
                            }
                        });
                    }
                }
            }
            checkControls();
            UserConfig.setString(baseName + "DataTarget", target.name());
            formatNotify.set(!formatNotify.get());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return target;
    }

    public synchronized void refreshControls() {
        try {
            if (tableController == null || tableController.data2D == null) {
                return;
            }
            nameInput.setText(tableController.data2D.dataName());
            if (rowSelector == null) {
                return;
            }
            int thisSelect = rowSelector.getSelectionModel().getSelectedIndex();
            List<String> rows = new ArrayList<>();
            if (tableController.tableData != null) {
                for (long i = 0; i < tableController.tableData.size(); i++) {
                    rows.add("" + (i + 1));
                }
            }
            rowSelector.getItems().setAll(rows);
            rowSelector.getSelectionModel().select(thisSelect >= 0 ? thisSelect : 0);

            String selectedCol = colSelector.getSelectionModel().getSelectedItem();
            if (tableController.data2D.getColumns() != null) {
                List<String> names = tableController.data2D.columnNames();
                for (Data2DColumn column : tableController.data2D.getColumns()) {
                    if (!column.isId()) {
                        names.add(column.getColumnName());
                    }
                }
                colSelector.getItems().setAll(names);
                if (selectedCol != null) {
                    colSelector.setValue(selectedCol);
                } else {
                    colSelector.getSelectionModel().select(0);
                }
            } else {
                colSelector.getItems().clear();
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setNotInTable(boolean notInTable) {
        this.notInTable = notInTable;
        checkTarget();
    }

    public boolean inTable() {
        return !notInTable && inTableBox != null
                && (insertRadio.isSelected() || appendRadio.isSelected() || replaceRadio.isSelected());
    }

    public boolean validateTarget() {
        try {
            if (target == null) {
                return false;
            }
            switch (target) {
                case CSV:
                case Excel:
                case Text:
                case JSON:
                case XML:
                case HTML:
                case PDF:
                    File file = targetFileController.file();
                    if (file == null) {
                        popError(message("InvalidParameter") + ": " + message("FileName"));
                        return false;
                    } else {
                        return true;
                    }
//                case Matrix:
//                case DatabaseTable:
//                    if (name() == null) {
//                        popError(message("InvalidParameter") + ": " + message("DataName"));
//                        return false;
//                    } else {
//                        return true;
//                    }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public String name() {
        String name = nameInput.getText();
        return name != null ? name.trim() : null;
    }

    public int row() {
        if (!inTable()) {
            return -1;
        }
        return rowSelector.getSelectionModel().getSelectedIndex();
    }

    public int col() {
        if (!inTable()) {
            return -1;
        }
        return tableController.data2D.colOrder(colSelector.getSelectionModel().getSelectedItem());
    }

    @Override
    public void cleanPane() {
        try {
            if (tableController != null) {
                tableController.statusNotify.removeListener(tableStatusListener);
            }
            tableStatusListener = null;
            tableController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
