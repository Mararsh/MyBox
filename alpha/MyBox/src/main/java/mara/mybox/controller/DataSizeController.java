package mara.mybox.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;

/**
 * @Author Mara
 * @CreateDate 2021-9-5
 * @License Apache License Version 2.0
 */
public class DataSizeController extends BaseController {

    protected ControlSheet sheetController;
    protected int rowsNumer, colsNumber;

    @FXML
    protected TextField rowsInput, colsInput;

    @Override
    public void setStageStatus() {
    }

    public void setParameters(ControlSheet sheetController) {
        try {
            this.sheetController = sheetController;
            this.baseName = sheetController.baseName;

            rowsNumer = (int) sheetController.rowsTotal();
            colsNumber = sheetController.columns.size();

            rowsInput.setText(rowsNumer + "");
            colsInput.setText(colsNumber + "");

            rowsInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                    try {
                        int v = Integer.parseInt(newv);
                        if (v >= 0) {
                            rowsNumer = v;
                            rowsInput.setStyle(null);
                        } else {
                            rowsInput.setStyle(NodeStyleTools.badStyle);
                        }
                    } catch (Exception e) {
                        rowsInput.setStyle(NodeStyleTools.badStyle);
                    }
                }
            });

            colsInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                    try {
                        int v = Integer.parseInt(newv);
                        if (v >= 0) {
                            colsNumber = v;
                            colsInput.setStyle(null);
                        } else {
                            colsInput.setStyle(NodeStyleTools.badStyle);
                        }
                    } catch (Exception e) {
                        colsInput.setStyle(NodeStyleTools.badStyle);
                    }
                }
            });

            okButton.disableProperty().bind(Bindings.isEmpty(rowsInput.textProperty())
                    .or(rowsInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                    .or(Bindings.isEmpty(colsInput.textProperty()))
                    .or(colsInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
            );

            setAsPopup(baseName);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    @FXML
    public void plusRows() {
        rowsInput.setText((rowsNumer + 1) + "");
    }

    @FXML
    public void minusRows() {
        if (rowsNumer < 1) {
            return;
        }
        rowsInput.setText((rowsNumer - 1) + "");
    }

    @FXML
    public void plusCols() {
        colsInput.setText((colsNumber + 1) + "");
    }

    @FXML
    public void minusCols() {
        if (colsNumber < 1) {
            return;
        }
        colsInput.setText((colsNumber - 1) + "");
    }

    @FXML
    @Override
    public void okAction() {
        try {
            sheetController.resizeSheet(rowsNumer, colsNumber);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

}
