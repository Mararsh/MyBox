package mara.mybox.db.table;

import java.sql.Connection;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2024-8-12
 * @License Apache License Version 2.0
 */
public abstract class BaseTreeData<D> extends BaseTable<D> {

    public abstract long insertData(Connection conn, String title, String info);

    public boolean initTreeTables(Connection conn) {
        if (conn == null || tableName == null) {
            return false;
        }
        try {
            TableDataNode treeTable = new TableDataNode(this);
            treeTable.createTable(conn);
            treeTable.createIndices(conn);

            new TableDataTag(this).createTable(conn);

            TableDataNodeTag nodeTagTable = new TableDataNodeTag(this);
            nodeTagTable.createTable(conn);
            nodeTagTable.createIndices(conn);

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean createTableTree(Connection conn, boolean dropExisted) {
        if (conn == null || tableName == null) {
            return false;
        }
        try {
            TableDataNode tableTree = new TableDataNode(this);
            tableTree.createTable(conn, dropExisted);
            tableTree.createIndices(conn);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

}
