package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-11
 * @License Apache License Version 2.0
 */
public class ControlData2DTarget extends BaseController {

    protected ControlData2DEditTable tableController;
    protected String target;
    protected boolean includeTable, handleFile;

    @FXML
    protected ToggleGroup targetGroup;
    @FXML
    protected RadioButton csvRadio, excelRadio, textsRadio, matrixRadio, systemClipboardRadio, myBoxClipboardRadio,
            replaceRadio, insertRadio, appendRadio;
    @FXML
    protected ComboBox<String> rowSelector, colSelector;
    @FXML
    protected VBox tableBox;
    @FXML
    protected FlowPane locationPane;

    public void setParameters(BaseController parent, ControlData2DEditTable tableController, boolean includeTable) {
        try {
            this.tableController = tableController;
            this.baseName = parent.baseName;

            locationPane.setVisible(inTable());
            targetGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    locationPane.setVisible(inTable());
                    checkTarget();
                }
            });

            target = UserConfig.getString(baseName + "DataTarget", "csv");
            setTarget(target);

            setIncludeTable(includeTable);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public String checkTarget() {
        try {
            if (csvRadio.isSelected()) {
                target = "csv";
            } else if (excelRadio.isSelected()) {
                target = "excel";
            } else if (textsRadio.isSelected()) {
                target = "texts";
            } else if (matrixRadio.isSelected()) {
                if (!handleFile) {
                    target = "matrix";
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            csvRadio.fire();
                        }
                    });
                }
            } else if (systemClipboardRadio.isSelected()) {
                target = "systemClipboard";
            } else if (myBoxClipboardRadio.isSelected()) {
                target = "myBoxClipboard";
            } else if (replaceRadio.isSelected()) {
                if (includeTable && !handleFile) {
                    target = "replace";
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            csvRadio.fire();
                        }
                    });
                }
            } else if (insertRadio.isSelected()) {
                if (includeTable && !handleFile) {
                    target = "insert";
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            csvRadio.fire();
                        }
                    });
                }
            } else if (appendRadio.isSelected()) {
                if (includeTable && !handleFile) {
                    target = "append";
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            csvRadio.fire();
                        }
                    });
                }
            }
            if (includeTable && !handleFile) {
                if (!thisPane.getChildren().contains(tableBox)) {
                    thisPane.getChildren().add(tableBox);
                }
                refreshControls();
            } else {
                if (thisPane.getChildren().contains(tableBox)) {
                    thisPane.getChildren().remove(tableBox);
                }
            }
            matrixRadio.setDisable(handleFile);
            UserConfig.setString(baseName + "DataTarget", target);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return target;
    }

    public void setTarget(String target) {
        try {
            if (target == null) {
                csvRadio.fire();
                return;
            }
            switch (target) {
                case "csv":
                    csvRadio.fire();
                    break;
                case "excel":
                    excelRadio.fire();
                    break;
                case "texts":
                    textsRadio.fire();
                    break;
                case "matrix":
                    matrixRadio.fire();
                    break;
                case "systemClipboard":
                    systemClipboardRadio.fire();
                    break;
                case "myBoxClipboard":
                    myBoxClipboardRadio.fire();
                    break;
                case "append":
                    if (includeTable) {
                        appendRadio.fire();
                    } else {
                        csvRadio.fire();
                    }
                    break;
                case "insert":
                    if (includeTable) {
                        insertRadio.fire();
                    } else {
                        csvRadio.fire();
                    }
                    break;
                case "relpace":
                    if (includeTable) {
                        replaceRadio.fire();
                    } else {
                        csvRadio.fire();
                    }
                    break;
                default:
                    csvRadio.fire();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void refreshControls() {
        try {
            if (tableController == null) {
                return;
            }
            int thisSelect = rowSelector.getSelectionModel().getSelectedIndex();
            List<String> rows = new ArrayList<>();
            for (long i = 0; i < tableController.tableData.size(); i++) {
                rows.add("" + (i + 1));
            }
            rowSelector.getItems().setAll(rows);
            int tableSelect = tableController.tableView.getSelectionModel().getSelectedIndex();
            rowSelector.getSelectionModel().select(tableSelect >= 0 ? tableSelect : (thisSelect >= 0 ? thisSelect : 0));

            String selectedCol = colSelector.getSelectionModel().getSelectedItem();
            colSelector.getItems().setAll(tableController.data2D.columnNames());
            if (selectedCol != null) {
                colSelector.setValue(selectedCol);
            } else {
                colSelector.getSelectionModel().select(0);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setIncludeTable(boolean includeTable) {
        this.includeTable = includeTable;
        checkTarget();
    }

    public void setHandleFile(boolean handleFile) {
        this.handleFile = handleFile;
        checkTarget();
    }

    public boolean inTable() {
        return includeTable
                && (insertRadio.isSelected() || appendRadio.isSelected() || replaceRadio.isSelected());
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

}
