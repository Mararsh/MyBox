package mara.mybox.controller;

import java.sql.Connection;
import java.util.List;
import mara.mybox.db.data.WebFavorite;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.table.TableWebFavorite;

/**
 * @Author Mara
 * @CreateDate 2021-4-30
 * @License Apache License Version 2.0
 */
public class WebFavoriteNodeCopyController extends BaseTreeNodeCopyController {

    protected TableWebFavorite tableWebFavorite;

    @Override
    protected String copyMembers(Connection conn, TreeNode sourceNode, TreeNode targetNode) {
        if (conn == null || sourceNode == null || targetNode == null) {
            return "InvalidData";
        }
        try {
            if (tableWebFavorite == null) {
                tableWebFavorite = new TableWebFavorite();
            }
            long sourceid = sourceNode.getNodeid();
            long targetid = targetNode.getNodeid();
            List<WebFavorite> addresses = tableWebFavorite.addresses(conn, sourceid);
            if (addresses != null) {
                conn.setAutoCommit(false);
                for (WebFavorite address : addresses) {
                    WebFavorite newAddress = new WebFavorite(targetid, address.getTitle(), address.getAddress(), address.getIcon());
                    tableWebFavorite.insertData(conn, newAddress);
                }
                conn.commit();
            }
            return null;
        } catch (Exception e) {
            return e.toString();
        }
    }

}
