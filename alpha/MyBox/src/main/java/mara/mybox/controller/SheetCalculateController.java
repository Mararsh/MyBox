package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-7
 * @License Apache License Version 2.0
 */
public class SheetCalculateController extends BaseDataOperationController {

    protected String value;

    @FXML
    protected ToggleGroup calGroup;
    @FXML
    protected RadioButton transposeRadio, statisticRadio, addRadio, subRadio, multiplyRadio,
            ascendingRadio, descendingRadio, mergeRadio, copyRadio;
    @FXML
    protected ControlListCheckBox calColsListController;
    @FXML
    protected Button calculatorButton;
    @FXML
    protected VBox optionsBox;
    @FXML
    protected HBox rowsBox, calColumnBox, calColumnsBox, columnsBox;
    @FXML
    protected Label colsLabel;

    @Override
    public void setParameters(ControlSheet sheetController, int row, int col) {
        try {
            super.setParameters(sheetController, row, col);

            calColsListController.setParent(sheetController);

            calGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkOperation();
                }
            });
            checkOperation();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void updateControls() {
        try {
            super.updateControls();

            List<String> cols = new ArrayList<>();
            if (sheetController.columns != null) {
                for (ColumnDefinition c : sheetController.columns) {
                    if (c.isNumberType()) {
                        cols.add(c.getName());
                    }
                }
            }
            calColsListController.setValues(cols);
            statisticRadio.setDisable(cols.isEmpty());
            addRadio.setDisable(cols.isEmpty());
            subRadio.setDisable(cols.isEmpty());
            multiplyRadio.setDisable(cols.isEmpty());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean checkOperation() {
        try {
            optionsBox.getChildren().clear();
            if (transposeRadio.isSelected()) {
                optionsBox.getChildren().addAll(rowsBox, columnsBox);
                colsLabel.setText(message("Columns"));

            } else if (statisticRadio.isSelected() || addRadio.isSelected()
                    || subRadio.isSelected() || multiplyRadio.isSelected()) {
                if (calColsListController.getValues().isEmpty()) {
                    popError(message("NoNumberColumns"));
                    return false;
                }
                optionsBox.getChildren().addAll(rowsBox, calColumnsBox, columnsBox);
                colsLabel.setText(message("ColumnsDisplay"));

            }
            return true;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public List<Integer> calCols() {
        List<String> names = calColsListController.checkedValues();
        if (names == null) {
            return null;
        }
        List<Integer> indices = new ArrayList<>();
        for (String name : names) {
            int index = sheetController.colIndex(name);
            if (index >= 0) {
                indices.add((Integer) index);
            }
        }
        return indices;
    }

    @FXML
    public void calculationAction() {
        List<Integer> cols = cols();
        List<Integer> calCols = selectedCols(calColsListController);
        List<Integer> rows = rows();
        List<Integer> disCols = new ArrayList<>();
        List<ColumnDefinition> dataColumns = new ArrayList<>();
        if (statisticRadio.isSelected() || addRadio.isSelected()
                || subRadio.isSelected() || multiplyRadio.isSelected()) {
            if (calCols == null || calCols.isEmpty()) {
                popError(message("SelectToHandle"));
                return;
            }
            if (cols != null && !cols.isEmpty()) {
                for (Integer i : cols) {
                    if (!calCols.contains(i)) {
                        disCols.add(i);
                    }
                }
            }
            // The column name can not start with "m_", or else errors popped by javafx class "CheckBoxSkin". I guess this is a bug of Javafx.
            // https://github.com/Mararsh/MyBox/issues/1222
            dataColumns.add(new ColumnDefinition("__" + message("CalculationName") + "__", ColumnType.String).setWidth(200));

            for (Integer i : calCols) {
                if (statisticRadio.isSelected()) {
                    dataColumns.add(new ColumnDefinition("__" + message("CalculationValue") + "__" + sheetController.columns.get(i).getName() + "__",
                            ColumnType.Double).setWidth(200));
                }
                dataColumns.add(sheetController.columns.get(i).cloneBase().setType(ColumnType.Double).setWidth(150));
            }
            for (Integer i : disCols) {
                dataColumns.add(sheetController.columns.get(i).cloneBase());
            }
        }
        synchronized (this) {
            SingletonTask calTask = new SingletonTask<Void>() {
                private String[][] data = null;

                @Override
                protected boolean handle() {
                    try {
                        if (rowAllRadio.isSelected()) {

                        } else {

                            if (transposeRadio.isSelected()) {
                                data = sheetController.transpose(rows, cols);

                            } else if (statisticRadio.isSelected()) {
                                data = sheetController.statistic(rows, calCols, disCols);

                            }
                        }

                        return data != null;
                    } catch (Exception e) {
                        error = e.toString();
                        MyBoxLog.error(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    DataFileCSVController controller = (DataFileCSVController) WindowTools.openStage(Fxmls.DataFileCSVFxml);
                    controller.loadData(data, dataColumns);
                }

            };
            start(calTask, false);
        }

    }

}
