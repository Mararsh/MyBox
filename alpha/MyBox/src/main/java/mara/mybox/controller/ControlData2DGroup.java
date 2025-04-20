package mara.mybox.controller;

import java.util.ArrayList;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.data.ValueRange;
import mara.mybox.data2d.DataFilter;
import mara.mybox.data2d.DataTableGroup.GroupType;
import mara.mybox.data2d.DataTableGroup.TimeType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-8-10
 * @License Apache License Version 2.0
 */
public class ControlData2DGroup extends BaseTableViewController<DataFilter> {

    protected BaseData2DTaskController taskController;
    protected String groupName, timeName, expression, filledExpression;
    protected List<String> groupNames, conditionVariables;
    protected List<DataFilter> groupConditions;

    @FXML
    protected ControlSelection columnsController;
    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected RadioButton valuesRadio, valueRangeRadio, timeRadio, expressionRadio, conditionsRadio, rowsRangeRadio,
            centuryRadio, yearRadio, monthRadio, dayRadio, hourRadio, minuteRadio, secondRadio;
    @FXML
    protected VBox groupBox, columnsBox, valueSplitBox, rowsSplitBox, expressionBox, conditionsBox, labelBox;
    @FXML
    protected HBox columnBox;
    @FXML
    protected ComboBox<String> columnSelector;
    @FXML
    protected FlowPane timePane;
    @FXML
    protected ControlData2DSplit valueSplitController;
    @FXML
    protected ControlSplit rowsSplitController;
    @FXML
    protected ControlData2DRowExpression expressionController;
    @FXML
    protected TableColumn<DataFilter, String> conditionColumn;
    @FXML
    protected TableColumn<DataFilter, Boolean> falseColumn;
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
                    if (!isSettingValues && valueRangeRadio.isSelected()) {
                        valueSplitController.setColumn(taskController.data2D.columnByName(columnSelector.getValue()));
                    }
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
            falseColumn.setCellValueFactory(new PropertyValueFactory<>("matchFalse"));
            maxColumn.setCellValueFactory(new PropertyValueFactory<>("maxPassed"));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseData2DTaskController taskController) {
        try {
            this.taskController = taskController;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void refreshControls() {
        try {
            columnsController.loadNames(null);
            isSettingValues = true;
            columnSelector.getItems().clear();
            tableData.clear();
            isSettingValues = false;
            expressionController.setData2D(taskController.data2D);
            if (!taskController.data2D.hasColumns()) {
                return;
            }
            List<String> names = taskController.data2D.columnNames();
            columnsController.loadNames(names);
            List<String> times = taskController.data2D.timeColumnNames();
            if (times == null || times.isEmpty()) {
                if (timeRadio.isSelected()) {
                    valuesRadio.setSelected(true);
                }
                timeRadio.setDisable(true);
            } else {
                timeRadio.setDisable(false);
            }
            loadColumnNames();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadColumnNames() {
        if (!taskController.data2D.hasColumns()) {
            return;
        }
        List<String> names;
        if (timeRadio.isSelected()) {
            names = taskController.data2D.timeColumnNames();
        } else {
            names = taskController.data2D.columnNames();
        }
        isSettingValues = true;
        columnSelector.getItems().setAll(names);
        columnSelector.getSelectionModel().select(0);
        if (valueRangeRadio.isSelected()) {
            valueSplitController.setColumn(taskController.data2D.columnByName(columnSelector.getValue()));
        }
        isSettingValues = false;
    }

    public void checkGroupType() {
        try {
            if (isSettingValues) {
                return;
            }
            groupBox.getChildren().clear();
            commentsLabel.setText("");

            if (valuesRadio.isSelected()) {
                groupBox.getChildren().addAll(columnsBox, labelBox);
                commentsLabel.setText(message("GroupValuesComments"));

            } else if (valueRangeRadio.isSelected()) {
                groupBox.getChildren().addAll(columnBox, valueSplitBox, labelBox);
                commentsLabel.setText(message("GroupRangeComments"));
                valueSplitController.refreshStyle();
                loadColumnNames();

            } else if (timeRadio.isSelected()) {
                groupBox.getChildren().addAll(columnBox, timePane, labelBox);
                commentsLabel.setText(message("GroupTimeComments"));
                loadColumnNames();

            } else if (rowsRangeRadio.isSelected()) {
                groupBox.getChildren().addAll(rowsSplitBox, labelBox);
                commentsLabel.setText(message("GroupRowsComments"));
                rowsSplitController.checkSplitType();

            } else if (expressionRadio.isSelected()) {
                groupBox.getChildren().addAll(expressionBox, labelBox);
                commentsLabel.setText(message("GroupExpressionComments"));
                expressionController.refreshStyle();

            } else if (conditionsRadio.isSelected()) {
                groupBox.getChildren().addAll(conditionsBox, labelBox);
                commentsLabel.setText(message("GroupConditionsComments"));
                refreshStyle(conditionsBox);

            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean pickValues() {
        try {
            groupName = null;
            groupNames = null;
            groupConditions = null;
            timeName = null;
            expression = null;
            filledExpression = null;

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

            } else if (timeRadio.isSelected()) {
                timeName = columnSelector.getValue();
                if (timeName == null || timeName.isBlank()) {
                    valid = false;
                }

            } else if (expressionRadio.isSelected()) {
                expression = expressionController.scriptInput.getText();
                if (expression == null || expression.isBlank()) {
                    popError(message("Invalid") + ": " + message("RowExpression"));
                    valid = false;
                }
                if (!expressionController.checkExpression(taskController.isAllPages())) {
                    if (!PopTools.askSure(getTitle(),
                            message("RowExpressionLooksInvalid") + ": \n"
                            + expressionController.error,
                            message("SureContinue"))) {
                        valid = false;
                    }
                }

            } else if (rowsRangeRadio.isSelected()) {
                rowsSplitController.checkSplitType();
                if (!rowsSplitController.valid.get()) {
                    valid = false;
                }
            }

            if (!valid) {
                taskController.popError(message("InvalidParameter") + ": " + message("Group"));
                taskController.tabPane.getSelectionModel().select(taskController.groupTab);
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

    public boolean byValueRange() {
        return valueRangeRadio.isSelected();
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

    public boolean byTime() {
        return timeRadio.isSelected();
    }

    public boolean byExpression() {
        return expressionRadio.isSelected();
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

        } else if (byTime()) {
            return GroupType.Time;

        } else if (byExpression()) {
            return GroupType.Expression;

        } else {
            return null;
        }
    }

    public TimeType timeType() {
        if (!byTime()) {
            return null;
        }
        if (centuryRadio.isSelected()) {
            return TimeType.Century;

        } else if (yearRadio.isSelected()) {
            return TimeType.Year;

        } else if (monthRadio.isSelected()) {
            return TimeType.Month;

        } else if (dayRadio.isSelected()) {
            return TimeType.Day;

        } else if (hourRadio.isSelected()) {
            return TimeType.Hour;

        } else if (minuteRadio.isSelected()) {
            return TimeType.Minute;

        } else if (secondRadio.isSelected()) {
            return TimeType.Second;

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

    public String timeName() {
        return timeName;
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

    public int valueSplitNumber() {
        if (byValueNumber()) {
            return valueSplitController.number;
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

    public int rowsSplitInterval() {
        if (byRowsSize()) {
            return rowsSplitController.size;

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

    public List<Integer> rowsSplitList() {
        if (byRowsList()) {
            return rowsSplitController.list;
        } else {
            return null;
        }
    }

    public List<String> scripts() {
        List<String> scripts = new ArrayList<>();
        if (byConditions()) {
            for (DataFilter filter : groupConditions) {
                String groupScript = filter.getSourceScript();
                if (groupScript != null && !groupScript.isBlank()) {
                    scripts.add(groupScript);
                }
            }
        } else if (byExpression()) {
            scripts.add(expression);
        }
        return scripts.isEmpty() ? null : scripts;
    }

    public void fillScripts(List<String> filledScripts) {
        if (filledScripts == null || filledScripts.isEmpty()) {
            return;
        }
        if (byConditions()) {
            int index = 0;
            for (DataFilter filter : groupConditions) {
                String groupScript = filter.getSourceScript();
                if (groupScript != null && !groupScript.isBlank()) {
                    filter.setFilledScript(filledScripts.get(index++));
                }
            }
        } else if (byExpression()) {
            filledExpression = filledScripts.get(0);
        }
    }

    @FXML
    @Override
    public void addAction() {
        Data2DRowFilterEdit controller = Data2DRowFilterEdit.open(taskController, null);
        controller.notify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                DataFilter filter = controller.getFilter();
                if (filter != null) {
                    tableData.add(filter);
                }
                controller.close();
            }
        });
    }

    @FXML
    @Override
    public void editAction() {
        int index = selectedIndix();
        if (index < 0) {
            popError(message("SelectToHandle"));
            return;
        }
        DataFilter selected = tableData.get(index);
        Data2DRowFilterEdit controller = Data2DRowFilterEdit.open(taskController, selected);
        controller.notify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                DataFilter filter = controller.getFilter();
                if (filter != null) {
                    tableData.set(index, filter);
                }
                controller.close();
            }
        });
    }

    @FXML
    @Override
    public void deleteAction() {
        try {
            List<DataFilter> selected = selectedItems();
            if (selected == null || selected.isEmpty()) {
                clearAction();
                return;
            }
            tableData.removeAll(selected);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void aboutGroupingRows() {
        openHtml(HelpTools.aboutGroupingRows());
    }

}
