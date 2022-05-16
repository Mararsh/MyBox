package mara.mybox.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.SingletonTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DChartController extends BaseData2DHandleController {

    protected ChangeListener<Boolean> tableStatusListener, tableLoadListener;
    protected String selectedCategory, selectedValue;
    protected List<Integer> checkedColsIndices;
    protected List<Integer> dataColsIndices;
    protected Map<String, String> palette;

    @FXML
    protected ComboBox<String> categoryColumnSelector, valueColumnSelector;

    @Override
    public void initControls() {
        try {
            super.initControls();

            initDataTab();

            palette = new HashMap();

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
    public boolean scaleChanged() {
        if (super.scaleChanged()) {
            okAction();
            return true;
        }
        return false;
    }

    @Override
    public void setParameters(ControlData2DEditTable tableController) {
        try {
            super.setParameters(tableController);

            tableStatusListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    refreshControls();
                }
            };
            tableController.statusNotify.addListener(tableStatusListener);

            tableLoadListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    okAction();
                }
            };
            tableController.loadedNotify.addListener(tableLoadListener);

            refreshControls();

            okAction();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterInit() {
    }

    public void refreshControls() {
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
        if (categoryColumnSelector != null) {
            selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
        }
        if (valueColumnSelector != null) {
            selectedValue = valueColumnSelector.getSelectionModel().getSelectedItem();
        }
        if (ok) {
            noticeMemory();
        }
        return ok;
    }

    public void noticeMemory() {
        if (isSettingValues) {
            return;
        }
        if (sourceController.allPages()) {
            infoLabel.setText(message("AllRowsLoadComments"));
        }
    }

    public boolean initData() {
        try {
            dataColsIndices = new ArrayList<>();
            checkedColsIndices = sourceController.checkedColsIndices();
            if (checkedColsIndices == null || checkedColsIndices.isEmpty()) {
                popError(message("SelectToHandle"));
                return false;
            }
            dataColsIndices.addAll(checkedColsIndices);

            outputColumns = new ArrayList<>();
            outputColumns.add(new Data2DColumn(message("RowNumber"), ColumnDefinition.ColumnType.String));
            outputColumns.addAll(sourceController.checkedCols());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public String chartTitle() {
        return null;
    }

    public String categoryName() {
        return categoryColumnSelector.getSelectionModel().getSelectedItem();
    }

    @FXML
    @Override
    public void okAction() {
        if (!checkOptions() || !initData()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(task);
                    readData();
                    return outputData != null && !outputData.isEmpty();
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                outputData();
            }

        };
        start(task);
    }

    public void readData() {
        if (sourceController.allPages()) {
            outputData = data2D.allRows(dataColsIndices, true);
        } else {
            outputData = sourceController.selectedData(sourceController.checkedRowsIndices(), dataColsIndices, true);
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

    public Map<String, String> makePalette() {
        try {
            Random random = new Random();
            if (palette == null) {
                palette = new HashMap();
            } else {
                palette.clear();
            }
            for (int i = 0; i < outputColumns.size(); i++) {
                Data2DColumn column = outputColumns.get(i);
                Color color = column.getColor();
                if (color == null) {
                    color = FxColorTools.randomColor(random);
                }
                String rgb = FxColorTools.color2rgb(color);
                palette.put(column.getColumnName(), rgb);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return palette;
    }

    public void redrawChart() {
        drawChart();
    }

    @FXML
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

    @Override
    public void cleanPane() {
        try {
            tableController.statusNotify.removeListener(tableStatusListener);
            tableController.loadedNotify.removeListener(tableLoadListener);
            tableStatusListener = null;
            tableLoadListener = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        get/set
     */
    public int getScale() {
        return scale;
    }

    public Map<String, String> getPalette() {
        return palette;
    }

}
