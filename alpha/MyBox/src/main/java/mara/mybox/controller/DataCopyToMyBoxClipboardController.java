package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-2
 * @License Apache License Version 2.0
 */
public class DataCopyToMyBoxClipboardController extends BaseDataOperationController {

    protected char delimiter;

    @FXML
    protected CheckBox colNameCheck;

    @Override
    public void setParameters(ControlSheet sheetController, int row, int col) {
        try {
            super.setParameters(sheetController, row, col);

            colNameCheck.setSelected(UserConfig.getBoolean(baseName + "Names", true));
            colNameCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Names", newValue);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            sheetController.copyDelimiter = delimiter;
            boolean withNames = colNameCheck.isSelected();

            List<Integer> cols = cols();
            if (rowAllRadio.isSelected()) {
                sheetController.copyCols(cols, withNames, false);

            } else {
                sheetController.copyRowsCols(rows(), cols, withNames, false);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

}
