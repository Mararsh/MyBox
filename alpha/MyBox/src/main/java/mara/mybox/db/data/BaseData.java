package mara.mybox.db.data;

/**
 * @Author Mara
 * @CreateDate 2020-7-19
 * @License Apache License Version 2.0
 */
public abstract class BaseData implements Cloneable {

    protected long id;

    public BaseData() {
        id = -1;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            BaseData newData = (BaseData) super.clone();
            newData.setId(-1);
            return super.clone();
        } catch (Exception e) {
            return null;
        }
    }

    /*
        get/set
     */
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
