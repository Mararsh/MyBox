package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import mara.mybox.data.StringTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.cell.TableRowSelectionCell;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.NumberTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @param <P> Data
 * @Author Mara
 * @CreateDate 2021-10-16
 * @License Apache License Version 2.0
 */
public abstract class BaseTablePagesController<P> extends BaseTableViewController<P> {

    protected String tableName, idColumnName, queryConditions, orderColumns, queryConditionsString;
    protected int pageSize, editingIndex, viewingIndex;
    protected long pagesNumber, dataSize;
    protected long currentPage, startRowOfCurrentPage;  // 0-based
    protected boolean dataSizeLoaded, loadInBackground;
    protected SimpleBooleanProperty loadedNotify, selectedNotify;

    @FXML
    protected TableColumn<P, Boolean> rowsSelectionColumn;
    @FXML
    protected Label dataSizeLabel, selectedLabel, pageLabel;
    @FXML
    protected CheckBox allRowsCheck;
    @FXML
    protected Button moveUpButton, moveDownButton, moveTopButton, refreshButton,
            deleteItemsButton, editItemButton, copyItemButton;
    @FXML
    protected FlowPane paginationPane;
    @FXML
    protected ComboBox<String> pageSizeSelector, pageSelector;

    public BaseTablePagesController() {
        tableName = "";
        TipsLabelKey = "TableTips";
        selectedNotify = new SimpleBooleanProperty(false);
        loadedNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

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

            initButtons();
            initPagination();
            initMore();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initMore() {

    }

    /*
        table
     */
    @Override
    protected void initTable() {
        try {
            super.initTable();
            if (tableView == null) {
                return;
            }
            tableData.addListener((ListChangeListener.Change<? extends P> change) -> {
                tableChanged();
            });

            tableView.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>() {
                @Override
                public void onChanged(ListChangeListener.Change c) {
                    checkSelected();
                    notifySelected();
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

            initColumns();

            checkSelected();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void notifySelected() {
        if (isSettingValues) {
            return;
        }
        selectedNotify.set(!selectedNotify.get());
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
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {
            private List<P> data;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    countPagination(conn, page);
                    data = readPageData(conn);
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
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
        if (loadInBackground) {
            start(task, tableView);
        } else {
            start(task, true, message("LoadingTableData"));
        }
    }

    protected void countPagination(Connection conn, long page) {
        dataSize = readDataSize(conn);
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
        notifyLoaded();
        if (!dataSizeLoaded) {
            loadDataSize();
        }
        setPagination();
    }

    public void notifyLoaded() {
        if (loadedNotify != null) {
            loadedNotify.set(!loadedNotify.get());
        }
    }

    public long readDataSize(Connection conn) {
        return 0;
    }

    public void loadDataSize() {
        dataSizeLoaded = true;
    }

    public List<P> readPageData(Connection conn) {
        return null;
    }

    protected void tableChanged() {
        if (isSettingValues) {
            return;
        }
        tableChanged(true);
    }

    public void tableChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        updateStatus();
    }

    public void updateStatus() {
        checkSelected();
        if (dataSizeLabel != null) {
            int tsize = tableData == null ? 0 : tableData.size();
            long start = startRowOfCurrentPage + 1;
            long end = start + tsize - 1;
            dataSizeLabel.setText(message("Rows") + ": "
                    + "[" + start + "-" + end + "]" + tsize
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

        popEventMenu(event, items);
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

            if (paginationPane == null || paginationPane.isVisible()) {
                if (pageNextButton != null && pageNextButton.isVisible() && !pageNextButton.isDisabled()) {
                    menu = new MenuItem(message("NextPage"), StyleTools.getIconImageView("iconNext.png"));
                    menu.setOnAction((ActionEvent menuItemEvent) -> {
                        pageNextAction();
                    });
                    items.add(menu);
                }

                if (pagePreviousButton != null && pagePreviousButton.isVisible() && !pagePreviousButton.isDisabled()) {
                    menu = new MenuItem(message("PreviousPage"), StyleTools.getIconImageView("iconPrevious.png"));
                    menu.setOnAction((ActionEvent menuItemEvent) -> {
                        pagePreviousAction();
                    });
                    items.add(menu);
                }
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

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public void resetView(boolean changed) {
        isSettingValues = true;
        tableData.clear();
        isSettingValues = false;
        pagesNumber = 1;
        dataSize = 0;
        startRowOfCurrentPage = 0;
        dataSizeLoaded = false;
        tableChanged(changed);
        checkSelected();
        editNull();
        viewNull();
    }

    public boolean isDataSizeLoaded() {
        return dataSizeLoaded;
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
            MyBoxLog.error(e.toString());
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
        if (viewButton != null) {
            viewButton.setDisable(none);
        }
        if (editButton != null) {
            editButton.setDisable(none);
        }
        if (editItemButton != null) {
            editItemButton.setDisable(none);
        }
        if (copyButton != null) {
            copyButton.setDisable(none);
        }
        if (copyItemButton != null) {
            copyItemButton.setDisable(none);
        }
        if (clearButton != null) {
            clearButton.setDisable(isEmpty);
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

            popEventMenu(mouseEvent, items);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
        List<Integer> indice = tableView.getSelectionModel().getSelectedIndices();
        if (indice == null || indice.isEmpty()) {
            clearAction();
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {

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
                    if (indice.contains(editingIndex)) {
                        editNull();
                    }
                    if (indice.contains(viewingIndex)) {
                        viewNull();
                    }
                    afterDeletion();
                }
            }
        };
        start(task);
    }

    protected int deleteSelectedData() {
        List<P> selected = new ArrayList<>();
        selected.addAll(selectedItems());
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
        if (!checkBeforeNextAction()) {
            return;
        }
        if (!PopTools.askSure(getTitle(), message("SureClearData"))) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {
            long deletedCount = 0;

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

    protected long clearData() {
        int size = tableData.size();
        isSettingValues = true;
        tableData.clear();
        isSettingValues = false;
        return size;
    }

    protected void afterClear() {
        resetView(false);
    }

    @FXML
    @Override
    public void deleteRowsAction() {
        List<P> selected = selectedItems();
        if (selected == null || selected.isEmpty()) {
            deleteAllRows();
            return;
        }
        isSettingValues = true;
        tableData.removeAll(selected);
        isSettingValues = false;
        tableChanged(true);
    }

    public void deleteAllRows() {
        isSettingValues = true;
        if (!PopTools.askSure(getTitle(), message("SureClearTable"))) {
            return;
        }
        tableData.clear();
        isSettingValues = false;
        tableChanged(true);
    }

    @FXML
    @Override
    public void refreshAction() {
        loadPage(currentPage);
    }

    @FXML
    public void dataAction() {
        if (tableData.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        SingletonTask dataTask = new SingletonTask<Void>(this) {
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
                DataFileCSVController.open(null, Data2DColumn.toColumns(names), data);
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
        SingletonTask htmlTask = new SingletonTask<Void>(this) {
            private StringTable table;

            @Override
            protected boolean handle() {
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
                    table = new StringTable(names, baseTitle);
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
                                    String base64 = FxImageTools.base64(image, "png");
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
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                table.htmlTable();
            }
        };
        start(htmlTask, false, message("LoadingTableData"));
    }

    @FXML
    public void snapAction() {
        ImageViewerController.openImage(NodeTools.snap(tableView));
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
            pageSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> o, String ov, String nv) {
                    checkPageSelector();
                }
            });

            pageSizeSelector.getItems().addAll(Arrays.asList("50", "30", "100", "20", "60", "200", "300",
                    "500", "1000", "2000", "5000", "10000", "20000", "50000"));
            pageSizeSelector.setValue(pageSize + "");
            pageSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> o, String ov, String nv) {
                    try {
                        int v = Integer.parseInt(nv.trim());
                        if (v <= 0) {
                            pageSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        } else {
                            pageSizeChanged(v);
                        }
                    } catch (Exception e) {
                        pageSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
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
            IndexRange range = NumberTools.scrollRange(UserConfig.selectorScrollSize(),
                    (int) pagesNumber, (int) currentPage);
            List<String> pages = new ArrayList<>();
            for (long i = range.getStart(); i < range.getEnd(); i++) {
                pages.add((i + 1) + "");
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

    @Override
    public void cleanPane() {
        try {
            selectedNotify = null;
            loadedNotify = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
