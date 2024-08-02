package mara.mybox.db.data;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TreeNode extends BaseData {

    protected String tableName, idName;

    private void init() {
    }

    public TreeNode() {
        init();
    }

    /*
        get/set
     */
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getIdName() {
        return idName;
    }

    public void setIdName(String idName) {
        this.idName = idName;
    }

}
