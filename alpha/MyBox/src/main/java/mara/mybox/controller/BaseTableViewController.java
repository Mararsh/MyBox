package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.cell.TableRowSelectionCell;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-7-7
 * @License Apache License Version 2.0
 */
public abstract class BaseTableViewController<P> extends BaseController {

    protected ObservableList<P> tableData;
    protected SimpleBooleanProperty tableDataChangedNotify;
    protected boolean isSettingTable;

    @FXML
    protected TableView<P> tableView;
    @FXML
    protected TableColumn<P, Boolean> rowsSelectionColumn;
    @FXML
    protected CheckBox allRowsCheck, lostFocusCommitCheck;

    @Override
    public void initValues() {
        try {
            super.initValues();

            tableDataChangedNotify = new SimpleBooleanProperty(false);
            tableData = FXCollections.observableArrayList();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initTable();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initTable() {
        try {
            if (tableView == null) {
                return;
            }
            tableView.setItems(tableData);
            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            tableData.addListener((ListChangeListener.Change<? extends P> change) -> {
                tableChanged();
            });

            initColumns();

            if (lostFocusCommitCheck != null) {
                isSettingTable = true;
                lostFocusCommitCheck.setSelected(AppVariables.commitModificationWhenDataCellLoseFocus);
                isSettingTable = false;
                thisPane.hoverProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        hovering(newValue);
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initColumns() {
        try {
            if (allRowsCheck != null) {
                allRowsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        if (newValue) {
                            tableView.getSelectionModel().selectAll();
                        } else {
                            tableView.getSelectionModel().clearSelection();
                        }
                    }
                });
            }

            if (rowsSelectionColumn != null) {
                tableView.setEditable(true);
                rowsSelectionColumn.setCellFactory(TableRowSelectionCell.create(tableView));

                rowsSelectionColumn.setPrefWidth(UserConfig.getInt("RowsSelectionColumnWidth", 100));
                rowsSelectionColumn.widthProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> o, Number ov, Number nv) {
                        UserConfig.setInt("RowsSelectionColumnWidth", nv.intValue());
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        tableview
     */
    protected void hovering(boolean isHovering) {
        if (isHovering && lostFocusCommitCheck != null) {
            isSettingTable = true;
            lostFocusCommitCheck.setSelected(AppVariables.commitModificationWhenDataCellLoseFocus);
            isSettingTable = false;
        }
    }

    /*
        data
     */
    protected void tableChanged() {
        tableChanged(true);
    }

    public void tableChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        if (changed) {
            tableDataChangedNotify.set(!tableDataChangedNotify.get());
        }
    }

    @FXML
    public void autoCommitCheck() {
        if (!isSettingTable && lostFocusCommitCheck != null) {
            AppVariables.lostFocusCommitData(lostFocusCommitCheck.isSelected());
        }
    }

    /*
        selection
     */
    public void selectNone() {
        if (allRowsCheck != null) {
            allRowsCheck.setSelected(false);
        } else {
            tableView.getSelectionModel().clearSelection();
        }
    }

    public void selectAll() {
        if (allRowsCheck != null) {
            allRowsCheck.setSelected(true);
        } else {
            tableView.getSelectionModel().selectAll();
        }
    }

    protected boolean isNoneSelected() {
        return tableView.getSelectionModel().getSelectedIndices().isEmpty();
    }

    protected List<P> selectedItems() {
        try {
            List<P> items = tableView.getSelectionModel().getSelectedItems();
            if (items != null) {
                return items;
            }
            List<Integer> selected = tableView.getSelectionModel().getSelectedIndices();
            if (selected != null && !selected.isEmpty()) {
                items = new ArrayList<>();
                for (int index : selected) {
                    items.add(tableData.get(index));
                }
                return items;
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
        return null;
    }

    protected int selectedIndix() {
        try {
            int index = tableView.getSelectionModel().getSelectedIndex();
            if (index >= 0 && index < tableData.size()) {
                return index;
            }
            List<Integer> selected = tableView.getSelectionModel().getSelectedIndices();
            if (selected != null && !selected.isEmpty()) {
                return selected.get(0);
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
        return -1;
    }

    protected P selectedItem() {
        try {
            int index = selectedIndix();
            if (index >= 0 && index < tableData.size()) {
                return tableData.get(index);
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
        return null;
    }

    /*
        actions
     */
    @FXML
    @Override
    public void deleteAction() {
        try {
            List<P> selected = tableView.getSelectionModel().getSelectedItems();
            if (selected == null || selected.isEmpty()) {
                popError(message("SelectToHandle"));
                return;
            }
            tableData.removeAll(selected);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void clearAction() {
        tableData.clear();
    }

    @FXML
    public void moveUpAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(tableView.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        isSettingValues = true;
        List<Integer> newselected = new ArrayList<>();
        for (Integer index : selected) {
            if (index == 0 || newselected.contains(index - 1)) {
                newselected.add(index);
                continue;
            }
            P current = tableData.get(index);
            P previous = tableData.get(index - 1);
            tableData.set(index, previous);
            tableData.set(index - 1, current);
            newselected.add(index - 1);
        }
        tableView.getSelectionModel().clearSelection();
        for (Integer index : newselected) {
            tableView.getSelectionModel().select(index);
        }
        tableView.refresh();
        isSettingValues = false;
        tableChanged(true);
    }

    @FXML
    public void moveDownAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(tableView.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        isSettingValues = true;
        List<Integer> newselected = new ArrayList<>();
        for (int i = selected.size() - 1; i >= 0; --i) {
            int index = selected.get(i);
            if (index == tableData.size() - 1
                    || newselected.contains(index + 1)) {
                newselected.add(index);
                continue;
            }
            P current = tableData.get(index);
            P next = tableData.get(index + 1);
            tableData.set(index, next);
            tableData.set(index + 1, current);
            newselected.add(index + 1);
        }
        tableView.getSelectionModel().clearSelection();
        for (Integer index : newselected) {
            tableView.getSelectionModel().select(index);
        }
        isSettingValues = false;
        tableView.refresh();
        tableChanged(true);
    }

    @FXML
    public void moveTopAction() {
        List<P> selected = new ArrayList<>();
        selected.addAll(selectedItems());
        if (selected.isEmpty()) {
            return;
        }
        isSettingValues = true;
        tableData.removeAll(selected);
        tableData.addAll(0, selected);
        tableView.getSelectionModel().clearSelection();
        tableView.getSelectionModel().selectRange(0, selected.size());
        tableView.refresh();
        isSettingValues = false;
        tableChanged(true);
    }

}
