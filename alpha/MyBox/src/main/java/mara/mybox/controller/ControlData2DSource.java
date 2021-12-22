package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Toggle;
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
    protected final SimpleBooleanProperty changeNotify;
    protected List<Integer> checkedRowsIndices, checkedColsIndices;

    @FXML
    protected HBox rowGroupBox;
    @FXML
    protected ToggleGroup rowGroup;
    @FXML
    protected RadioButton rowAllRadio, rowTableRadio;

    public ControlData2DSource() {
        forDisplay = true;
        changeNotify = new SimpleBooleanProperty(false);
    }

    public void setParameters(Data2DOperateController opController) {
        try {
            this.opController = opController;
            this.loadController = opController.loadController;
            setData(opController.data2D);

            dataNameLabel = opController.dataNameLabel;

            loadController.statusNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (!loadController.isSettingValues) {
                        loadData();
                    }
                }
            });

            rowGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    notifyChange();
                }
            });

            tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    notifyChange();
                }
            });

            loadData();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void notifyChange() {
        if (isSettingValues) {
            return;
        }
        changeNotify.set(!changeNotify.get());
    }

    @Override
    public void makeColumns() {
        try {
            if (!validateData()) {
                return;
            }
            super.makeColumns();
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = new CheckBox(tableColumn.getText());
                cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        if (!isSettingValues) {
                            notifyChange();
                        }
                    }
                });
                tableColumn.setGraphic(cb);
                tableColumn.setText(null);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void tableChanged(boolean changed) {
        validateData();
        notifyChange();
    }

    @Override
    public void loadData() {
        try {
            if (!validateData()) {
                notifyChange();
                return;
            }
            makeColumns();
            tableData.setAll(loadController.tableData);
            dataNameLabel.setText(data2D.displayName());
            restoreSource();
            notifyChange();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void selectAllCols() {
        try {
            isSettingValues = true;
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                cb.setSelected(true);
            }
            isSettingValues = false;
            notifyChange();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void selectNoneCols() {
        try {
            isSettingValues = true;
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                cb.setSelected(false);
            }
            isSettingValues = false;
            notifyChange();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        loadData();
    }

    public boolean isAllData() {
        return rowAllRadio.isSelected();
    }

    public List<Integer> checkedRowsIndices() {
        try {
            checkedRowsIndices = new ArrayList<>();
            if (isAllData()) {
                for (int i = 0; i < tableData.size(); i++) {
                    checkedRowsIndices.add(i);
                }
            } else {
                checkedRowsIndices = tableView.getSelectionModel().getSelectedIndices();
            }
            return checkedRowsIndices;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public List<Integer> checkedColsIndices() {
        try {
            checkedColsIndices = new ArrayList<>();
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                if (cb.isSelected()) {
                    int col = data2D.colOrder(cb.getText());
                    if (col >= 0) {
                        checkedColsIndices.add(col);
                    }
                }
            }
            return checkedColsIndices;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public boolean checkSource() {
        checkedRowsIndices = checkedRowsIndices();
        checkedColsIndices = checkedColsIndices();
        return checkedRowsIndices == null || checkedRowsIndices.isEmpty()
                || checkedColsIndices == null || checkedColsIndices.isEmpty();
    }

    public boolean isSquare() {
        return checkedRowsIndices != null && checkedColsIndices != null
                && checkedRowsIndices.size() == checkedColsIndices.size();
    }

    public void checkRows(List<Integer> rows) {
        try {
            isSettingValues = true;
            tableView.getSelectionModel().clearSelection();
            if (rows != null && !rows.isEmpty()) {
                int size = tableData.size();
                for (Integer r : rows) {
                    if (r >= 0 && r < size) {
                        tableView.getSelectionModel().select(r);
                    }
                }
            }
            isSettingValues = false;
            notifyChange();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void checkCols(List<Integer> cols) {
        try {
            isSettingValues = true;
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                if (cols != null && !cols.isEmpty()) {
                    int col = data2D.colOrder(cb.getText());
                    cb.setSelected(col >= 0 && cols.contains(col));
                } else {
                    cb.setSelected(false);
                }
            }
            isSettingValues = false;
            notifyChange();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void restoreSource() {
        checkRows(checkedRowsIndices);
        checkCols(checkedColsIndices);
    }

}
