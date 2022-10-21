package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DChartController extends BaseData2DHandleController {

    protected String selectedCategory, selectedValue;
    protected List<Integer> dataColsIndices;
    protected boolean needRowNumber = true;

    @FXML
    protected ComboBox<String> categoryColumnSelector, valueColumnSelector;
    @FXML
    protected Label noticeLabel;
    @FXML
    protected CheckBox displayAllCheck;

    @Override
    public void initControls() {
        try {
            super.initControls();

            initDataTab();

            if (displayAllCheck != null) {
                displayAllCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayAll", true));
                displayAllCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "DisplayAll", displayAllCheck.isSelected());
                    noticeMemory();
                });

                displayAllCheck.visibleProperty().bind(allPagesRadio.selectedProperty());
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initDataTab() {
        try {
            if (categoryColumnSelector != null) {
                categoryColumnSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        checkOptions();
                    }
                });
            }

            if (valueColumnSelector != null) {
                valueColumnSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        checkOptions();
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void refreshControls() {
        try {
            super.refreshControls();
            makeOptions();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void makeOptions() {
        try {
            List<String> names = tableController.data2D.columnNames();
            if (names == null || names.isEmpty()) {
                return;
            }
            isSettingValues = true;
            if (categoryColumnSelector != null) {
                selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
                categoryColumnSelector.getItems().setAll(names);
                if (selectedCategory != null && names.contains(selectedCategory)) {
                    categoryColumnSelector.setValue(selectedCategory);
                } else {
                    categoryColumnSelector.getSelectionModel().select(0);
                }
            }
            if (valueColumnSelector != null) {
                selectedValue = valueColumnSelector.getSelectionModel().getSelectedItem();
                valueColumnSelector.getItems().setAll(names);
                if (selectedValue != null && names.contains(selectedValue)) {
                    valueColumnSelector.setValue(selectedValue);
                } else {
                    valueColumnSelector.getSelectionModel().select(names.size() > 1 ? 1 : 0);
                }
            }
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        if (isSettingValues) {
            return true;
        }
        boolean ok = super.checkOptions();
        noticeMemory();
        return ok;
    }

    public void noticeMemory() {
        if (noticeLabel == null) {
            return;
        }
        noticeLabel.setVisible(isAllPages()
                && (displayAllCheck == null || displayAllCheck.isSelected()));
    }

    @Override
    public boolean initData() {
        try {
            if (!super.initData()) {
                return false;
            }
            if (categoryColumnSelector != null) {
                selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
            }
            if (valueColumnSelector != null) {
                selectedValue = valueColumnSelector.getSelectionModel().getSelectedItem();
            }
            dataColsIndices = new ArrayList<>();
            if (!notSelectColumnsInTable && (checkedColsIndices == null || checkedColsIndices.isEmpty())) {
                outOptionsError(message("SelectToHandle") + ": " + message("Columns"));
                return false;
            }
            dataColsIndices.addAll(checkedColsIndices);
            outputColumns = new ArrayList<>();
            if (needRowNumber) {
                outputColumns.add(new Data2DColumn(message("RowNumber"), ColumnDefinition.ColumnType.String));
            }
            outputColumns.addAll(checkedColumns);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public String chartTitle() {
        return baseTitle;
    }

    public String categoryName() {
        return selectedCategory;
    }

    @Override
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(task, filterController.filter);
                    readData();
                    data2D.stopFilter();
                    return true;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (outputData == null || outputData.isEmpty()) {
                    popError(message("NoData"));
                    return;
                }
                outputData();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                task = null;
            }

        };
        start(task);
    }

    public void readData() {
        try {
            if (isAllPages()) {
                outputData = data2D.allRows(dataColsIndices, needRowNumber);
            } else {
                outputData = filtered(dataColsIndices, needRowNumber);
            }
            if (outputData == null || outputColumns == null
                    || scaleSelector == null || scale < 0) {
                return;
            }
            boolean needScale = false;
            for (int i = 0; i < outputColumns.size(); i++) {
                Data2DColumn column = outputColumns.get(i);
                if (column.needScale()) {
                    needScale = true;
                    break;
                }
            }
            if (!needScale) {
                return;
            }
            List<List<String>> scaled = new ArrayList<>();
            for (List<String> row : outputData) {
                List<String> srow = new ArrayList<>();
                for (int i = 0; i < outputColumns.size(); i++) {
                    Data2DColumn column = outputColumns.get(i);
                    String s = row.get(i);
                    if (s == null || !column.needScale() || scale < 0) {
                        srow.add(s);
                    } else {
                        srow.add(DoubleTools.scaleString(s, invalidAs, scale));
                    }
                }
                scaled.add(srow);
            }
            outputData = scaled;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public void outputData() {
        drawChart();
    }

    public void drawChart() {
        try {
            if (outputData == null || outputData.isEmpty()) {
                popError(message("NoData"));
                return;
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void redrawChart() {
        drawChart();
    }

    @FXML
    @Override
    public void refreshAction() {
        okAction();
    }

    public StringTable dataHtmlTable() {
        try {
            List<String> names = new ArrayList<>();
            if (outputColumns != null) {
                for (Data2DColumn c : outputColumns) {
                    names.add(c.getColumnName());
                }
            }
            StringTable table = new StringTable(names);
            for (List<String> row : outputData) {
                table.add(row);
            }
            return table;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

}
