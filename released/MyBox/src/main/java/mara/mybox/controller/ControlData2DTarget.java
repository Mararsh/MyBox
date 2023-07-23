package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
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
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-11
 * @License Apache License Version 2.0
 */
public class ControlData2DTarget extends BaseController {

    protected ControlData2DLoad tableController;
    protected String target;
    protected boolean notInTable;
    protected ChangeListener<Boolean> tableStatusListener;

    @FXML
    protected ToggleGroup targetGroup;
    @FXML
    protected RadioButton csvRadio, excelRadio, textsRadio, matrixRadio, databaseRadio,
            jsonRadio, xmlRadio, htmlRadio, pdfRadio,
            systemClipboardRadio, myBoxClipboardRadio, replaceRadio, insertRadio, appendRadio;
    @FXML
    protected ComboBox<String> rowSelector, colSelector;
    @FXML
    protected VBox externalBox, inTableBox;
    @FXML
    protected HBox prefixBox, locationBox;
    @FXML
    protected TextField nameInput;

    public void setParameters(BaseController parent, ControlData2DLoad tableController) {
        try {
            baseName = parent.baseName;
            this.tableController = tableController;

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

            target = UserConfig.getString(baseName + "DataTarget", "csv");
            setTarget(target);

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
            if (externalBox.getChildren().contains(prefixBox)) {
                externalBox.getChildren().remove(prefixBox);
            }
        } else {
            if (inTableBox != null && inTableBox.getChildren().contains(locationBox)) {
                inTableBox.getChildren().remove(locationBox);
            }
            if (!externalBox.getChildren().contains(prefixBox)) {
                externalBox.getChildren().add(prefixBox);
            }
        }
    }

    public String checkTarget() {
        try {
            target = "csv";
            if (csvRadio.isSelected()) {
                target = "csv";
            } else if (excelRadio.isSelected()) {
                target = "excel";
            } else if (textsRadio.isSelected()) {
                target = "texts";
            } else if (matrixRadio.isSelected()) {
                target = "matrix";
            } else if (systemClipboardRadio.isSelected()) {
                target = "systemClipboard";
            } else if (myBoxClipboardRadio.isSelected()) {
                target = "myBoxClipboard";
            } else if (databaseRadio.isSelected()) {
                target = "table";
            } else if (jsonRadio.isSelected()) {
                target = "json";
            } else if (xmlRadio.isSelected()) {
                target = "xml";
            } else if (htmlRadio.isSelected()) {
                target = "html";
            } else if (pdfRadio.isSelected()) {
                target = "pdf";
            } else if (inTableBox != null) {
                if (replaceRadio.isSelected()) {
                    if (!notInTable) {
                        target = "replace";
                    } else {
                        target = "csv";
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                csvRadio.setSelected(true);
                            }
                        });
                    }
                } else if (insertRadio.isSelected()) {
                    if (!notInTable) {
                        target = "insert";
                    } else {
                        target = "csv";
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                csvRadio.setSelected(true);
                            }
                        });
                    }
                } else if (appendRadio.isSelected()) {
                    if (!notInTable) {
                        target = "append";
                    } else {
                        target = "csv";
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
            UserConfig.setString(baseName + "DataTarget", target);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return target;
    }

    public void setTarget(String target) {
        try {
            if (target == null) {
                csvRadio.setSelected(true);
                return;
            }
            switch (target) {
                case "csv":
                    csvRadio.setSelected(true);
                    break;
                case "excel":
                    excelRadio.setSelected(true);
                    break;
                case "texts":
                    textsRadio.setSelected(true);
                    break;
                case "matrix":
                    matrixRadio.setSelected(true);
                    break;
                case "systemClipboard":
                    systemClipboardRadio.setSelected(true);
                    break;
                case "myBoxClipboard":
                    myBoxClipboardRadio.setSelected(true);
                    break;
                case "table":
                    databaseRadio.setSelected(true);
                    break;
                case "json":
                    jsonRadio.setSelected(true);
                    break;
                case "xml":
                    xmlRadio.setSelected(true);
                    break;
                case "html":
                    htmlRadio.setSelected(true);
                    break;
                case "pdf":
                    pdfRadio.setSelected(true);
                    break;
                case "append":
                    if (notInTable && inTableBox != null) {
                        appendRadio.setSelected(true);
                    } else {
                        csvRadio.setSelected(true);
                    }
                    break;
                case "insert":
                    if (notInTable && inTableBox != null) {
                        insertRadio.setSelected(true);
                    } else {
                        csvRadio.setSelected(true);
                    }
                    break;
                case "relpace":
                    if (notInTable && inTableBox != null) {
                        replaceRadio.setSelected(true);
                    } else {
                        csvRadio.setSelected(true);
                    }
                    break;
                default:
                    csvRadio.setSelected(true);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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

    public String name() {
        return nameInput.getText().trim();
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
