package mara.mybox.db.data;

/**
 * @Author Mara
 * @CreateDate 2020-7-19
 * @License Apache License Version 2.0
 */
public abstract class BaseData implements Cloneable {

    protected long baseID = -1;
    protected int rowIndex = -1;

    /*
        abstract
     */
    public abstract boolean valid();

    public abstract boolean setValue(String column, Object value);

    public abstract Object getValue(String column);


    /*
        others
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            BaseData newData = (BaseData) super.clone();
            return newData;
        } catch (Exception e) {
            return null;
        }
    }

    /*
        get/set
     */
    public long getBaseID() {
        return baseID;
    }

    public void setBaseID(long baseID) {
        this.baseID = baseID;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

}
