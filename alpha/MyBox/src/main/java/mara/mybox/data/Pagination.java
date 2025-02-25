package mara.mybox.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2025-2-25
 * @License Apache License Version 2.0
 */
public class Pagination {

    public long totalSize, currentPage, pagesNumber,
            startRowOfCurrentPage, endRowOfCurrentPage; // 0-based
    public int pageSize, selectedRows;

    public Pagination() {
        pageSize = 50;
        reset();
    }

    public Pagination initSize(int pagesize) {
        try {
            pageSize = pagesize;
            if (pageSize < 1) {
                pageSize = 50;
            }
            reset();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return this;
    }

    public final void reset() {
        totalSize = 0;
        startRowOfCurrentPage = 0;
        endRowOfCurrentPage = 0;
        currentPage = 0;
        pagesNumber = 1;
        selectedRows = 0;
    }

    public void goPage(long dataSize, long page) {
        totalSize = dataSize < 0 ? 0 : dataSize;
        if (totalSize < 0 || totalSize <= pageSize) {
            pagesNumber = 1;
        } else {
            pagesNumber = totalSize / pageSize;
            if (totalSize % pageSize > 0) {
                pagesNumber++;
            }
        }
        if (page >= pagesNumber) {
            currentPage = pagesNumber - 1;
        } else {
            currentPage = page;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }
        startRowOfCurrentPage = pageSize * currentPage;
    }

    public void updatePageEnd(long tableSize) {
        endRowOfCurrentPage = startRowOfCurrentPage + tableSize - 1;
    }

    public String info() {
        String s = "totalSize:" + totalSize + "\n"
                + "pageSize:" + pageSize + "\n"
                + "startRowOfCurrentPage:" + startRowOfCurrentPage + "\n"
                + "endRowOfCurrentPage:" + endRowOfCurrentPage + "\n"
                + "currentPage:" + currentPage + "\n"
                + "pagesNumber:" + pagesNumber + "\n"
                + "selectedRows:" + selectedRows;
        return s;
    }

    /*
        get/set
     */
    public long getTotalSize() {
        return totalSize;
    }

    public Pagination setTotalSize(long totalSize) {
        this.totalSize = totalSize;
        return this;
    }

    public long getCurrentPage() {
        return currentPage;
    }

    public Pagination setCurrentPage(long currentPage) {
        this.currentPage = currentPage;
        return this;
    }

    public long getPagesNumber() {
        return pagesNumber;
    }

    public Pagination setPagesNumber(long pagesNumber) {
        this.pagesNumber = pagesNumber;
        return this;
    }

    public long getStartRowOfCurrentPage() {
        return startRowOfCurrentPage;
    }

    public Pagination setStartRowOfCurrentPage(long startRowOfCurrentPage) {
        this.startRowOfCurrentPage = startRowOfCurrentPage;
        return this;
    }

    public long getEndRowOfCurrentPage() {
        return endRowOfCurrentPage;
    }

    public Pagination setEndRowOfCurrentPage(long endRowOfCurrentPage) {
        this.endRowOfCurrentPage = endRowOfCurrentPage;
        return this;
    }

    public int getPageSize() {
        return pageSize;
    }

    public Pagination setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public int getSelectedRows() {
        return selectedRows;
    }

    public Pagination setSelectedRows(int selectedRows) {
        this.selectedRows = selectedRows;
        return this;
    }

}
