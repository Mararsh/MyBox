package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.calculation.DescriptiveStatistic;
import mara.mybox.calculation.DescriptiveStatistic.StatisticObject;
import mara.mybox.calculation.DoubleStatistic;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.TmpTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-12
 * @License Apache License Version 2.0
 */
public class Data2DStatisticController extends BaseData2DTargetsController {

    protected DescriptiveStatistic calculation;
    protected int categorysCol;
    protected String selectedCategory;

    @FXML
    protected ControlStatisticSelection statisticController;
    @FXML
    protected VBox dataOptionsBox;
    @FXML
    protected FlowPane categoryColumnsPane;
    @FXML
    protected ComboBox<String> categoryColumnSelector;

    public Data2DStatisticController() {
        baseTitle = message("DescriptiveStatistics");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            categoryColumnSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkOptions();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void refreshControls() {
        try {
            super.refreshControls();

            List<String> names = tableController.data2D.columnNames();
            if (names == null || names.isEmpty()) {
                return;
            }
            isSettingValues = true;

            selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
            names.add(0, message("RowNumber"));
            categoryColumnSelector.getItems().setAll(names);
            if (selectedCategory != null && names.contains(selectedCategory)) {
                categoryColumnSelector.setValue(selectedCategory);
            } else {
                categoryColumnSelector.getSelectionModel().select(0);
            }
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void objectChanged() {
        super.objectChanged();
        if (rowsRadio.isSelected()) {
            if (!dataOptionsBox.getChildren().contains(categoryColumnsPane)) {
                dataOptionsBox.getChildren().add(1, categoryColumnsPane);
            }
        } else {
            if (dataOptionsBox.getChildren().contains(categoryColumnsPane)) {
                dataOptionsBox.getChildren().remove(categoryColumnsPane);
            }
        }
    }

    @Override
    public boolean initData() {
        try {
            if (!super.initData()) {
                return false;
            }
            categorysCol = -1;
            if (rowsRadio.isSelected()) {
                selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
                if (selectedCategory != null && categoryColumnSelector.getSelectionModel().getSelectedIndex() != 0) {
                    categorysCol = data2D.colOrder(selectedCategory);
                }
            }
            calculation = statisticController.pickValues()
                    .setScale(scale)
                    .setInvalidAs(invalidAs)
                    .setHandleController(this)
                    .setData2D(data2D)
                    .setColsIndices(checkedColsIndices)
                    .setColsNames(checkedColsNames)
                    .setCategoryName(categorysCol >= 0 ? selectedCategory : null);
            switch (objectType) {
                case Rows:
                    calculation.setStatisticObject(StatisticObject.Rows);
                    break;
                case All:
                    calculation.setStatisticObject(StatisticObject.All);
                    break;
                default:
                    calculation.setStatisticObject(StatisticObject.Columns);
                    break;
            }
            UserConfig.setBoolean(baseName + "SkipNonnumeric", skipNonnumericRadio.isSelected());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    protected void startOperation() {
        try {
            if (!calculation.prepare()) {
                return;
            }
            data2D.resetStatistic();
            if (isAllPages()) {
                switch (objectType) {
                    case Rows:
                        handleAllTask();
                        break;
                    case All:
                        handleAllByAllTask();
                        break;
                    default:
                        handleAllByColumnsTask();
                        break;
                }
            } else {
                handleRowsTask();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public boolean handleRows() {
        List<Integer> colsIndices = checkedColsIndices;
        if (rowsRadio.isSelected() && categorysCol >= 0) {
            colsIndices.add(0, categorysCol);
        }
        if (!calculation.statisticData(tableFiltered(colsIndices, rowsRadio.isSelected() && categorysCol < 0))) {
            return false;
        }
        outputColumns = calculation.getOutputColumns();
        outputData = calculation.getOutputData();
        return true;
    }

    public void handleAllByColumnsTask() {
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(this, filterController.filter);
                    calculation.setTask(this);
                    if (calculation.needStored()) {
                        TmpTable tmpTable = TmpTable.toStatisticTable(data2D, this, checkedColsIndices, invalidAs);
                        if (tmpTable == null) {
                            return false;
                        }
                        tmpTable.startTask(this, null);
                        calculation.setData2D(tmpTable)
                                .setColsIndices(tmpTable.columnIndices().subList(1, tmpTable.columnsNumber()))
                                .setColsNames(tmpTable.columnNames().subList(1, tmpTable.columnsNumber()));
                        ok = calculation.statisticAllByColumns();
                        tmpTable.stopFilter();
                        tmpTable.drop();
                    } else {
                        ok = calculation.statisticAllByColumnsWithoutStored();
                    }
                    data2D.stopFilter();
                    return ok;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                outputColumns = calculation.getOutputColumns();
                outputData = calculation.getOutputData();
                if (targetController.inTable()) {
                    updateTable();
                } else {
                    outputExternal();
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                calculation.setTask(null);
                if (targetController != null) {
                    targetController.refreshControls();
                }
            }

        };
        start(task);
    }

    public void handleAllByAllTask() {
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(this, filterController.filter);
                    calculation.setTask(this);
                    if (calculation.needStored()) {
                        DataTable dataTable = data2D.singleColumn(this, checkedColsIndices);
                        if (dataTable == null) {
                            return false;
                        }
                        dataTable.startTask(this, null);
                        calculation.setTask(this);
                        calculation.setData2D(dataTable)
                                .setColsIndices(dataTable.columnIndices().subList(1, 2))
                                .setColsNames(dataTable.columnNames().subList(1, 2));
                        ok = calculation.statisticAllByColumns();
                        dataTable.stopFilter();
                        return ok;
                    } else {
                        DoubleStatistic statisticData = data2D.statisticByAllWithoutStored(checkedColsIndices, calculation);
                        if (statisticData == null) {
                            return false;
                        }
                        calculation.statisticByColumnsWrite(statisticData);
                        data2D.stopFilter();
                        return true;
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                outputColumns = calculation.getOutputColumns();
                outputData = calculation.getOutputData();
                if (targetController.inTable()) {
                    updateTable();
                } else {
                    outputExternal();
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                calculation.setTask(null);
                if (targetController != null) {
                    targetController.refreshControls();
                }
            }

        };
        start(task);
    }

    @Override
    public DataFileCSV generatedFile() {
        List<Integer> colsIndices = checkedColsIndices;
        if (rowsRadio.isSelected() && categorysCol >= 0) {
            colsIndices.add(0, categorysCol);
        }
        return data2D.statisticByRows(targetController.name(),
                calculation.getOutputNames(), colsIndices, calculation);
    }

    /*
        static
     */
    public static Data2DStatisticController open(BaseData2DLoadController tableController) {
        try {
            Data2DStatisticController controller = (Data2DStatisticController) WindowTools.branchStage(
                    tableController, Fxmls.Data2DStatisticFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
