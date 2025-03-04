package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-4-21
 * @License Apache License Version 2.0
 */
public class ControlData2DSimpleLinearRegressionTable extends BaseData2DLoadController {

    protected BaseData2DRegressionController regressController;
    protected TableColumn sortColumn;

    @Override
    public void initValues() {
        try {
            super.initValues();
            data2D = Data2D.create(Data2DDefinition.DataType.Texts);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public List<Data2DColumn> createColumns() {
        try {
            List<Data2DColumn> cols = new ArrayList<>();
            cols.add(new Data2DColumn(message("DependentVariable"), ColumnType.String, 100));
            cols.add(new Data2DColumn(message("IndependentVariable"), ColumnType.String, 100));
            cols.add(new Data2DColumn(message("CoefficientOfDetermination"), ColumnType.Double, 80));
            cols.add(new Data2DColumn(message("PearsonsR"), ColumnType.Double, 80));
            cols.add(new Data2DColumn(message("Model"), ColumnType.String, 300));
            cols.add(new Data2DColumn(message("Slope"), ColumnType.Double, 100));
            cols.add(new Data2DColumn(message("Intercept"), ColumnType.Double, 100));

            return cols;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public void makeColumns() {
        try {
            List<Data2DColumn> cols = createColumns();
            data2D.setColumns(cols);
            super.makeColumns();
            sortColumn = tableView.getColumns().get(3);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseData2DRegressionController regressController) {
        try {
            this.regressController = regressController;
            makeColumns();
            checkButtons();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void addRow(List<String> row) {
        if (row == null) {
            return;
        }
        row.add(0, "" + (tableData.size() + 1));
        isSettingValues = true;
        tableData.add(row);
        isSettingValues = false;
    }

    public void afterRegression() {
        isSettingValues = true;
        tableView.getSortOrder().clear();
        if (sortColumn != null) {
            sortColumn.setSortType(TableColumn.SortType.DESCENDING);
            tableView.getSortOrder().add(sortColumn);
        }
        isSettingValues = false;
        checkButtons();
    }

    public List<String> selected() {
        return selectedItem();
    }

    @Override
    protected void checkButtons() {
        super.checkButtons();
        if (regressController == null) {
            return;
        }
        regressController.dataButton.setDisable(tableData.isEmpty());
        regressController.viewButton.setDisable(false);
    }

    @FXML
    @Override
    public void dataAction() {
        if (tableData.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        Data2DManufactureController.openData(data2D.getName(),
                data2D.getColumns(), data2D.pageData());
    }

    @FXML
    @Override
    public void editAction() {
        if (regressController == null) {
            return;
        }
        List<String> selected = selected();
        if (selected == null) {
            Data2DSimpleLinearRegressionController.open(regressController.dataController);
        } else {
            try {
                Data2DSimpleLinearRegressionController controller = (Data2DSimpleLinearRegressionController) WindowTools
                        .operationStage(regressController.parentController, Fxmls.Data2DSimpleLinearRegressionFxml);
                controller.categoryColumnSelector.getItems().setAll(selected.get(2));
                controller.categoryColumnSelector.getSelectionModel().select(0);
                controller.valueColumnSelector.getItems().setAll(selected.get(1));
                controller.valueColumnSelector.getSelectionModel().select(0);
                controller.interceptCheck.setSelected(regressController.interceptCheck.isSelected());
                controller.alphaSelector.getSelectionModel().select(regressController.alpha + "");
                controller.cloneOptions(regressController);
                controller.setParameters(regressController.dataController);
                controller.startAction();
                controller.requestMouse();
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
        }
    }

}
