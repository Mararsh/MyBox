package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.tools.Data2DColumnTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.cell.TableRowSelectionCell;
import mara.mybox.fxml.image.FxImageTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-7-7
 * @License Apache License Version 2.0
 */
public abstract class BaseTableViewController<P> extends BaseFileController {

    protected ObservableList<P> tableData;
    protected boolean isSettingTable;
    protected SimpleBooleanProperty loadedNotify, selectedNotify;
    protected int editingIndex, viewingIndex;

    @FXML
    protected TableView<P> tableView;
    @FXML
    protected TableColumn<P, Boolean> rowsSelectionColumn;
    @FXML
    protected CheckBox allRowsCheck, lostFocusCommitCheck;
    @FXML
    protected Button moveUpButton, moveDownButton, moveTopButton, refreshButton,
            copyItemsButton, deleteItemsButton, clearItemsButton, insertItemButton;

    public BaseTableViewController() {
        TipsLabelKey = "TableTips";
        selectedNotify = new SimpleBooleanProperty(false);
        loadedNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            tableData = FXCollections.observableArrayList();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initButtons();
            initColumns();
            initTable();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        tableview
     */
    protected void initTable() {
        try {
            if (tableView == null) {
                return;
            }

            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>() {
                @Override
                public void onChanged(ListChangeListener.Change c) {
                    selectionChanged();
                }
            });

            tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (popMenu != null && popMenu.isShowing()) {
                        popMenu.hide();
                    }
                    if (event.getClickCount() > 1) {
                        doubleClicked(event);
                    } else if (event.getButton() == MouseButton.SECONDARY) {
                        rightClicked(event);
                    } else {
                        clicked(event);
                    }
                }
            });

            tableView.setItems(tableData);
            tableData.addListener(new ListChangeListener<P>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends P> c) {
                    tableChanged();
                }
            });

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

            checkSelected();

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

    protected void hovering(boolean isHovering) {
        if (isHovering && lostFocusCommitCheck != null) {
            isSettingTable = true;
            lostFocusCommitCheck.setSelected(AppVariables.commitModificationWhenDataCellLoseFocus);
            isSettingTable = false;
        }
    }

    protected void setSelectable(boolean selectable) {
        tableView.getSelectionModel().clearSelection();
        if (rowsSelectionColumn == null) {
            return;
        }
        if (selectable) {
            if (!tableView.getColumns().contains(rowsSelectionColumn)) {
                tableView.getColumns().add(0, rowsSelectionColumn);
            }
        } else {
            if (tableView.getColumns().contains(rowsSelectionColumn)) {
                tableView.getColumns().remove(rowsSelectionColumn);
            }
        }
    }

    /*
        status
     */
    public void notifyLoaded() {
        if (loadedNotify != null) {
            loadedNotify.set(!loadedNotify.get());
        }
    }

    protected void tableChanged() {
        tableChanged(true);
    }

    public void tableChanged(boolean changed) {
        if (isSettingValues || isSettingTable) {
            return;
        }
        updateStatus();
    }

    public void postLoadedTableData() {
        isSettingValues = true;
        tableView.refresh();
        isSettingValues = false;
        editNull();
        viewNull();
        tableChanged(false);
        notifyLoaded();
    }

    public void clicked(Event event) {
    }

    public void doubleClicked(Event event) {
        editAction();
    }

    public void rightClicked(Event event) {
        popTableMenu(event);
    }

    public void updateStatus() {
        checkSelected();
        checkPagination();
    }

    public void checkPagination() {
        if (pagination != null) {
            pagination.updatePageEnd(tableData == null ? 0 : tableData.size());
            List<Integer> selectedIndices = tableView.getSelectionModel().getSelectedIndices();
            if (selectedIndices != null && !selectedIndices.isEmpty()) {
                pagination.selection = message("Selected") + ": " + selectedIndices.size();
            } else {
                pagination.selection = null;
            }
        }
        if (paginationController != null) {
            paginationController.updateStatus(isShowPagination());
        }
    }

    public boolean isShowPagination() {
        return true;
    }

    /*
        selection
     */
    protected void selectionChanged() {
        checkPagination();
        checkSelected();
        notifySelected();
    }

    public void notifySelected() {
        if (isSettingValues) {
            return;
        }
        selectedNotify.set(!selectedNotify.get());
    }

    protected void checkSelected() {
        if (isSettingValues) {
            return;
        }
        checkButtons();
    }

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

    public boolean isNoneSelected() {
        List<Integer> selectedIndices = tableView.getSelectionModel().getSelectedIndices();
        return selectedIndices == null || selectedIndices.isEmpty();
    }

    public List<P> selectedItems() {
        try {
            List<P> selectedItems = tableView.getSelectionModel().getSelectedItems();
            if (selectedItems != null && !selectedItems.isEmpty()) {
                List<P> items = new ArrayList<>();
                for (P item : selectedItems) {
                    items.add(item);
                }
                return items;
            }
            List<Integer> selectedIndices = tableView.getSelectionModel().getSelectedIndices();
            if (selectedIndices != null && !selectedIndices.isEmpty()) {
                List<P> items = new ArrayList<>();
                for (int index : selectedIndices) {
                    items.add(tableData.get(index));
                }
                return items;
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
        return null;
    }

    public int selectedIndix() {
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

    public P selectedItem() {
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
        buttons
     */
    protected void initButtons() {
    }

    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        boolean isEmpty = tableData == null || tableData.isEmpty();
        boolean none = isNoneSelected();
        if (deleteButton != null) {
            deleteButton.setDisable(none);
        }
        if (deleteRowsButton != null) {
            deleteRowsButton.setDisable(none);
        }
        if (deleteItemsButton != null) {
            deleteItemsButton.setDisable(none);
        }
        if (insertItemButton != null) {
            insertItemButton.setDisable(none);
        }
        if (clearButton != null) {
            clearButton.setDisable(isEmpty);
        }
        if (clearItemsButton != null) {
            clearItemsButton.setDisable(isEmpty);
        }
        if (viewButton != null) {
            viewButton.setDisable(none);
        }
        if (editButton != null) {
            editButton.setDisable(none);
        }
        if (copyButton != null) {
            copyButton.setDisable(none);
        }
        if (copyItemsButton != null) {
            copyItemsButton.setDisable(none);
        }
        if (moveUpButton != null) {
            moveUpButton.setDisable(none);
        }
        if (moveTopButton != null) {
            moveTopButton.setDisable(none);
        }
        if (moveDownButton != null) {
            moveDownButton.setDisable(none);
        }

    }

    /*
        data
     */
    public P newData() {
        return null;
    }

    public int addRows(int index, int number) {
        if (number < 1) {
            return -1;
        }
        List<P> list = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            list.add(newData());
        }
        return addRows(index, list);
    }

    public int addRows(int index, List<P> list) {
        if (list == null || list.isEmpty()) {
            return -1;
        }
        if (index < 0) {
            index = tableData.size();
        }
        isSettingValues = true;
        tableData.addAll(index, list);
        tableView.scrollTo(index - 5);
        isSettingValues = false;
        tableChanged(true);
        return list.size();
    }

    public P dataCopy(P data) {
        return data;
    }

    public void copySelected() {
        List<P> selected = selectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        isSettingValues = true;
        P newData = null;
        for (P data : selected) {
            newData = dataCopy(data);
            tableData.add(newData);
        }
        tableView.scrollTo(newData);
        isSettingValues = false;
        tableChanged(true);
    }

    public String cellString(int row, int col) {
        try {
            return tableView.getColumns().get(col).getCellData(row).toString();
        } catch (Exception e) {
            return null;
        }
    }

    public List<List<String>> dataList() {
        try {
            if (tableData.isEmpty()) {
                return null;
            }
            int rowsSelectionColumnIndex = -1;
            if (rowsSelectionColumn != null) {
                rowsSelectionColumnIndex = tableView.getColumns().indexOf(rowsSelectionColumn);
            }
            int colsNumber = tableView.getColumns().size();
            List<List<String>> data = new ArrayList<>();
            for (int r = 0; r < tableData.size(); r++) {
                List<String> row = new ArrayList<>();
                for (int c = 0; c < colsNumber; c++) {
                    if (c == rowsSelectionColumnIndex) {
                        continue;
                    }
                    String s = null;
                    try {
                        s = tableView.getColumns().get(c).getCellData(r).toString();
                    } catch (Exception e) {
                    }
                    row.add(s);
                }
                data.add(row);
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        actions
     */
    @FXML
    public void autoCommitCheck() {
        if (!isSettingTable && lostFocusCommitCheck != null) {
            AppVariables.lostFocusCommitData(lostFocusCommitCheck.isSelected());
        }
    }

    @FXML
    @Override
    public void addAction() {
        addRowsAction();
    }

    @FXML
    @Override
    public void addRowsAction() {
        TableAddRowsController.open(this);
    }

    @FXML
    public void popAddMenu(Event pEvent) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("AddInFront"));
            menu.setOnAction((ActionEvent event) -> {
                addRows(0, 1);
            });
            items.add(menu);

            menu = new MenuItem(message("AddInEnd"));
            menu.setOnAction((ActionEvent event) -> {
                addRows(-1, 1);
            });
            items.add(menu);

            menu = new MenuItem(message("AddBeforeSelected"));
            menu.setOnAction((ActionEvent event) -> {
                addRows(selectedIndix(), 1);
            });
            items.add(menu);

            menu = new MenuItem(message("AddAfterSelected"));
            menu.setOnAction((ActionEvent event) -> {
                addRows(selectedIndix() + 1, 1);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            popEventMenu(pEvent, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void editAction() {
        edit(selectedIndix());
    }

    public void editNull() {
        editingIndex = -1;
    }

    public void edit(int index) {
        if (index < 0 || tableData == null || index >= tableData.size()) {
            editNull();
            return;
        }
        editingIndex = index;
    }

    @FXML
    public void viewAction() {
        view(selectedIndix());
    }

    public void viewNull() {
        viewingIndex = -1;
    }

    public void view(int index) {
        if (index < 0 || tableData == null || index >= tableData.size()) {
            viewNull();
            return;
        }
        viewingIndex = index;
    }

    @FXML
    @Override
    public void copyAction() {
        copySelected();
    }

    @FXML
    public void insertAction() {
        addRows(selectedIndix(), 1);
    }

    @FXML
    @Override
    public void recoverAction() {
        edit(editingIndex);
    }

    @FXML
    @Override
    public void deleteAction() {
        deleteRowsAction();
    }

    @FXML
    @Override
    public void deleteRowsAction() {
        try {
            List<P> selected = selectedItems();
            if (selected == null || selected.isEmpty()) {
                popError(message("SelectToHandle"));
                return;
            }
            isSettingValues = true;
            tableData.removeAll(selected);
            isSettingValues = false;
            tableChanged(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void clearAction() {
        clear();
    }

    public void clear() {
        isSettingValues = true;
        tableData.clear();
        isSettingValues = false;
        tableChanged(true);
    }

    @FXML
    public void removeLastItem() {
        if (tableData.isEmpty()) {
            return;
        }
        tableData.remove(tableData.size() - 1);
    }

    public void clearWithSure() {
        if (!PopTools.askSure(getTitle(), message("SureClearTable"))) {
            return;
        }
        clear();
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

    @FXML
    public void snapAction() {
        ImageEditorController.openImage(NodeTools.snap(tableView));
    }

    @FXML
    public void dataAction() {
        if (tableData.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        FxTask dataTask = new FxTask<Void>(this) {
            private List<String> names;
            private List<List<String>> data;

            @Override
            protected boolean handle() {
                try {
                    names = new ArrayList<>();
                    int rowsSelectionColumnIndex = -1;
                    if (rowsSelectionColumn != null) {
                        rowsSelectionColumnIndex = tableView.getColumns().indexOf(rowsSelectionColumn);
                    }
                    int colsNumber = tableView.getColumns().size();
                    for (int c = 0; c < colsNumber; c++) {
                        if (c == rowsSelectionColumnIndex) {
                            continue;
                        }
                        names.add(tableView.getColumns().get(c).getText());
                    }
                    data = new ArrayList<>();
                    for (int r = 0; r < tableData.size(); r++) {
                        if (!isWorking()) {
                            return false;
                        }
                        List<String> row = new ArrayList<>();
                        for (int c = 0; c < colsNumber; c++) {
                            if (c == rowsSelectionColumnIndex) {
                                continue;
                            }
                            String s = null;
                            try {
                                s = tableView.getColumns().get(c).getCellData(r).toString();
                            } catch (Exception e) {
                            }
                            row.add(s);
                        }
                        data.add(row);
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                Data2DManufactureController.openData(baseTitle, Data2DColumnTools.toColumns(names), data);
            }
        };
        start(dataTask, false, message("LoadingTableData"));
    }

    @FXML
    public void htmlAction() {
        if (tableData.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        FxTask htmlTask = new FxTask<Void>(this) {
            private StringTable table;

            @Override
            protected boolean handle() {
                table = makeStringTable(this);
                return table != null;
            }

            @Override
            protected void whenSucceeded() {
                table.htmlTable();
            }
        };
        start(htmlTask, false, message("LoadingTableData"));
    }

    protected StringTable makeStringTable(FxTask currentTask) {
        try {
            List<String> names = new ArrayList<>();
            int rowsSelectionColumnIndex = -1;
            if (rowsSelectionColumn != null) {
                rowsSelectionColumnIndex = tableView.getColumns().indexOf(rowsSelectionColumn);
            }
            int colsNumber = tableView.getColumns().size();
            for (int c = 0; c < colsNumber; c++) {
                if (c == rowsSelectionColumnIndex) {
                    continue;
                }
                names.add(tableView.getColumns().get(c).getText());
            }
            StringTable table = new StringTable(names, baseTitle);
            for (int r = 0; r < tableData.size(); r++) {
                List<String> row = new ArrayList<>();
                for (int c = 0; c < colsNumber; c++) {
                    if (c == rowsSelectionColumnIndex) {
                        continue;
                    }
                    String s = null;
                    try {
                        Object cellData = tableView.getColumns().get(c).getCellData(r);
                        Image image = null;
                        int width = 20;
                        if (cellData instanceof ImageView) {
                            image = ((ImageView) cellData).getImage();
                            width = (int) ((ImageView) cellData).getFitWidth();
                        } else if (cellData instanceof Image) {
                            image = (Image) cellData;
                            width = (int) image.getWidth();
                        }
                        if (image != null) {
                            String base64 = FxImageTools.base64(currentTask, image, "png");
                            if (base64 != null) {
                                s = "<img src=\"data:image/png;base64," + base64 + "\" width=" + width + " >";
                            }
                        }
                        if (s == null) {
                            s = cellData.toString();
                        }
                    } catch (Exception e) {
                    }
                    row.add(s);
                }
                table.add(row);
            }
            return table;
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }


    /*
        interface
     */
    @FXML
    protected void popTableMenu(Event event) {
        if (isSettingValues) {
            return;
        }
        List<MenuItem> items = makeTableContextMenu();
        if (items == null || items.isEmpty()) {
            return;
        }
        items.add(new SeparatorMenuItem());

        popNodeMenu(tableView, items);
    }

    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            List<MenuItem> group = new ArrayList<>();

            if (addButton != null && addButton.isVisible() && !addButton.isDisabled()) {
                menu = new MenuItem(message("Add"), StyleTools.getIconImageView("iconNewItem.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    addAction();
                });
                group.add(menu);
            }

            if (viewButton != null && viewButton.isVisible() && !viewButton.isDisabled()) {
                menu = new MenuItem(message("View"), StyleTools.getIconImageView("iconView.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    viewAction();
                });
                group.add(menu);
            }

            if (editButton != null && editButton.isVisible() && !editButton.isDisabled()) {
                menu = new MenuItem(message("Edit"), StyleTools.getIconImageView("iconEdit.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    editAction();
                });
                group.add(menu);
            }

            if (deleteButton != null && deleteButton.isVisible() && !deleteButton.isDisabled()) {
                menu = new MenuItem(message("Delete"), StyleTools.getIconImageView("iconDelete.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    deleteAction();
                });
                group.add(menu);
            }

            if (clearButton != null && clearButton.isVisible() && !clearButton.isDisabled()) {
                menu = new MenuItem(message("Clear"), StyleTools.getIconImageView("iconClear.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    clearAction();
                });
                group.add(menu);
            }

            if (!group.isEmpty()) {
                items.addAll(group);
                items.add(new SeparatorMenuItem());
            }

            if (refreshButton != null && refreshButton.isVisible() && !refreshButton.isDisabled()) {
                menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    refreshAction();
                });
                items.add(menu);
            }

            if (moveUpButton != null && moveUpButton.isVisible() && !moveUpButton.isDisabled()) {
                menu = new MenuItem(message("MoveUp"), StyleTools.getIconImageView("iconUp.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    moveUpAction();
                });
                items.add(menu);
            }

            if (moveTopButton != null && moveTopButton.isVisible() && !moveTopButton.isDisabled()) {
                menu = new MenuItem(message("MoveTop"), StyleTools.getIconImageView("iconDoubleUp.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    moveTopAction();
                });
                items.add(menu);
            }

            if (moveDownButton != null && moveDownButton.isVisible() && !moveDownButton.isDisabled()) {
                menu = new MenuItem(message("MoveDown"), StyleTools.getIconImageView("iconDown.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    moveDownAction();
                });
                items.add(menu);
            }

            menu = new MenuItem(message("Snapshot"), StyleTools.getIconImageView("iconSnapshot.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                snapAction();
            });
            items.add(menu);

            menu = new MenuItem("Html", StyleTools.getIconImageView("iconHtml.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                htmlAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Data"), StyleTools.getIconImageView("iconData.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                dataAction();
            });
            items.add(menu);

            List<MenuItem> more = moreContextMenu();
            if (more != null && !more.isEmpty()) {
                items.addAll(more);
            }

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    protected List<MenuItem> moreContextMenu() {
        return null;
    }

    @Override
    public void cleanPane() {
        try {
            selectedNotify = null;
            loadedNotify = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        get
     */
    public TableView<P> getTableView() {
        return tableView;
    }

    public void setTableView(TableView<P> tableView) {
        this.tableView = tableView;
    }

}
