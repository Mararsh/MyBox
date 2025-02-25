package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.PopTools;
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

    @FXML
    protected Label pageLabel;
    @FXML
    protected FlowPane paginationPane;
    @FXML
    protected ComboBox<String> pageSizeSelector, pageSelector;

    public BaseTablePagesController() {
        tableName = "";
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
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initPagination();

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
        setDataSizeLabel();
    }

    public void setDataSizeLabel() {
        if (dataSizeLabel == null) {
            return;
        }
        int tsize = tableData == null ? 0 : tableData.size();
        long start = startRowOfCurrentPage + 1;
        long end = start + tsize - 1;
        dataSizeLabel.setText(message("Rows") + ": "
                + "[" + start + "-" + end + "]" + tsize
                + (dataSize > 0 ? "/" + dataSize : ""));
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
        task = new FxSingletonTask<Void>(this) {
            private List<P> data;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    countPagination(this, conn, page);
                    if (task == null || !isWorking()) {
                        return false;
                    }
                    data = readPageData(this, conn);
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

    protected void countPagination(FxTask currentTask, Connection conn, long page) {
        dataSize = readDataSize(currentTask, conn);
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
        pagesNumber = 1;
        dataSize = 0;
        startRowOfCurrentPage = 0;
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
//        TableAddRowsController.open(this);
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
        loadPage(currentPage);
    }

    @Override
    protected List<MenuItem> moreContextMenu() {
        if (paginationPane == null || !paginationPane.isVisible()) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;
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
        if (items.isEmpty()) {
            return null;
        }
        items.add(0, new SeparatorMenuItem());
        return items;
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
            MyBoxLog.error(e);
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
            MyBoxLog.debug(e);
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
