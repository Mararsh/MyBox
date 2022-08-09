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
public class ControlData2DRegressionTable extends ControlData2DLoad {

    protected Data2DSimpleLinearRegressionCombinationController regressController;

    @Override
    public void initControls() {
        try {
            super.initControls();

            data2D = Data2D.create(Data2DDefinition.Type.Texts);
            List<Data2DColumn> cols = new ArrayList<>();
            cols.add(new Data2DColumn(message("DependentVariable"), ColumnType.String, 100));
            cols.add(new Data2DColumn(message("IndependentVariable"), ColumnType.String, 100));
            cols.add(new Data2DColumn(message("CoefficientOfDetermination"), ColumnType.Double, 80));
            cols.add(new Data2DColumn(message("PearsonsR"), ColumnType.Double, 80));
            cols.add(new Data2DColumn(message("Model"), ColumnType.String, 300));
            cols.add(new Data2DColumn(message("Slope"), ColumnType.Double, 100));
            cols.add(new Data2DColumn(message("Intercept"), ColumnType.Double, 100));
            data2D.setColumns(cols);
            makeColumns();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(Data2DSimpleLinearRegressionCombinationController regressController) {
        try {
            this.regressController = regressController;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void clear() {
        tableData.clear();
    }

    public void addRow(List<String> row) {
        if (row == null) {
            return;
        }
        row.add(0, "" + (tableData.size() + 1));
        tableData.add(row);
    }

    public void sortR() {
        tableView.getSortOrder().clear();
        TableColumn rColumn = tableView.getColumns().get(4);
        rColumn.setSortType(TableColumn.SortType.DESCENDING);
        tableView.getSortOrder().add(rColumn);
    }

    public List<String> selected() {
        return tableView.getSelectionModel().getSelectedItem();
    }

    public void editTable() {
        DataManufactureController.open(data2D.getColumns(), data2D.tableRowsWithoutNumber());
    }

    @FXML
    @Override
    public void editAction() {
        List<String> selected = selected();
        if (selected == null) {
            Data2DSimpleLinearRegressionController.open(regressController.tableController);
        } else {
            try {
                Data2DSimpleLinearRegressionController controller = (Data2DSimpleLinearRegressionController) WindowTools.openChildStage(
                        regressController.parentController.getMyWindow(), Fxmls.Data2DSimpleLinearRegressionFxml, false);
                controller.categoryColumnSelector.getItems().setAll(selected.get(2));
                controller.categoryColumnSelector.getSelectionModel().select(0);
                controller.valueColumnSelector.getItems().setAll(selected.get(1));
                controller.valueColumnSelector.getSelectionModel().select(0);
                controller.interceptCheck.setSelected(regressController.interceptCheck.isSelected());
                controller.alphaSelector.getSelectionModel().select(regressController.alpha + "");
                controller.cloneOptions(regressController);
                controller.setParameters(regressController.tableController);
                controller.requestMouse();
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
            }
        }
    }

}
