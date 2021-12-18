package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2DSource extends ControlData2DLoad {

    protected Data2DOperateController opController;
    protected ControlData2DLoad loadController;
    protected Label dataNameLabel;

    @FXML
    protected HBox rowGroupBox;
    @FXML
    protected ToggleGroup rowGroup;
    @FXML
    protected RadioButton rowAllRadio, rowTableRadio;

    public ControlData2DSource() {
        forDisplay = true;
    }

    public void setParameters(Data2DOperateController opController) {
        try {
            this.opController = opController;
            this.loadController = opController.loadController;
            setData(opController.data2D.cloneAll());

            dataNameLabel = opController.dataNameLabel;

            loadController.statusNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (!loadController.isSettingValues) {
                        loadData();
                    }
                }
            });

            loadData();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void tableChanged(boolean changed) {
        validateData();
    }

    @Override
    public void loadData() {
        try {
            if (data2D == null) {
                return;
            }
            makeColumns();
            tableData.setAll(loadController.tableData);

            dataNameLabel.setText(data2D.displayName());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void selectAllCols() {
        try {
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                cb.setSelected(true);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void selectNoneCols() {
        try {
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                cb.setSelected(false);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        loadData();
    }

}
