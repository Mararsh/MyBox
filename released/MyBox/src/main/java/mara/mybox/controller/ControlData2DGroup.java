package mara.mybox.controller;

import java.util.Arrays;
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
import mara.mybox.data2d.DataFilter;
import mara.mybox.data2d.reader.DataTableGroup.GroupType;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

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
    protected int scale;

    @FXML
    protected ControlSelection columnsController;
    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected RadioButton valuesRadio, valueRangeRadio, conditionsRadio, rowsRangeRadio;
    @FXML
    protected VBox groupBox, columnsBox, conditionsBox, splitBox, labelBox;
    @FXML
    protected HBox columnBox, scaleBox;
    @FXML
    protected ComboBox<String> columnSelector, scaleSelector;
    @FXML
    protected ControlSplit splitController;
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
            splitController.setParameters(this);

            splitController.splitGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    setScaleSelector();
                }
            });

            scale = UserConfig.getInt(baseName + "Scale", 2);
            if (scale < 0) {
                scale = 2;
            }
            scaleSelector.getItems().addAll(
                    Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
            );
            scaleSelector.getSelectionModel().select(scale + "");
            scaleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            scale = v;
                            scaleSelector.getEditor().setStyle(null);
                            UserConfig.setInt(baseName + "Scale", scale);
                        } else {
                            scaleSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        scaleSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

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
                groupBox.getChildren().addAll(columnsBox, labelBox);
                commentsLabel.setText(message("GroupValuesComments"));

            } else if (valueRangeRadio.isSelected()) {
                groupBox.getChildren().addAll(columnBox, splitBox, labelBox);
                commentsLabel.setText(message("GroupRangeComments"));
                splitController.isPositiveInteger = false;
                splitController.checkSplitType();

            } else if (rowsRangeRadio.isSelected()) {
                groupBox.getChildren().addAll(splitBox, labelBox);
                commentsLabel.setText(message("GroupRowsComments"));
                splitController.isPositiveInteger = true;
                splitController.checkSplitType();

            } else if (conditionsRadio.isSelected()) {
                groupBox.getChildren().addAll(conditionsBox, labelBox);
                commentsLabel.setText(message("GroupConditionsComments"));

            }

            setScaleSelector();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setScaleSelector() {
        scaleBox.setVisible(valueRangeRadio.isSelected() && !splitController.listRadio.isSelected());
    }

    public boolean pickValues() {
        try {
            groupName = null;
            groupNames = null;
            groupConditions = null;
            groupType = groupType();

            splitController.checkSplitType();

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
                if (groupName == null || groupName.isBlank() || !splitController.valid.get()) {
                    valid = false;
                }

            } else if (rowsRangeRadio.isSelected()) {
                if (!splitController.valid.get()) {
                    valid = false;
                }
            }

            if (!valid) {
                handleController.popError(message("InvalidParameter") + ": " + message("GroupID"));
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
        return valueRangeRadio.isSelected() && splitController.sizeRadio.isSelected();
    }

    public boolean byValueNumber() {
        return valueRangeRadio.isSelected() && splitController.numberRadio.isSelected();
    }

    public boolean byValueList() {
        return valueRangeRadio.isSelected() && splitController.listRadio.isSelected();
    }

    public boolean byRowsRange() {
        return rowsRangeRadio.isSelected();
    }

    public boolean byRowsSize() {
        return rowsRangeRadio.isSelected() && splitController.sizeRadio.isSelected();
    }

    public boolean byRowsNumber() {
        return rowsRangeRadio.isSelected() && splitController.numberRadio.isSelected();
    }

    public boolean byRowsList() {
        return rowsRangeRadio.isSelected() && splitController.listRadio.isSelected();
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

    public double splitInterval() {
        if (byValueSize() || byRowsSize()) {
            return splitController.size;

        } else {
            return Double.NaN;
        }
    }

    public int splitNumber() {
        if (byValueNumber() || byRowsNumber()) {
            return splitController.number;
        } else {
            return -1;
        }
    }

    public List<Double> splitList() {
        if (byValueList() || byRowsList()) {
            return splitController.list;
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

}
