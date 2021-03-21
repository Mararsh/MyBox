package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2021-3-19
 * @License Apache License Version 2.0
 */
public class TableSizeController extends BaseController {

    protected SimpleBooleanProperty notify;
    protected int colsNumber, rowsNumber;

    @FXML
    protected ComboBox<String> colsSelector, rowsSelector;

    public TableSizeController() {
        baseTitle = AppVariables.message("Table");
    }

    public void setValues(BaseController parent) {
        try {
            parentController = parent;
            baseName = parent.baseName;
            notify = new SimpleBooleanProperty();

            colsSelector.getItems().addAll(Arrays.asList(
                    "3", "5", "4", "6", "2", "1", "10", "8", "9", "7", "15", "20", "30"
            ));
            colsNumber = AppVariables.getUserConfigInt(baseName + "ColsNumber", 3);
            colsNumber = colsNumber <= 0 ? 3 : colsNumber;
            colsSelector.setValue(colsNumber + "");
            colsSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue.trim());
                            if (v <= 0) {
                                colsSelector.getEditor().setStyle(badStyle);
                            } else {
                                colsNumber = v;
                                AppVariables.setUserConfigInt(baseName + "ColsNumber", colsNumber);
                                colsSelector.getEditor().setStyle(null);
                            }
                        } catch (Exception e) {
                            colsSelector.getEditor().setStyle(badStyle);
                        }
                    });

            rowsSelector.getItems().addAll(Arrays.asList(
                    "3", "5", "4", "6", "2", "1", "10", "8", "9", "7", "15", "20", "30", "50"
            ));
            rowsNumber = AppVariables.getUserConfigInt(baseName + "RowsNumber", 3);
            rowsNumber = rowsNumber <= 0 ? 3 : rowsNumber;
            rowsSelector.setValue(rowsNumber + "");
            rowsSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue.trim());
                            if (v <= 0) {
                                rowsSelector.getEditor().setStyle(badStyle);
                            } else {
                                rowsNumber = v;
                                AppVariables.setUserConfigInt(baseName + "RowsNumber", rowsNumber);
                                rowsSelector.getEditor().setStyle(null);
                            }
                        } catch (Exception e) {
                            rowsSelector.getEditor().setStyle(badStyle);
                        }
                    });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());

        }
    }

    @FXML
    @Override
    public void okAction() {
        notify.set(!notify.get());
    }

    @FXML
    @Override
    public void cancelAction() {
        closeStage();
    }

}
