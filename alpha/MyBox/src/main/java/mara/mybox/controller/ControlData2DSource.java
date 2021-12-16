package mara.mybox.controller;

import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2DSource extends ControlData2DEditTable {

    protected Data2DOperateController opController;
    protected ControlData2DEditTable tableController;
    protected Label dataNameLabel;

    @FXML
    protected HBox rowGroupBox;
    @FXML
    protected ToggleGroup rowGroup;
    @FXML
    protected RadioButton rowAllRadio, rowTableRadio;

    public ControlData2DSource() {
    }

    public void setParameters(Data2DOperateController opController) {
        try {
            this.opController = opController;
            this.data2D = opController.data2D;
            this.tableController = opController.tableController;
            tableData2DDefinition = tableController.tableData2DDefinition;
            tableData2DColumn = tableController.tableData2DColumn;

            dataNameLabel = opController.dataNameLabel;

            setControls();

            data2D.getTableChangedNotify().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    MyBoxLog.debug(newValue + "  " + tableController.isSettingValues);
                    if (!tableController.isSettingValues) {
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
    }

    @Override
    public void loadData() {
        try {
            if (data2D == null) {
                return;
            }
            makeColumns();
            String name;
            if (data2D.getFile() != null) {
                name = data2D.getFile().getAbsolutePath();
            } else {
                name = data2D.getDataName();
            }
            if (name == null) {
                name = message("NewData");
            }
            dataNameLabel.setText(message(data2D.getType().name()) + " - "
                    + (data2D.getD2did() >= 0 ? data2D.getD2did() + " - " : "")
                    + name);

            tableData.setAll(tableController.tableData);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void makeColumns() {
        try {
            isSettingValues = true;
            tableData.clear();
            tableView.getColumns().remove(2, tableView.getColumns().size());
            tableView.setItems(tableData);
            isSettingValues = false;

            if (!checkData()) {
                return;
            }
            List<Data2DColumn> columns = data2D.getColumns();
            for (int i = 0; i < columns.size(); i++) {
                Data2DColumn dataColumn = columns.get(i);
                String name = dataColumn.getName();
                TableColumn tableColumn = new TableColumn<List<String>, String>();
                tableColumn.setGraphic(new CheckBox(name));
                tableColumn.setEditable(false);
                tableColumn.setUserData(dataColumn.getIndex());
                int col = i + 1;

                tableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<List<String>, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<List<String>, String> param) {
                        try {
                            List<String> row = (List<String>) param.getValue();
                            String value = row.get(col);
                            if (value == null) {
                                return null;
                            }
                            return new SimpleStringProperty(value);
                        } catch (Exception e) {
                            return null;
                        }
                    }
                });
                tableView.getColumns().add(tableColumn);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
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

    @Override
    public boolean checkBeforeNextAction() {
        return true;
    }

}
