package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2022-2-12
 * @License Apache License Version 2.0
 */
public class DataInternalTable extends DataTable {

    public DataInternalTable() {
        type = Type.InternalTable;
    }

    @Override
    public int type() {
        return type(Type.InternalTable);
    }

}
