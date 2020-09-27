package mara.mybox.data;

import mara.mybox.db.TableBase;

/**
 * @Author Mara
 * @CreateDate 2020-7-19
 * @License Apache License Version 2.0
 */
public abstract class TableData implements Cloneable {

    protected TableBase table;
    protected int maxColumnIndex;

    private void init() {
        maxColumnIndex = 20;
    }

    public TableData() {
        init();
    }

    public TableData(TableBase table) {
        this.table = table;
        init();
    }

    /*
        Abstract methods
     */
    protected abstract TableBase getTable();

    protected abstract Object getValue(String name);

    protected abstract boolean setValue(String name, Object value);

    protected abstract boolean valid();

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            return super.clone();
        } catch (Exception e) {
            return null;
        }
    }

    /*
        get/set
     */
    public void setTable(TableBase table) {
        this.table = table;
    }

}
