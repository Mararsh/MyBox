package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.Pagination;
import mara.mybox.data.Pagination.ObjectType;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @param <P> Data
 * @Author Mara
 * @CreateDate 2021-10-16
 * @License Apache License Version 2.0
 */
public abstract class BaseTablePagesController<P> extends BaseTableViewController<P> {

    protected String tableName, idColumnName, queryConditions, orderColumns, queryConditionsString;
    protected int editingIndex, viewingIndex;
    protected boolean dataSizeLoaded, loadInBackground;

    public BaseTablePagesController() {
        tableName = "";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            editingIndex = viewingIndex = -1;
            dataSizeLoaded = false;
            pagination = new Pagination();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (paginationController != null) {
                paginationController.setParameters(this, pagination, ObjectType.Table);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        table
     */
    @Override
    public void updateStatus() {
        super.updateStatus();
        pagination.updatePageEnd(tableData == null ? 0 : tableData.size());
        if (paginationController != null) {
            paginationController.updateStatus();
        }
    }

    public boolean checkBeforeLoadingTableData() {
        return true;
    }

    public void loadTableData() {
        loadPage(pagination.currentPage);
    }

    @Override
    public void loadPage(long page) {
        MyBoxLog.console(page);
        MyBoxLog.console(pagination.info());
        if (isSettingValues || !checkBeforeLoadingTableData()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private List<P> data;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    pagination.goPage(readDataSize(this, conn), page);
                    MyBoxLog.console(pagination.info());
                    if (task == null || !isWorking()) {
                        return false;
                    }
                    data = readPageData(this, conn);
                    pagination.updatePageEnd(data.size());
                    MyBoxLog.console(pagination.info());
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

    public long readDataSize(FxTask currentTask, Connection conn) {
        return 0;
    }

    public void loadDataSize() {
        dataSizeLoaded = true;
    }

    public List<P> readPageData(FxTask currentTask, Connection conn) {
        return null;
    }

    public void resetView(boolean changed) {
        isSettingValues = true;
        tableData.clear();
        isSettingValues = false;
        paginationController.reset();
        dataSizeLoaded = true;
        tableChanged(changed);
        editNull();
        viewNull();
        setPagination();
    }

    public boolean isDataSizeLoaded() {
        return dataSizeLoaded;
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
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
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
    @Override
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
        task = new FxSingletonTask<Void>(this) {

            private int deletedCount = 0;

            @Override
            protected boolean handle() {
                deletedCount = deleteSelectedData(this);
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

    protected int deleteSelectedData(FxTask currentTask) {
        List<P> selected = new ArrayList<>();
        selected.addAll(selectedItems());
        if (selected.isEmpty()) {
            return 0;
        }
        return deleteData(currentTask, selected);
    }

    protected int deleteData(FxTask currentTask, List<P> data) {
        return 0;
    }

    protected void afterDeletion() {
        refreshAction();
    }

    @FXML
    @Override
    public void clearAction() {
        if (!PopTools.askSure(getTitle(), message("SureClearData"))) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {
            long deletedCount = 0;

            @Override
            protected boolean handle() {
                deletedCount = clearData(this);
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

    protected long clearData(FxTask currentTask) {
        return tableData.size();
    }

    protected void afterClear() {
        resetView(false);
    }

    @FXML
    @Override
    public void deleteRowsAction() {
        List<P> selected = selectedItems();
        if (selected == null || selected.isEmpty()) {
            popError(message("SelectToHandle"));
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
        loadPage(pagination.currentPage);
    }

    @Override
    protected List<MenuItem> moreContextMenu() {
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;
        if (pagination.currentPage < pagination.pagesNumber) {
            menu = new MenuItem(message("NextPage") + "  ALT+PAGE_DOWN",
                    StyleTools.getIconImageView("iconNext.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                pageNextAction();
            });
            items.add(menu);
        }

        if (pagination.currentPage > 1) {
            menu = new MenuItem(message("PreviousPage") + "  ALT+PAGE_UP",
                    StyleTools.getIconImageView("iconPrevious.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                pagePreviousAction();
            });
            items.add(menu);
        }

        if (items.isEmpty()) {
            return null;
        }
        items.add(0, new SeparatorMenuItem());
        return items;
    }


    /*
        pagination
     */
    protected void setPagination() {
        showPaginationPane(dataSizeLoaded);
    }

    protected void showPaginationPane(boolean show) {
        if (paginationController == null) {
            return;
        }
        paginationController.setVisible(show);
        if (show) {
            paginationController.updateStatus();
        }
    }

}
