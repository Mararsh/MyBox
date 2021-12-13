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
import javafx.scene.layout.HBox;
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
    protected boolean includeTable;

    @FXML
    protected ToggleGroup targetGroup;
    @FXML
    protected RadioButton csvRadio, excelRadio, textsRadio, matrixRadio, systemClipboardRadio, myBoxClipboardRadio,
            frontRadio, endRadio, belowRadio, aboveRadio;
    @FXML
    protected ComboBox<String> rowSelector;
    @FXML
    protected VBox tableBox;
    @FXML
    protected HBox rowBox;

    public void setParameters(BaseController parent, ControlData2DEditTable tableController) {
        try {
            this.tableController = tableController;
            this.baseName = parent.baseName;

            this.includeTable = tableController != null;
            if (!includeTable) {
                thisPane.getChildren().remove(tableBox);
            } else {
                refreshControls();
            }

            rowBox.setVisible(false);
            targetGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkTarget();
                }
            });

            target = UserConfig.getString(baseName + "DataTarget", "csv");
            setTarget(target);

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
                target = "matrix";
            } else if (systemClipboardRadio.isSelected()) {
                target = "systemClipboard";
            } else if (myBoxClipboardRadio.isSelected()) {
                target = "myBoxClipboard";
            } else if (frontRadio.isSelected()) {
                if (includeTable) {
                    target = "front";
                } else {
                    target = "csv";
                }
            } else if (endRadio.isSelected()) {
                if (includeTable) {
                    target = "end";
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            csvRadio.fire();
                        }
                    });
                }
            } else if (belowRadio.isSelected()) {
                if (includeTable) {
                    target = "below";
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            csvRadio.fire();
                        }
                    });
                }
            } else if (aboveRadio.isSelected()) {
                if (includeTable) {
                    target = "above";
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            csvRadio.fire();
                        }
                    });
                }
            }
            UserConfig.setString(baseName + "DataTarget", target);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        rowBox.setVisible(includeTable && (belowRadio.isSelected() || aboveRadio.isSelected()));
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
                case "front":
                    if (includeTable) {
                        frontRadio.fire();
                    } else {
                        csvRadio.fire();
                    }
                    break;
                case "end":
                    if (includeTable) {
                        endRadio.fire();
                    } else {
                        csvRadio.fire();
                    }
                    break;
                case "below":
                    if (includeTable) {
                        belowRadio.fire();
                    } else {
                        csvRadio.fire();
                    }
                    break;
                case "above":
                    if (includeTable) {
                        aboveRadio.fire();
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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public int tableIndex() {
        if (!includeTable) {
            return -1;
        }
        int index = rowSelector.getSelectionModel().getSelectedIndex();
        if (frontRadio.isSelected()) {
            index = 0;
        } else if (index < 0 || endRadio.isSelected()) {
            index = tableController.tableData.size();
        } else if (belowRadio.isSelected()) {
            index++;
        }
        return index;
    }

    public boolean isTable() {
        return includeTable
                && (frontRadio.isSelected() || endRadio.isSelected()
                || belowRadio.isSelected() || aboveRadio.isSelected());
    }

}
