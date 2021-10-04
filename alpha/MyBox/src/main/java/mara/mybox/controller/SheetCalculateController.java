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

    public void checkOperation() {
        try {
            optionsBox.getChildren().clear();
            if (transposeRadio.isSelected()) {
                optionsBox.getChildren().addAll(rowsBox, columnsBox);
                colsLabel.setText(message("Columns"));

            } else if (statisticRadio.isSelected()) {
                if (calColsListController.getValues().isEmpty()) {
                    popError(message("NoNumberColumns"));
                    return;
                }
                optionsBox.getChildren().addAll(rowsBox, calColumnsBox, columnsBox);
                colsLabel.setText(message("ColumnsDisplay"));

            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
        synchronized (this) {
            SingletonTask calTask = new SingletonTask<Void>() {
                private String[][] data = null;
                private List<ColumnDefinition> dataColumns = null;

                @Override
                protected boolean handle() {
                    try {
                        List<Integer> cols = cols();

                        if (rowAllRadio.isSelected()) {

                        } else {
                            List<Integer> rows = rows();
                            if (transposeRadio.isSelected()) {
                                data = sheetController.transpose(rows, cols);

                            } else if (statisticRadio.isSelected()) {
                                List<Integer> calCols = selectedCols(calColsListController);
                                List<Integer> disCols = new ArrayList<>();
                                if (cols != null && !cols.isEmpty()) {
                                    for (Integer i : cols) {
                                        if (!calCols.contains(i)) {
                                            disCols.add(i);
                                        }
                                    }
                                }
                                data = sheetController.statistic(rows, calCols, disCols);
                                dataColumns = new ArrayList<>();
                                dataColumns.add(new ColumnDefinition(message("Calculation"), ColumnType.String));
                                for (Integer i : calCols) {
                                    dataColumns.add(sheetController.columns.get(i).cloneBase().setType(ColumnType.Double));
                                }
                                for (Integer i : disCols) {
                                    dataColumns.add(sheetController.columns.get(i).cloneBase());
                                }
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
