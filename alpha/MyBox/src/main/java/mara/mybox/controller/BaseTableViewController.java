package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.StyleTools;
import mara.mybox.fxml.cell.TableRowSelectionCell;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @param <P> Data
 * @Author Mara
 * @CreateDate 2021-10-16
 * @License Apache License Version 2.0
 */
public abstract class BaseTableViewController<P> extends BaseController {
    
    protected ObservableList<P> tableData;
    protected String tableName, idColumn, queryConditions, orderColumns, queryConditionsString;
    protected int pageSize, editingIndex, viewingIndex;
    protected long pagesNumber, dataSize;
    protected long currentPage, startRowOfCurrentPage;  // 0-based
    protected boolean dataSizeLoaded, loadInBackground;
    protected final SimpleBooleanProperty selectNotify;
    
    @FXML
    protected TableView<P> tableView;
    @FXML
    protected TableColumn<P, Boolean> rowsSelectionColumn;
    @FXML
    protected Label dataSizeLabel, selectedLabel, pageLabel;
    @FXML
    protected CheckBox deleteConfirmCheck, allRowsCheck;
    @FXML
    protected Button moveUpButton, moveDownButton, refreshButton, deleteItemsButton;
    @FXML
    protected FlowPane paginationPane;
    @FXML
    protected ComboBox<String> pageSizeSelector, pageSelector;
    
    public BaseTableViewController() {
        tableName = "";
        TipsLabelKey = "TableTips";
        selectNotify = new SimpleBooleanProperty(false);
    }
    
    @Override
    public void initValues() {
        try {
            super.initValues();
            
            tableData = FXCollections.observableArrayList();
            pagesNumber = 1;
            currentPage = startRowOfCurrentPage = 0;
            dataSize = 0;
            editingIndex = viewingIndex = -1;
            dataSizeLoaded = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    @Override
    public void initControls() {
        try {
            super.initControls();
            
            initTable();
            initButtons();
            initPagination();
            initMore();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    public void initMore() {
        
    }
    
    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            
            if (deleteButton != null) {
                NodeStyleTools.setTooltip(deleteButton, new Tooltip(message("DeleteRows")));
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        table
     */
    protected void initTable() {
        try {
            if (tableView == null) {
                return;
            }
            tableData.addListener((ListChangeListener.Change<? extends P> change) -> {
                tableChanged();
            });
            
            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener() {
                @Override
                public void onChanged(ListChangeListener.Change c) {
                    selectNotify.set(selectNotify.get());
                    checkSelected();
                }
            });
            
            tableView.setOnMouseClicked((MouseEvent event) -> {
                if (popMenu != null && popMenu.isShowing()) {
                    popMenu.hide();
                }
                if (event.getButton() == MouseButton.SECONDARY) {
                    popTableMenu(event);
                } else if (event.getClickCount() == 1) {
                    itemClicked();
                } else if (event.getClickCount() > 1) {
                    itemDoubleClicked();
                }
            });
            
            tableView.setItems(tableData);
            
            if (deleteConfirmCheck != null) {
                deleteConfirmCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            UserConfig.setBoolean(baseName + "ConfirmDelete", deleteConfirmCheck.isSelected());
                        });
                deleteConfirmCheck.setSelected(UserConfig.getBoolean(baseName + "ConfirmDelete", true));
            }
            
            initColumns();
            
            checkSelected();
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    public boolean checkBeforeLoadingTableData() {
        return true;
    }
    
    public void loadTableData() {
        loadPage(currentPage);
    }
    
    public void loadPage(long page) {
        if (!checkBeforeLoadingTableData()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {
            private List<P> data;
            
            @Override
            protected boolean handle() {
                countPagination(page);
                data = readPageData();
                return true;
            }
            
            @Override
            protected void whenSucceeded() {
            }
            
            @Override
            protected void finalAction() {
                super.finalAction();
                isSettingValues = true;
                if (data != null && !data.isEmpty()) {
                    tableData.setAll(data);
                } else {
                    tableData.clear();
                }
                isSettingValues = false;
                postLoadedTableData();
            }
            
        };
        start(task, !loadInBackground, message("LoadingTableData"));
    }
    
    protected void countPagination(long page) {
        dataSize = readDataSize();
        if (dataSize < 0 || dataSize <= pageSize) {
            pagesNumber = 1;
        } else {
            pagesNumber = dataSize % pageSize == 0 ? dataSize / pageSize : dataSize / pageSize + 1;
        }
        currentPage = page;
        if (currentPage >= pagesNumber) {
            currentPage = pagesNumber - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }
        startRowOfCurrentPage = pageSize * currentPage;
    }
    
    public void postLoadedTableData() {
        isSettingValues = true;
        tableView.refresh();
        isSettingValues = false;
        checkSelected();
        editNull();
        viewNull();
        tableChanged(false);
        if (!dataSizeLoaded) {
            loadDataSize();
        }
        setPagination();
    }
    
    public long readDataSize() {
        return 0;
    }
    
    public void loadDataSize() {
        dataSizeLoaded = true;
//        setPagination();
    }
    
    public List<P> readPageData() {
        return null;
    }
    
    protected void tableChanged() {
        if (isSettingValues) {
            return;
        }
        tableChanged(true);
    }
    
    public void tableChanged(boolean changed) {
        updateStatus();
    }
    
    public void updateStatus() {
        checkSelected();
        if (dataSizeLabel != null) {
            dataSizeLabel.setText(message("Rows") + ": "
                    + (tableData == null ? 0 : tableData.size())
                    + (dataSize > 0 ? "/" + dataSize : ""));
        }
    }
    
    protected void checkSelected() {
        if (isSettingValues) {
            return;
        }
        checkButtons();
    }
    
    public void itemClicked() {
    }
    
    public void itemDoubleClicked() {
        editAction();
    }
    
    protected void popTableMenu(MouseEvent event) {
        if (isSettingValues) {
            return;
        }
        List<MenuItem> items = makeTableContextMenu();
        if (items == null || items.isEmpty()) {
            return;
        }
        items.add(new SeparatorMenuItem());
        MenuItem menu = new MenuItem(message("PopupClose"), StyleTools.getIconImage("iconCancel.png"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = null;
        });
        items.add(menu);
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        popMenu.show(tableView, event.getScreenX(), event.getScreenY());
        
    }
    
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;
            
            List<MenuItem> group = new ArrayList<>();
            
            if (addButton != null && addButton.isVisible() && !addButton.isDisabled()) {
                menu = new MenuItem(message("Add"), StyleTools.getIconImage("iconAdd.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    addAction();
                });
                group.add(menu);
            }
            
            if (viewButton != null && viewButton.isVisible() && !viewButton.isDisabled()) {
                menu = new MenuItem(message("View"), StyleTools.getIconImage("iconView.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    viewAction();
                });
                group.add(menu);
            }
            
            if (editButton != null && editButton.isVisible() && !editButton.isDisabled()) {
                menu = new MenuItem(message("Edit"), StyleTools.getIconImage("iconEdit.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    editAction();
                });
                group.add(menu);
            }
            
            if (deleteButton != null && deleteButton.isVisible() && !deleteButton.isDisabled()) {
                menu = new MenuItem(message("Delete"), StyleTools.getIconImage("iconDelete.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    deleteAction();
                });
                group.add(menu);
            }
            
            if (clearButton != null && clearButton.isVisible() && !clearButton.isDisabled()) {
                menu = new MenuItem(message("Clear"), StyleTools.getIconImage("iconClear.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    clearAction();
                });
                group.add(menu);
            }
            
            if (!group.isEmpty()) {
                items.addAll(group);
                items.add(new SeparatorMenuItem());
            }
            
            if (paginationPane == null || paginationPane.isVisible()) {
                if (pageNextButton != null && pageNextButton.isVisible() && !pageNextButton.isDisabled()) {
                    menu = new MenuItem(message("NextPage"), StyleTools.getIconImage("iconNext.png"));
                    menu.setOnAction((ActionEvent menuItemEvent) -> {
                        pageNextAction();
                    });
                    items.add(menu);
                }
                
                if (pagePreviousButton != null && pagePreviousButton.isVisible() && !pagePreviousButton.isDisabled()) {
                    menu = new MenuItem(message("PreviousPage"), StyleTools.getIconImage("iconPrevious.png"));
                    menu.setOnAction((ActionEvent menuItemEvent) -> {
                        pagePreviousAction();
                    });
                    items.add(menu);
                }
            }
            
            if (refreshButton != null && refreshButton.isVisible() && !refreshButton.isDisabled()) {
                menu = new MenuItem(message("Refresh"), StyleTools.getIconImage("iconRefresh.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    refreshAction();
                });
                items.add(menu);
            }
            
            if (moveUpButton != null && moveUpButton.isVisible() && !moveUpButton.isDisabled()) {
                menu = new MenuItem(message("MoveUp"), StyleTools.getIconImage("iconUp.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    moveUpAction();
                });
                items.add(menu);
            }
            
            if (moveDownButton != null && moveDownButton.isVisible() && !moveDownButton.isDisabled()) {
                menu = new MenuItem(message("MoveDown"), StyleTools.getIconImage("iconDown.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    moveDownAction();
                });
                items.add(menu);
            }
            
            menu = new MenuItem(message("Snapshot"), StyleTools.getIconImage("iconSnapshot.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                ImageViewerController.load(NodeTools.snap(tableView));
            });
            items.add(menu);
            
            return items;
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }
    
    public void resetView() {
        isSettingValues = true;
        tableData.clear();
        isSettingValues = false;
        pagesNumber = 1;
        dataSize = 0;
        startRowOfCurrentPage = 0;
        dataSizeLoaded = false;
        tableChanged(false);
        checkSelected();
        editNull();
        viewNull();
    }

    /*
        columns
     */
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
                            for (int i = 0; i < tableData.size(); i++) {  // Fail to listen to selectAll() 
                                tableView.getSelectionModel().select(i);
                            }
                        } else {
                            tableView.getSelectionModel().clearSelection();
                        }
                    }
                });
            }
            
            if (rowsSelectionColumn != null) {
                tableView.setEditable(true);
                rowsSelectionColumn.setCellFactory(TableRowSelectionCell.create(tableView));
            }
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        data
     */
    public P newData() {
        return null;
    }
    
    public void addRows() {
        addRows(-1, 1);
    }
    
    public void addRows(int index, int number) {
        if (number < 1) {
            return;
        }
        if (index < 0) {
            index = tableData.size();
        }
        isSettingValues = true;
        List<P> list = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            list.add(newData());
        }
        tableData.addAll(index, list);
        tableView.scrollTo(index - 5);
        isSettingValues = false;
        tableChanged(true);
    }
    
    public P dataCopy(P data) {
        return data;
    }
    
    public void copySelected() {
        List<P> selected = tableView.getSelectionModel().getSelectedItems();
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
            MyBoxLog.error(e.toString());
            return null;
        }
    }


    /*
        buttons
     */
    protected void initButtons() {
        try {
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        boolean isEmpty = tableData == null || tableData.isEmpty();
        boolean none = isEmpty || tableView.getSelectionModel().getSelectedItem() == null;
        if (deleteButton != null) {
            deleteButton.setDisable(isEmpty);
        }
        if (deleteItemsButton != null) {
            deleteItemsButton.setDisable(isEmpty);
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
        if (clearButton != null) {
            clearButton.setDisable(isEmpty);
        }
        if (moveUpButton != null) {
            moveUpButton.setDisable(none);
        }
        if (moveDownButton != null) {
            moveDownButton.setDisable(none);
        }
        if (selectedLabel != null) {
            selectedLabel.setText(message("Selected") + ": "
                    + (none ? 0 : tableView.getSelectionModel().getSelectedIndices().size()));
        }
    }
    
    @FXML
    @Override
    public void addAction() {
        editNull();
    }
    
    @FXML
    @Override
    public void addRowsAction() {
        TableAddRowsController.open(this);
    }
    
    @FXML
    public void popAddMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);
            
            MenuItem menu;
            
            menu = new MenuItem(message("AddInFront"));
            menu.setOnAction((ActionEvent event) -> {
                addRows(0, 1);
            });
            popMenu.getItems().add(menu);
            
            menu = new MenuItem(message("AddInEnd"));
            menu.setOnAction((ActionEvent event) -> {
                addRows(-1, 1);
            });
            popMenu.getItems().add(menu);
            
            menu = new MenuItem(message("AddBeforeSelected"));
            menu.setOnAction((ActionEvent event) -> {
                addRows(tableView.getSelectionModel().getSelectedIndex(), 1);
            });
            popMenu.getItems().add(menu);
            
            menu = new MenuItem(message("AddAfterSelected"));
            menu.setOnAction((ActionEvent event) -> {
                addRows(tableView.getSelectionModel().getSelectedIndex() + 1, 1);
            });
            popMenu.getItems().add(menu);
            
            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);
            
            LocateTools.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    @FXML
    public void editAction() {
        edit(tableView.getSelectionModel().getSelectedIndex());
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
        view(tableView.getSelectionModel().getSelectedIndex());
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
        addRows(tableView.getSelectionModel().getSelectedIndex(), 1);
    }
    
    @FXML
    @Override
    public void recoverAction() {
        edit(editingIndex);
    }
    
    @FXML
    @Override
    public void deleteAction() {
        List<Integer> indice = tableView.getSelectionModel().getSelectedIndices();
        if (indice == null || indice.isEmpty()) {
            clearAction();
            return;
        }
        if (deleteConfirmCheck != null && deleteConfirmCheck.isSelected()) {
            if (!PopTools.askSure(this, getBaseTitle(), message("SureDelete"))) {
                return;
            }
        }
        if (indice.contains(editingIndex)) {
            editNull();
        }
        if (indice.contains(viewingIndex)) {
            viewNull();
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                
                private int deletedCount = 0;
                
                @Override
                protected boolean handle() {
                    deletedCount = deleteSelectedData();
                    return deletedCount >= 0;
                }
                
                @Override
                protected void whenSucceeded() {
                    popInformation(message("Deleted") + ":" + deletedCount);
                    if (deletedCount > 0) {
                        afterDeletion();
                    }
                }
            };
            start(task);
        }
    }
    
    protected int deleteSelectedData() {
        List<P> selected = new ArrayList<>();
        selected.addAll(tableView.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) {
            return 0;
        }
        return deleteData(selected);
    }
    
    protected int deleteData(List<P> data) {
        return 0;
    }
    
    protected void afterDeletion() {
        refreshAction();
    }
    
    @FXML
    @Override
    public void clearAction() {
        if (tableData == null || tableData.isEmpty()) {
            return;
        }
        if (!PopTools.askSure(this, getBaseTitle(), message("SureClear"))) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                int deletedCount = 0;
                
                @Override
                protected boolean handle() {
                    deletedCount = clearData();
                    return deletedCount >= 0;
                }
                
                @Override
                protected void whenSucceeded() {
                    popInformation(message("Deleted") + ":" + deletedCount);
                    if (deletedCount > 0) {
                        afterClear();
                    }
                }
            };
            start(task);
        }
    }
    
    protected int clearData() {
        int size = tableData.size();
        isSettingValues = true;
        tableData.clear();
        isSettingValues = false;
        return size;
    }
    
    protected void afterClear() {
        resetView();
        afterDeletion();
    }
    
    @FXML
    public void deleteRowsAction() {
        List<P> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            clearAction();
            return;
        }
        isSettingValues = true;
        tableData.removeAll(selected);
        isSettingValues = false;
        tableChanged(true);
    }
    
    @FXML
    public void refreshAction() {
        loadPage(currentPage);
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
            P info = tableData.get(index);
            tableData.set(index, tableView.getItems().get(index - 1));
            tableData.set(index - 1, info);
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
            P info = tableData.get(index);
            tableData.set(index, tableData.get(index + 1));
            tableData.set(index + 1, info);
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
    public void dataAction() {
        try {
            if (tableData.isEmpty()) {
                return;
            }
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
            DataFileCSVController.open(Data2DColumn.toColumns(names), data);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    @FXML
    public void htmlAction() {
        try {
            if (tableData.isEmpty()) {
                return;
            }
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
                        s = tableView.getColumns().get(c).getCellData(r).toString();
                    } catch (Exception e) {
                    }
                    row.add(s);
                }
                table.add(row);
            }
            table.editHtml();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        pagination
     */
    protected void initPagination() {
        try {
            pageSize = UserConfig.getInt(baseName + "PageSize", 50);
            if (pageSize < 1) {
                pageSize = 50;
            }
            if (pageSelector == null) {
                return;
            }
            pageSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        checkPageSelector();
                    });
            
            pageSizeSelector.getItems().addAll(Arrays.asList("50", "30", "100", "20", "60", "200", "300",
                    "500", "1000", "2000", "5000", "10000", "20000", "50000"));
            
            pageSizeSelector.setValue(pageSize + "");
            pageSizeSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        if (newValue == null) {
                            return;
                        }
                        try {
                            int v = Integer.parseInt(newValue.trim());
                            if (v <= 0) {
                                pageSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                            } else {
                                pageSizeChanged(v);
                            }
                        } catch (Exception e) {
                            pageSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    protected void pageSizeChanged(int v) {
        if (v <= 0) {
            return;
        }
        pageSize = v;
        UserConfig.setInt(baseName + "PageSize", pageSize);
        pageSizeSelector.getEditor().setStyle(null);
        if (!isSettingValues) {
            loadTableData();
        }
    }
    
    protected boolean checkPageSelector() {
        if (isSettingValues || pageSelector == null) {
            return false;
        }
        try {
            String value = pageSelector.getEditor().getText();
            int v = Integer.parseInt(value);
            if (v <= 0) {
                pageSelector.getEditor().setStyle(UserConfig.badStyle());
                return false;
            } else {
                pageSelector.getEditor().setStyle(null);
                loadPage(v - 1);
                return true;
            }
        } catch (Exception e) {
            pageSelector.getEditor().setStyle(UserConfig.badStyle());
            return false;
        }
    }
    
    protected void setPagination() {
        try {
            if (paginationPane != null) {
                if (!dataSizeLoaded) {
                    paginationPane.setVisible(false);
                    return;
                }
                paginationPane.setVisible(true);
            }
            if (pageSelector == null) {
                return;
            }
            isSettingValues = true;
            pageSelector.setDisable(false);
            List<String> pages = new ArrayList<>();
            for (long i = Math.max(1, currentPage - 20);
                    i <= Math.min(pagesNumber, currentPage + 20); i++) {
                pages.add(i + "");
            }
            pageSelector.getItems().clear();
            pageSelector.getItems().addAll(pages);
            pageSelector.getSelectionModel().select((currentPage + 1) + "");
            pageLabel.setText("/" + pagesNumber);
            if (currentPage > 0) {
                pagePreviousButton.setDisable(false);
                pageFirstButton.setDisable(false);
            } else {
                pagePreviousButton.setDisable(true);
                pageFirstButton.setDisable(true);
            }
            if (currentPage >= pagesNumber - 1) {
                pageNextButton.setDisable(true);
                pageLastButton.setDisable(true);
            } else {
                pageNextButton.setDisable(false);
                pageLastButton.setDisable(false);
            }
            pageSelector.getEditor().setStyle(null);
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        
    }
    
    @FXML
    public void goPage() {
        checkPageSelector();
    }
    
    @FXML
    @Override
    public void pageNextAction() {
        loadPage(currentPage + 1);
    }
    
    @FXML
    @Override
    public void pagePreviousAction() {
        loadPage(currentPage - 1);
    }
    
    @FXML
    @Override
    public void pageFirstAction() {
        loadPage(0);
    }
    
    @FXML
    @Override
    public void pageLastAction() {
        loadPage(Integer.MAX_VALUE);
    }
    
}
