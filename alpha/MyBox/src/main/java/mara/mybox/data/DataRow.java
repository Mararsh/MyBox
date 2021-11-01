package mara.mybox.data;

import mara.mybox.db.data.*;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class DataRow extends BaseData {

    private void init() {

    }

    public DataRow() {
        init();
    }

    /*
        Static methods
     */
    public static DataRow create() {
        return new DataRow();
    }

    /*
        get/set
     */
}
