package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-7-7
 * @License Apache License Version 2.0
 */
public abstract class BaseTableViewController<P> extends BaseController {

    protected ObservableList<P> tableData;
    protected SimpleBooleanProperty tableDataChangedNotify;

    @FXML
    protected TableView<P> tableView;
    @FXML
    protected CheckBox lostFocusCommitCheck;

    @Override
    public void initValues() {
        try {
            super.initValues();

            tableDataChangedNotify = new SimpleBooleanProperty(false);
            tableData = FXCollections.observableArrayList();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initTable();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

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
        if (lostFocusCommitCheck != null) {
            AppVariables.lostFocusCommitData(lostFocusCommitCheck.isSelected());
        }
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
