package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.data.ValueRange;
import mara.mybox.data2d.DataFilter;
import mara.mybox.data2d.reader.DataTableGroup.GroupType;
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
    protected List<String> groupNames, conditionVariables;
    protected List<DataFilter> groupConditions;
    protected GroupType groupType;

    @FXML
    protected ControlSelection columnsController;
    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected RadioButton valuesRadio, valueRangeRadio, conditionsRadio, rowsRangeRadio;
    @FXML
    protected VBox groupBox, columnsBox, valueSplitBox, rowsSplitBox, conditionsBox, labelBox;
    @FXML
    protected HBox columnBox;
    @FXML
    protected ComboBox<String> columnSelector;
    @FXML
    protected ControlData2DSplit valueSplitController;
    @FXML
    protected ControlSplit rowsSplitController;
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
            valueSplitController.setParameters(this);

            columnSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkGroupType();
                }
            });

            rowsSplitController.setParameters(this);

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
            isSettingValues = true;
            columnSelector.getItems().clear();
            tableData.clear();
            isSettingValues = false;
            if (!handleController.data2D.isValid()) {
                return;
            }
            List<String> names = handleController.data2D.columnNames();
            columnsController.loadNames(names);
            isSettingValues = true;
            columnSelector.getItems().setAll(names);
            columnSelector.getSelectionModel().select(0);
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkGroupType() {
        try {
            groupBox.getChildren().clear();
            commentsLabel.setText("");

            if (valuesRadio.isSelected()) {
                groupBox.getChildren().addAll(columnsBox, labelBox);
                commentsLabel.setText(message("GroupValuesComments"));

            } else if (valueRangeRadio.isSelected()) {
                groupBox.getChildren().addAll(columnBox, valueSplitBox, labelBox);
                commentsLabel.setText(message("GroupRangeComments"));
                valueSplitController.setColumn(handleController.data2D.columnByName(columnSelector.getValue()));

            } else if (rowsRangeRadio.isSelected()) {
                groupBox.getChildren().addAll(rowsSplitBox, labelBox);
                commentsLabel.setText(message("GroupRowsComments"));
                rowsSplitController.checkSplitType();

            } else if (conditionsRadio.isSelected()) {
                groupBox.getChildren().addAll(conditionsBox, labelBox);
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
            groupType = groupType();

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

            } else if (valueRangeRadio.isSelected()) {
                groupName = columnSelector.getValue();
                if (groupName == null || groupName.isBlank() || !valueSplitController.isValid()) {
                    valid = false;
                }

            } else if (rowsRangeRadio.isSelected()) {
                rowsSplitController.checkSplitType();
                if (!rowsSplitController.valid.get()) {
                    valid = false;
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

    public boolean byEqualValues() {
        return valuesRadio.isSelected();
    }

    public boolean byValueSize() {
        return valueRangeRadio.isSelected() && valueSplitController.sizeRadio.isSelected();
    }

    public boolean byValueNumber() {
        return valueRangeRadio.isSelected() && valueSplitController.numberRadio.isSelected();
    }

    public boolean byValueList() {
        return valueRangeRadio.isSelected() && valueSplitController.listRadio.isSelected();
    }

    public boolean byRowsRange() {
        return rowsRangeRadio.isSelected();
    }

    public boolean byRowsSize() {
        return rowsRangeRadio.isSelected() && rowsSplitController.sizeRadio.isSelected();
    }

    public boolean byRowsNumber() {
        return rowsRangeRadio.isSelected() && rowsSplitController.numberRadio.isSelected();
    }

    public boolean byRowsList() {
        return rowsRangeRadio.isSelected() && rowsSplitController.listRadio.isSelected();
    }

    public boolean byConditions() {
        return conditionsRadio.isSelected();
    }

    public GroupType groupType() {
        if (byEqualValues()) {
            return GroupType.EqualValues;

        } else if (byValueSize()) {
            return GroupType.ValueSplitInterval;

        } else if (byValueNumber()) {
            return GroupType.ValueSplitNumber;

        } else if (byValueList()) {
            return GroupType.ValueSplitList;

        } else if (byRowsSize()) {
            return GroupType.RowsSplitInterval;

        } else if (byRowsNumber()) {
            return GroupType.RowsSplitNumber;

        } else if (byRowsList()) {
            return GroupType.RowsSplitList;

        } else if (byConditions()) {
            return GroupType.Conditions;

        } else {
            return null;
        }
    }

    public String groupName() {
        return groupName;
    }

    public List<String> groupNames() {
        return groupNames;
    }

    public List<DataFilter> groupConditions() {
        return groupConditions;
    }

    public int splitScale() {
        return valueSplitController.scale;
    }

    public double valueSplitInterval() {
        if (byValueSize()) {
            return valueSplitController.size;

        } else {
            return Double.NaN;
        }
    }

    public double rowsSplitInterval() {
        if (byRowsSize()) {
            return rowsSplitController.size;

        } else {
            return Double.NaN;
        }
    }

    public int valueSplitNumber() {
        if (byValueNumber()) {
            return valueSplitController.number;
        } else {
            return -1;
        }
    }

    public int rowsSplitNumber() {
        if (byRowsNumber()) {
            return rowsSplitController.number;
        } else {
            return -1;
        }
    }

    public List<ValueRange> valueSplitList() {
        if (byValueList()) {
            return valueSplitController.tableData;
        } else {
            return null;
        }
    }

    public List<Integer> rowsSplitList() {
        if (byRowsList()) {
            return rowsSplitController.list;
        } else {
            return null;
        }
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

    @Override
    public void cleanPane() {
        try {
            listener = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}