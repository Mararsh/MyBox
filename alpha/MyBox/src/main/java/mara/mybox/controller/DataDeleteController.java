package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public class DataDeleteController extends BaseDataOperationController {

    @FXML
    protected ToggleGroup deleteGroup;
    @FXML
    protected RadioButton deleteRowsRadio, deleteColsRadio;

    @Override
    public void setParameters(ControlSheet sheetController, int row, int col) {
        try {
            super.setParameters(sheetController, row, col);

            deleteGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkDelete();
                }
            });
            checkDelete();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkDelete() {
        if (deleteRowsRadio.isSelected()) {
            if (!thisPane.getChildren().contains(rowBox)) {
                thisPane.getChildren().add(2, rowBox);
            }
            if (thisPane.getChildren().contains(colBox)) {
                thisPane.getChildren().remove(colBox);
            }
        } else {
            if (!thisPane.getChildren().contains(colBox)) {
                thisPane.getChildren().add(2, colBox);
            }
            if (thisPane.getChildren().contains(rowBox)) {
                thisPane.getChildren().remove(rowBox);
            }
        }
        refreshStyle();
    }

    @FXML
    @Override
    public void okAction() {
        try {
            if (!PopTools.askSure(message("Delete"), message("SureDelete"))) {
                return;
            }

            if (deleteRowsRadio.isSelected()) {
                if (rowCheckedRadio.isSelected()) {
                    sheetController.deleteRows(sheetController.rowsIndex(false));

                } else if (rowCurrentPageRadio.isSelected()) {
                    sheetController.deletePageRows();

                } else if (rowAllRadio.isSelected()) {
                    sheetController.deleteAllRows();

                } else if (rowSelectRadio.isSelected()) {
                    sheetController.deleteRows(selectedRows());
                }

            } else {
                if (colCheckedRadio.isSelected()) {
                    sheetController.deleteCols(sheetController.colsIndex(false));

                } else if (colAllRadio.isSelected()) {
                    sheetController.deleteAllCols();

                } else if (colSelectRadio.isSelected()) {
                    sheetController.deleteCols(selectedCols());
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
