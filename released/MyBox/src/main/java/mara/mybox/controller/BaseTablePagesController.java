package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
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
    public boolean checkBeforeLoadingTableData() {
        return true;
    }

    public void loadTableData() {
        loadPage(pagination.currentPage);
    }

    @Override
    public void loadPage(long page) {
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
                    if (task == null || !isWorking()) {
                        return false;
                    }
                    data = readPageData(this, conn);
                    pagination.updatePageEnd(data != null ? data.size() : 0);
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

    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        if (!dataSizeLoaded) {
            loadDataSize();
        }
    }

    public long readDataSize(FxTask currentTask, Connection conn) {
        return 0;
    }

    public void loadDataSize() {
    }

    public List<P> readPageData(FxTask currentTask, Connection conn) {
        return null;
    }

    public void resetView(boolean changed) {
        isSettingValues = true;
        tableData.clear();
        isSettingValues = false;
        if (paginationController != null) {
            paginationController.reset();
        }
        dataSizeLoaded = true;
        tableChanged(changed);
        editNull();
        viewNull();
    }

    @Override
    public boolean isShowPagination() {
        return dataSizeLoaded;
    }

    public boolean isDataSizeLoaded() {
        return dataSizeLoaded;
    }

    /*
        data
     */
    @FXML
    @Override
    public void deleteAction() {
        List<Integer> indice = tableView.getSelectionModel().getSelectedIndices();
        if (indice == null || indice.isEmpty()) {
            popError(message("SelectToHandle"));
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

}
