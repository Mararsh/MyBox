package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;

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
    protected RadioButton transposeRadio, sumRadio, addRadio, subRadio, multiplyRadio,
            ascendingRadio, descendingRadio, mergeRadio, copyRadio;
    @FXML
    protected ControlListCheckBox calColsListController;
    @FXML
    protected Button calculatorButton;

    @Override
    public void setParameters(ControlSheet sheetController, int row, int col) {
        try {
            super.setParameters(sheetController, row, col);

            calColsListController.setParent(sheetController);

            calGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {

                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean isNumberOperation() {
        return sumRadio.isSelected() || addRadio.isSelected() || subRadio.isSelected() || multiplyRadio.isSelected();
    }

    public void checkOperation() {
        try {
            List<String> cols = new ArrayList<>();
            if (sheetController.columns != null) {
                for (ColumnDefinition c : sheetController.columns) {
                    if (!isNumberOperation() || c.isNumberType()) {
                        cols.add(c.getName());
                    }
                }
            }
            calColsListController.setValues(cols);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void calculationAction() {
        try {

            List<Integer> cols = cols();
            if (rowAllRadio.isSelected()) {
                sheetController.setCols(cols, value);

            } else {
                sheetController.setRowsCols(rows(), cols, value);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
