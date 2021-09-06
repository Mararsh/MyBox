package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

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
    @FXML
    protected Label titleLabel;

    public TableSizeController() {
        baseTitle = Languages.message("Table");
    }

    public void setParameters(BaseController parent, String title) {
        try {
            parentController = parent;
            if (parent != null) {
                baseName = parent.baseName;
            }
            getMyStage().centerOnScreen();
            titleLabel.setText(title);

            notify = new SimpleBooleanProperty();

            colsSelector.getItems().addAll(Arrays.asList(
                    "3", "5", "4", "6", "2", "1", "10", "8", "9", "7", "15", "20", "30"
            ));
            colsNumber = UserConfig.getInt(baseName + "ColsNumber", 3);
            colsNumber = colsNumber <= 0 ? 3 : colsNumber;
            colsSelector.setValue(colsNumber + "");
            colsSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                try {
                    int v = Integer.parseInt(newValue.trim());
                    if (v <= 0) {
                        colsSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                    } else {
                        colsNumber = v;
                        UserConfig.setInt(baseName + "ColsNumber", colsNumber);
                        colsSelector.getEditor().setStyle(null);
                    }
                } catch (Exception e) {
                    colsSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                }
            });

            rowsSelector.getItems().addAll(Arrays.asList(
                    "3", "5", "4", "6", "2", "1", "10", "8", "9", "7", "15", "20", "30", "50"
            ));
            rowsNumber = UserConfig.getInt(baseName + "RowsNumber", 3);
            rowsNumber = rowsNumber <= 0 ? 3 : rowsNumber;
            rowsSelector.setValue(rowsNumber + "");
            rowsSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                try {
                    int v = Integer.parseInt(newValue.trim());
                    if (v <= 0) {
                        rowsSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                    } else {
                        rowsNumber = v;
                        UserConfig.setInt(baseName + "RowsNumber", rowsNumber);
                        rowsSelector.getEditor().setStyle(null);
                    }
                } catch (Exception e) {
                    rowsSelector.getEditor().setStyle(NodeStyleTools.badStyle);
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
