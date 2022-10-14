package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.DataFilter;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-8-10
 * @License Apache License Version 2.0
 */
public class ControlData2DGroup extends BaseTableViewController<DataFilter> {

    protected BaseData2DHandleController handleController;
    protected ChangeListener<Boolean> listener;
    protected String groupName;
    protected List<String> groupNames;
    protected List<DataFilter> groupConditions;
    protected double groupInterval, groupNumber;

    @FXML
    protected ControlSelection columnsController;
    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected RadioButton valuesRadio, intervalRadio, numberRadio, conditionsRadio;
    @FXML
    protected VBox groupBox, columnsBox, conditionsBox;
    @FXML
    protected HBox columnlBox, intervalBox, numberBox;
    @FXML
    protected TextField intervalInput, numberInput;
    @FXML
    protected ComboBox<String> columnSelector;
    @FXML
    protected TableColumn<DataFilter, String> conditionColumn;
    @FXML
    protected TableColumn<DataFilter, Boolean> reverseColumn;
    @FXML
    protected TableColumn<DataFilter, Long> maxColumn;
    @FXML
    protected Label commentsLabel;

    @Override
    public void initControls() {
        try {
            super.initControls();

            columnsController.setParameters(this, message("Column"), message("GroupBy"));
            typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkGroupType();
                }
            });
            checkGroupType();

            conditionColumn.setCellValueFactory(new PropertyValueFactory<>("sourceScript"));
            reverseColumn.setCellValueFactory(new PropertyValueFactory<>("reversed"));
            maxColumn.setCellValueFactory(new PropertyValueFactory<>("maxPassed"));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(BaseData2DHandleController handleController) {
        try {
            this.handleController = handleController;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void refreshControls() {
        try {
            columnsController.loadNames(null);
            columnSelector.getItems().clear();
            tableData.clear();
            if (!handleController.data2D.isValid()) {
                return;
            }
            List<String> names = handleController.data2D.columnNames();
            columnsController.loadNames(names);
            columnSelector.getItems().setAll(names);
            columnSelector.getSelectionModel().select(0);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkGroupType() {
        try {
            groupBox.getChildren().clear();
            commentsLabel.setText("");

            if (valuesRadio.isSelected()) {
                groupBox.getChildren().add(columnsBox);
                commentsLabel.setText(message("GroupValuesComments"));

            } else if (intervalRadio.isSelected()) {
                groupBox.getChildren().addAll(columnlBox, intervalBox);
                commentsLabel.setText(message("GroupIntervalComments"));

            } else if (numberRadio.isSelected()) {
                groupBox.getChildren().addAll(columnlBox, numberBox);
                commentsLabel.setText(message("GroupNumberComments"));

            } else if (conditionsRadio.isSelected()) {
                groupBox.getChildren().add(conditionsBox);
                commentsLabel.setText(message("GroupConditionsComments"));

            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean pickValues() {
        try {
            groupName = null;
            groupNames = null;
            groupConditions = null;
            groupInterval = Double.NaN;
            groupNumber = Double.NaN;

            boolean valid = true;
            if (valuesRadio.isSelected()) {
                groupNames = columnsController.selectedNames();
                if (groupNames == null || groupNames.isEmpty()) {
                    valid = false;
                }

            } else if (conditionsRadio.isSelected()) {
                groupConditions = tableData;
                if (groupConditions == null || groupConditions.isEmpty()) {
                    valid = false;
                }

            } else if (intervalRadio.isSelected()) {
                groupName = columnSelector.getValue();
                if (groupName == null || groupName.isBlank()) {
                    valid = false;
                } else {
                    try {
                        groupInterval = Double.valueOf(intervalInput.getText());
                    } catch (Exception e) {
                        valid = false;
                    }
                }

            } else if (numberRadio.isSelected()) {
                groupName = columnSelector.getValue();
                if (groupName == null || groupName.isBlank()) {
                    valid = false;
                } else {
                    try {
                        int v = Integer.valueOf(numberInput.getText());
                        if (v <= 0) {
                            valid = false;
                        } else {
                            groupNumber = v;
                        }
                    } catch (Exception e) {
                        valid = false;
                    }
                }
            }

            if (!valid) {
                handleController.popError(message("InvalidParameter") + ": " + message("Group"));
                handleController.tabPane.getSelectionModel().select(handleController.groupTab);
                return false;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean byValues() {
        return valuesRadio.isSelected();
    }

    public boolean byInterval() {
        return intervalRadio.isSelected();
    }

    public boolean byNumber() {
        return numberRadio.isSelected();
    }

    public boolean byConditions() {
        return conditionsRadio.isSelected();
    }

    @FXML
    @Override
    public void addAction() {
        Data2DRowFilterEdit controller = Data2DRowFilterEdit.open(handleController, null);
        listener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                controller.notify.removeListener(listener);
                DataFilter filter = controller.getFilter();
                if (filter != null) {
                    tableData.add(filter);
                }
                controller.close();
            }
        };
        controller.notify.addListener(listener);
    }

    @FXML
    @Override
    public void editAction() {
        int index = tableView.getSelectionModel().getSelectedIndex();
        if (index < 0) {
            return;
        }
        DataFilter selected = tableData.get(index);
        Data2DRowFilterEdit controller = Data2DRowFilterEdit.open(handleController, selected);
        listener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                controller.notify.removeListener(listener);
                DataFilter filter = controller.getFilter();
                if (filter != null) {
                    tableData.set(index, filter);
                }
                controller.close();
            }
        };
        controller.notify.addListener(listener);
    }

    @FXML
    @Override
    public void deleteAction() {
        try {
            List<DataFilter> selected = tableView.getSelectionModel().getSelectedItems();
            if (selected == null || selected.isEmpty()) {
                clearAction();
                return;
            }
            tableData.removeAll(selected);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
