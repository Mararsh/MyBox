package mara.mybox.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2025-2-25
 * @License Apache License Version 2.0
 */
public class Pagination {

    // 0-based, exclude end
    public long currentPage, pagesNumber,
            rowsNumber, startRowOfCurrentPage, endRowOfCurrentPage,
            objectsNumber, startObjectOfCurrentPage, endObjectOfCurrentPage;
    public int pageSize, defaultPageSize;
    public String selection;
    public ObjectType objectType;

    public enum ObjectType {
        Table, Text, Bytes
    }

    public Pagination() {
        init(ObjectType.Table);
    }

    public Pagination(ObjectType type) {
        init(type);
    }

    public final void init(ObjectType type) {
        objectType = type != null ? type : ObjectType.Table;
        switch (objectType) {
            case Table:
                defaultPageSize = 50;
                break;
            case Bytes:
                defaultPageSize = 100000;
                break;
            case Text:
                defaultPageSize = 200;
                break;
        }
        pageSize = defaultPageSize;
        reset();
    }

    public Pagination init(ObjectType type, int size) {
        try {
            init(type);
            pageSize = size > 0 ? size : defaultPageSize;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return this;
    }

    public final Pagination reset() {
        rowsNumber = 0;
        currentPage = 0;
        pagesNumber = 1;
        selection = null;
        startRowOfCurrentPage = 0;
        endRowOfCurrentPage = 0;
        startObjectOfCurrentPage = 0;
        endObjectOfCurrentPage = 0;
        return this;
    }

    public Pagination copyFrom(Pagination p) {
        if (p == null) {
            return this;
        }
        objectType = p.objectType;
        pageSize = p.pageSize;
        defaultPageSize = p.defaultPageSize;
        rowsNumber = p.rowsNumber;
        currentPage = p.currentPage;
        pagesNumber = p.pagesNumber;
        selection = p.selection;
        startRowOfCurrentPage = p.startRowOfCurrentPage;
        endRowOfCurrentPage = p.endRowOfCurrentPage;
        startObjectOfCurrentPage = p.startObjectOfCurrentPage;
        endObjectOfCurrentPage = p.endObjectOfCurrentPage;
        return this;
    }

    public Pagination copyTo(Pagination p) {
        if (p == null) {
            p = new Pagination();
        }
        p.objectType = objectType;
        p.pageSize = pageSize;
        p.defaultPageSize = defaultPageSize;
        p.rowsNumber = rowsNumber;
        p.currentPage = currentPage;
        p.pagesNumber = pagesNumber;
        p.selection = selection;
        p.startRowOfCurrentPage = startRowOfCurrentPage;
        p.endRowOfCurrentPage = endRowOfCurrentPage;
        p.startObjectOfCurrentPage = startObjectOfCurrentPage;
        p.endObjectOfCurrentPage = endObjectOfCurrentPage;
        return p;
    }

    public void goPage(long dataSize, long page) {
        rowsNumber = dataSize < 0 ? 0 : dataSize;
        if (rowsNumber <= pageSize) {
            pagesNumber = 1;
        } else {
            pagesNumber = rowsNumber / pageSize;
            if (rowsNumber % pageSize > 0) {
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
        endRowOfCurrentPage = startRowOfCurrentPage + tableSize;
    }

    public void updatePageSize(int size) {
        if (size < 0 || pageSize == size) {
            return;
        }
        pageSize = size;
        if (rowsNumber <= pageSize) {
            pagesNumber = 1;
        } else {
            pagesNumber = rowsNumber / pageSize;
            if (rowsNumber % pageSize > 0) {
                pagesNumber++;
            }
        }
        if (startRowOfCurrentPage <= 0) {
            startRowOfCurrentPage = 0;
            currentPage = 0;
        } else {
            currentPage = startRowOfCurrentPage / pageSize;
            if (startRowOfCurrentPage % pageSize > 0) {
                currentPage++;
            }
            if (currentPage >= pagesNumber) {
                currentPage = pagesNumber - 1;
            }
        }
    }

    public String info() {
        String s = "rowsNumber:" + rowsNumber + "\n"
                + "pageSize:" + pageSize + "\n"
                + "startRowOfCurrentPage:" + startRowOfCurrentPage + "\n"
                + "endRowOfCurrentPage:" + endRowOfCurrentPage + "\n"
                + "currentPage:" + currentPage + "\n"
                + "pagesNumber:" + pagesNumber + "\n"
                + "startObjectOfCurrentPage:" + startObjectOfCurrentPage + "\n"
                + "endObjectOfCurrentPage:" + endObjectOfCurrentPage + "\n"
                + "selection:" + selection;
        return s;
    }

    /*
        get/set
     */
    public long getRowsNumber() {
        return rowsNumber;
    }

    public Pagination setRowsNumber(long rowsNumber) {
        this.rowsNumber = rowsNumber;
        return this;
    }

    public long getObjectsNumber() {
        return objectsNumber;
    }

    public Pagination setObjectsNumber(long objectsNumber) {
        this.objectsNumber = objectsNumber;
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

    public String getSelection() {
        return selection;
    }

    public Pagination setSelection(String selection) {
        this.selection = selection;
        return this;
    }

    public long getStartObjectOfCurrentPage() {
        return startObjectOfCurrentPage;
    }

    public Pagination setStartObjectOfCurrentPage(long startObjectOfCurrentPage) {
        this.startObjectOfCurrentPage = startObjectOfCurrentPage;
        return this;
    }

    public long getEndObjectOfCurrentPage() {
        return endObjectOfCurrentPage;
    }

    public Pagination setEndObjectOfCurrentPage(long endObjectOfCurrentPage) {
        this.endObjectOfCurrentPage = endObjectOfCurrentPage;
        return this;
    }

}
