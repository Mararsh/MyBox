package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-9-7
 * @License Apache License Version 2.0
 */
public class SheetCalculateController extends BaseDataOperationController {

    protected String value;

    @FXML
    protected RadioButton zeroRadio, oneRadio, blankRadio, randomRadio, setRadio;
    @FXML
    protected TextField valueInput;
    @FXML
    protected Button exampleCalculationColumnsButton, exampleDisplayColumnsButton, calculatorButton;

    @Override
    public void setParameters(ControlSheet sheetController, int row, int col) {
        try {
            super.setParameters(sheetController, row, col);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void calculationAction() {
        try {
            if (randomRadio.isSelected()) {
                value = null;
            }

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
