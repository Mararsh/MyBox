package mara.mybox.db.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.ImageScopeTools;
import mara.mybox.controller.MyBoxLoadingController;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.tools.Data2DDefinitionTools;
import mara.mybox.db.Database;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.DataNodeTag;
import mara.mybox.db.data.DataTag;
import mara.mybox.db.table.BaseNodeTable;
import static mara.mybox.db.table.BaseNodeTable.RootID;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.db.table.TableNodeDataColumn;
import mara.mybox.db.table.TableNodeGeographyCode;
import mara.mybox.db.table.TableNodeHtml;
import mara.mybox.db.table.TableNodeImageScope;
import mara.mybox.db.table.TableNodeJEXL;
import mara.mybox.db.table.TableNodeJShell;
import mara.mybox.db.table.TableNodeJavaScript;
import mara.mybox.db.table.TableNodeMathFunction;
import mara.mybox.db.table.TableNodeRowExpression;
import mara.mybox.db.table.TableNodeSQL;
import mara.mybox.db.table.TableNodeText;
import mara.mybox.db.table.TableNodeWebFavorite;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-06-09
 * @License Apache License Version 2.0
 */
public class DataMigrationFrom68 {

    public static void handleVersions(MyBoxLoadingController controller,
            int lastVersion, Connection conn, String lang) {
        try {
            if (lastVersion < 6008000) {
                updateIn68(conn);
            }
            if (lastVersion < 6008002) {
                updateIn682(controller, conn);
            }
            if (lastVersion < 6008003) {
                updateIn683(controller, conn, lang);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn683(MyBoxLoadingController controller,
            Connection conn, String lang) {
        try {
            MyBoxLog.info("Updating tables in 6.8.3...");
            controller.info("Updating tables in 6.8.3...");

            TableNodeGeographyCode gcTable = new TableNodeGeographyCode();

            String tname = gcTable.getTableName();
            controller.info("Moving data: " + gcTable.getTreeName());
            // for debug.Remove this block later
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("DROP TABLE " + tname);
            } catch (Exception e) {
                MyBoxLog.console(e);
            }
            try {
                gcTable.createTable(conn);
            } catch (Exception e) {
                MyBoxLog.console(e);
            }

            try (Statement statement = conn.createStatement()) {
                conn.setAutoCommit(true);
                statement.executeUpdate("DROP TABLE MYBOX_TMP_TREE_Migration683");
            } catch (Exception e) {
//            MyBoxLog.console(e);
            }

            try (Statement statement = conn.createStatement()) {
                conn.setAutoCommit(true);

                statement.executeUpdate("CREATE TABLE MYBOX_TMP_TREE_Migration683"
                        + " ( old_nodeid BIGINT, old_parentid BIGINT, new_nodeid BIGINT)");
                ResultSet query = conn.createStatement().executeQuery(
                        "SELECT * FROM Geography_Code ORDER BY gcid");
                conn.setAutoCommit(false);
                long count = 0;
                float orderNum = 0;
                DataNode node;
                boolean isChinese = Languages.isChinese(lang);
                while (query.next()) {
                    try {
                        long nodeid = query.getLong("gcid");
                        long parentid = query.getLong("owner");
                        String chinese_name = query.getString("chinese_name");
                        String english_name = query.getString("english_name");
                        String name = isChinese && chinese_name != null ? chinese_name
                                : (english_name != null ? english_name : chinese_name);
                        node = DataNode.create()
                                .setNodeid(-1).setParentid(RootID)
                                .setOrderNumber(++orderNum)
                                .setTitle(name);
                        node.setValue("level", query.getShort("level"));
                        node.setValue("coordinate_system", query.getShort("coordinate_system"));
                        node.setValue("longitude", query.getDouble("longitude"));
                        node.setValue("latitude", query.getDouble("latitude"));
                        node.setValue("altitude", query.getDouble("altitude"));
                        node.setValue("precision", query.getDouble("precision"));
                        node.setValue("chinese_name", chinese_name);
                        node.setValue("english_name", english_name);
                        node.setValue("code1", query.getString("code1"));
                        node.setValue("code2", query.getString("code2"));
                        node.setValue("code3", query.getString("code3"));
                        node.setValue("code4", query.getString("code4"));
                        node.setValue("code5", query.getString("code5"));
                        node.setValue("alias1", query.getString("alias1"));
                        node.setValue("alias2", query.getString("alias2"));
                        node.setValue("alias3", query.getString("alias3"));
                        node.setValue("alias4", query.getString("alias4"));
                        node.setValue("alias5", query.getString("alias5"));
                        node.setValue("area", query.getLong("area") + 0d);
                        node.setValue("population", query.getLong("population"));
                        node.setValue("description", query.getString("comments"));
                        node = gcTable.insertData(conn, node);
                        long newNodeid = node.getNodeid();
                        if (newNodeid >= 0) {
                            if (++count % Database.BatchSize == 0) {
                                conn.commit();
                            }
                            statement.executeUpdate("INSERT INTO MYBOX_TMP_TREE_Migration683 VALUES ("
                                    + nodeid + ", " + parentid + ", " + newNodeid + ")");
                        }
                    } catch (Exception e) {
                        MyBoxLog.console(e);
                    }
                }
                conn.commit();
                controller.info("Saved: " + gcTable.getTreeName() + " count:" + count);

                query = conn.createStatement().executeQuery("select A.new_nodeid AS nodeid,"
                        + " B.new_nodeid AS parentid "
                        + " from MYBOX_TMP_TREE_Migration683 A, MYBOX_TMP_TREE_Migration683 AS B "
                        + " WHERE A.old_parentid=B.old_nodeid");
                conn.setAutoCommit(false);
                count = 0;
                while (query.next()) {
                    try {
                        long nodeid = query.getLong("nodeid");
                        long parentid = query.getLong("parentid");
                        if (nodeid == parentid) {
                            parentid = RootID;
                        }
                        String sql = "UPDATE " + tname + " SET parentid=" + parentid + " WHERE nodeid=" + nodeid;
                        statement.executeUpdate(sql);
                        if (++count % Database.BatchSize == 0) {
                            conn.commit();
                        }
                    } catch (Exception e) {
                        MyBoxLog.console(e);
                    }
                }
                conn.commit();
                controller.info("Moved: " + gcTable.getTreeName() + " count:" + count);

            } catch (Exception e) {
                MyBoxLog.error(e);
            }

//            try (Statement statement = conn.createStatement()) {
//                conn.setAutoCommit(true);
//                statement.executeUpdate("DROP TABLE MYBOX_TMP_TREE_Migration683");
//                statement.executeUpdate("DROP TABLE Geography_Code");
//            } catch (Exception e) {
//                MyBoxLog.console(e);
//            }
        } catch (Exception e) {
//            MyBoxLog.console(e);
        }
    }

    public static void updateIn682(MyBoxLoadingController controller, Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.8.2...");
            controller.info("Updating tables in 6.8.2...");

            updateIn682_move(controller, conn, new TableNodeHtml(), "Notebook");
            updateIn682_move(controller, conn, new TableNodeText(), "InformationInTree");
            updateIn682_move(controller, conn, new TableNodeWebFavorite(), "WebFavorite");
            updateIn682_move(controller, conn, new TableNodeSQL(), "SQL");
            updateIn682_move(controller, conn, new TableNodeMathFunction(), "MathFunction");
            updateIn682_move(controller, conn, new TableNodeImageScope(), "ImageScope");
            updateIn682_move(controller, conn, new TableNodeJShell(), "JShellCode");
            updateIn682_move(controller, conn, new TableNodeJEXL(), "JEXLCode");
            updateIn682_move(controller, conn, new TableNodeJavaScript(), "JavaScript");
            updateIn682_move(controller, conn, new TableNodeRowExpression(), "RowFilter");
            updateIn682_move(controller, conn, new TableNodeDataColumn(), "Data2DDefinition");

            try (Statement statement = conn.createStatement()) {
                conn.setAutoCommit(true);
                statement.executeUpdate("DROP TABLE MYBOX_TMP_TREE_Migration682");
                statement.executeUpdate("DROP TABLE MYBOX_TMP_TAG_Migration682");
                statement.executeUpdate("DROP TABLE tree_node_tag");
                statement.executeUpdate("DROP TABLE tree_node");
                statement.executeUpdate("DROP TABLE tag");
            } catch (Exception e) {
                MyBoxLog.console(e);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn682_move(MyBoxLoadingController controller, Connection conn,
            BaseNodeTable dataTable, String category) {
        String tname = dataTable.getTableName();
        controller.info("Moving data: " + dataTable.getTreeName());
        // for debug.Remove this block later
//        try (Statement statement = conn.createStatement()) {
//            conn.setAutoCommit(true);
//            statement.executeUpdate("DROP TABLE " + tname + "_Node_Tag");
//            statement.executeUpdate("DROP TABLE " + tname + "_Tag");
//        } catch (Exception e) {
//            MyBoxLog.console(e);
//        }
//        try (Statement statement = conn.createStatement()) {
//            statement.executeUpdate("DROP TABLE " + tname);
//        } catch (Exception e) {
//            MyBoxLog.console(e);
//        }
//        try {
//            dataTable.createTable(conn);
//        } catch (Exception e) {
//            MyBoxLog.console(e);
//        }

        try (Statement statement = conn.createStatement()) {
            conn.setAutoCommit(true);
            statement.executeUpdate("DROP TABLE MYBOX_TMP_TREE_Migration682");
            statement.executeUpdate("DROP TABLE MYBOX_TMP_TAG_Migration682");
        } catch (Exception e) {
//            MyBoxLog.console(e);
        }

        try (Statement statement = conn.createStatement()) {
            conn.setAutoCommit(true);

            statement.executeUpdate("CREATE TABLE MYBOX_TMP_TREE_Migration682"
                    + " ( old_nodeid BIGINT, old_parentid BIGINT, new_nodeid BIGINT)");
            ResultSet query = conn.createStatement().executeQuery(
                    "SELECT * FROM tree_node WHERE category='" + category + "' ORDER BY nodeid");
            conn.setAutoCommit(false);
            long count = 0;
            float orderNum = 0;
            DataNode node;
            DataFileCSV data2D;
            while (query.next()) {
                try {
                    String info = query.getString("info");
                    if ("Node_Data_Column".equals(tname)) {
                        data2D = Data2DDefinitionTools.fromXML(info);
                        if (data2D == null) {
                            continue;
                        }
                        node = new DataNode();

                    } else {
                        node = fromText(tname, info);
                        if (node == null) {
                            continue;
                        }
                        data2D = null;
                    }
                    long nodeid = query.getLong("nodeid");
                    long parentid = query.getLong("parentid");
                    node.setNodeid(-1).setParentid(RootID)
                            .setOrderNumber(++orderNum)
                            .setTitle(query.getString("title"));
                    node = dataTable.insertData(conn, node);
                    long newNodeid = node.getNodeid();
                    if (newNodeid >= 0) {
                        if (++count % Database.BatchSize == 0) {
                            conn.commit();
                        }
                        statement.executeUpdate("INSERT INTO MYBOX_TMP_TREE_Migration682 VALUES ("
                                + nodeid + ", " + parentid + ", " + newNodeid + ")");
                        if (data2D != null) {
                            int corder = 0;
                            for (Data2DColumn column : data2D.getColumns()) {
                                DataNode columnNode = TableNodeDataColumn.fromColumn(column);
                                columnNode.setParentid(newNodeid).setOrderNumber(++corder);
                                dataTable.insertData(conn, columnNode);
                            }
                        }
                    }
                } catch (Exception e) {
                    MyBoxLog.console(e);
                }
            }
            conn.commit();
            controller.info("Saved: " + dataTable.getTreeName() + " count:" + count);

            query = conn.createStatement().executeQuery("select A.new_nodeid AS nodeid,"
                    + " B.new_nodeid AS parentid "
                    + " from MYBOX_TMP_TREE_Migration682 A, MYBOX_TMP_TREE_Migration682 AS B "
                    + " WHERE A.old_parentid=B.old_nodeid");
            conn.setAutoCommit(false);
            count = 0;
            while (query.next()) {
                try {
                    long nodeid = query.getLong("nodeid");
                    long parentid = query.getLong("parentid");
                    if (nodeid == parentid) {
                        parentid = RootID;
                    }
                    String sql = "UPDATE " + tname + " SET parentid=" + parentid + " WHERE nodeid=" + nodeid;
                    statement.executeUpdate(sql);
                    if (++count % Database.BatchSize == 0) {
                        conn.commit();
                    }
                } catch (Exception e) {
                    MyBoxLog.console(e);
                }
            }
            conn.commit();
            controller.info("Moved: " + dataTable.getTreeName() + " count:" + count);

            statement.executeUpdate("CREATE TABLE MYBOX_TMP_TAG_Migration682"
                    + " ( old_tagid BIGINT, new_tagid BIGINT)");
            query = conn.createStatement().executeQuery("select * from tag where category='" + category + "' ORDER BY tgid ");
            conn.setAutoCommit(false);
            count = 0;
            TableDataTag tableTreeTag = new TableDataTag(dataTable);
            while (query.next()) {
                try {
                    DataTag tag = new DataTag()
                            .setTag(query.getString("tag"))
                            .setColorString(query.getString("color"));
                    tag = tableTreeTag.insertData(conn, tag);
                    statement.executeUpdate("INSERT INTO MYBOX_TMP_TAG_Migration682 VALUES ("
                            + query.getLong("tgid") + ", " + tag.getTagid() + ")");
                    if (++count % Database.BatchSize == 0) {
                        conn.commit();
                    }
                } catch (Exception e) {
                    MyBoxLog.console(e);
                }
            }
            conn.commit();
            controller.info("Tags saved: " + dataTable.getTreeName() + " count:" + count);

            query = conn.createStatement().executeQuery("select  C.new_nodeid AS mnodeid, B.new_tagid AS mtagid "
                    + " from tree_node_tag A, MYBOX_TMP_TAG_Migration682 AS B, MYBOX_TMP_TREE_Migration682 AS C"
                    + " WHERE A.tnodeid=C.old_nodeid AND A.tagid=B.old_tagid ");
            conn.setAutoCommit(false);
            count = 0;
            TableDataNodeTag tableTreeNodeTag = new TableDataNodeTag(dataTable);
            while (query.next()) {
                try {
                    DataNodeTag nodeTag = new DataNodeTag()
                            .setTnodeid(query.getLong("mnodeid"))
                            .setTtagid(query.getLong("mtagid"));
                    tableTreeNodeTag.insertData(conn, nodeTag);
                    if (++count % Database.BatchSize == 0) {
                        conn.commit();
                    }
                } catch (Exception e) {
                    MyBoxLog.console(e);
                }
            }
            conn.commit();
            conn.setAutoCommit(true);
            controller.info("Tags moved: " + dataTable.getTreeName() + " count:" + count);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static DataNode fromText(String tableName, String text) {
        try {
            if (tableName == null) {
                return null;
            }
            DataNode node = new DataNode();
            if (text == null || text.isBlank()) {
                return node;
            }
            String ValueSeparater = "_:;MyBoxNodeValue;:_";
            String MoreSeparater = "MyBoxTreeNodeMore:";
            String info = text.trim();
            switch (tableName) {
                case "Node_Text":
                    node.setValue("text", info);
                    break;
                case "Node_Html":
                    node.setValue("html", info);
                    break;
                case "Node_Web_Favorite":
                    node.setValue("address", null);
                    node.setValue("icon", null);
                    if (info != null) {
                        if (info.contains(ValueSeparater)) {
                            String[] ss = info.split(ValueSeparater);
                            if (ss.length > 0) {
                                node.setValue("address", ss[0].trim());
                            }
                            if (ss.length > 1) {
                                node.setValue("icon", ss[1].trim());
                            }
                        } else if (info.contains(MoreSeparater)) {
                            String[] ss = info.split(MoreSeparater);
                            if (ss.length > 0) {
                                node.setValue("address", ss[0].trim());
                            }
                            if (ss.length > 1) {
                                node.setValue("icon", ss[1].trim());
                            }
                        } else {
                            node.setValue("address", info);
                        }
                    }
                    break;
                case "Node_SQL":
                    node.setValue("statement", info);
                    break;
                case "Node_Image_Scope":
                    ImageScope scope = ImageScopeTools.fromXML(null, null, info);
                    return ImageScopeTools.toDataNode(node, scope);
                case "Node_JShell":
                    node.setValue("codes", info);
                    break;
                case "Node_JavaScript":
                    node.setValue("script", info);
                    break;
                case "Node_JEXL":
                    node.setValue("script", null);
                    node.setValue("context", null);
                    node.setValue("parameters", null);
                    if (info != null) {
                        String[] ss = null;
                        if (info.contains(ValueSeparater)) {
                            ss = info.split(ValueSeparater);
                        } else if (info.contains(MoreSeparater)) {
                            ss = info.split(MoreSeparater);
                        }
                        if (ss != null) {
                            if (ss.length > 0) {
                                node.setValue("script", ss[0].trim());
                            }
                            if (ss.length > 1) {
                                node.setValue("context", ss[1].trim());
                            }
                            if (ss.length > 2) {
                                node.setValue("parameters", ss[2].trim());
                            }
                        } else {
                            node.setValue("script", info);
                        }
                    }
                    break;
                case "Node_Row_Expression":
                    node.setValue("script", null);
                    if (info != null) {
                        if (info.contains(ValueSeparater)) {
                            String[] ss = info.split(ValueSeparater);
                            if (ss.length > 0) {
                                node.setValue("script", ss[0].trim());
                            }
                        } else if (info.contains(MoreSeparater)) {
                            String[] ss = info.split(MoreSeparater);
                            node.setValue("script", ss[0].trim());
                        } else {
                            node.setValue("script", info);
                        }
                    }
                    break;
                case "Node_Math_Function":
                    node.setValue("variables", null);
                    node.setValue("expression", null);
                    node.setValue("domain", null);
                    if (info != null) {
                        if (info.contains(ValueSeparater)) {
                            String[] ss = info.split(ValueSeparater);
                            if (ss.length > 0) {
                                node.setTitle(ss[0].trim());
                            }
                            if (ss.length > 1) {
                                node.setValue("variables", ss[1].trim());
                            }
                            if (ss.length > 2) {
                                node.setValue("expression", ss[2].trim());
                            }
                            if (ss.length > 3) {
                                node.setValue("domain", ss[3].trim());
                            }
                        } else {
                            String prefix = "Names:::";
                            if (info.startsWith(prefix)) {
                                info = info.substring(prefix.length());
                                int pos = info.indexOf("\n");
                                String names;
                                if (pos >= 0) {
                                    names = info.substring(0, pos);
                                    info = info.substring(pos);
                                } else {
                                    names = info;
                                    info = null;
                                }
                                pos = names.indexOf(",");
                                if (pos >= 0) {
                                    node.setTitle(names.substring(0, pos));
                                    String vs = names.substring(pos).trim();
                                    if (vs.length() > 0) {
                                        node.setValue("variables", vs.substring(1));
                                    }
                                } else {
                                    node.setTitle(names);
                                }
                            }
                            if (info != null && info.contains(MoreSeparater)) {
                                String[] ss = info.split(MoreSeparater);
                                node.setValue("expression", ss[0].trim());
                                if (ss.length > 1) {
                                    node.setValue("domain", ss[1].trim());
                                }
                            } else {
                                node.setValue("expression", info);
                            }
                        }
                    }
                    break;
            }
            return node;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static void updateIn68(Connection conn) {
        try (Statement statement = conn.createStatement()) {
            MyBoxLog.info("Updating tables in 6.8...");

            conn.setAutoCommit(true);
            TableInfoNode tableTreeNode = new TableInfoNode();
            InfoNode rootNode = tableTreeNode.findAndCreateRoot(conn, InfoNode.ImageScope);
            if (rootNode == null) {
                return;
            }
            long rootid = rootNode.getNodeid(), parentid;
            ResultSet query = statement.executeQuery("SELECT * FROM image_scope");
            HashMap<String, Long> parents = new HashMap<>();
            conn.setAutoCommit(false);
            while (query.next()) {
                ImageScope scope = new ImageScope();
                try {
                    ImageScope.ScopeType type = ImageScopeTools.scopeType(query.getString("scope_type"));
                    if (ImageScopeTools.decodeAreaData(type, query.getString("area_data"), scope)
                            && ImageScopeTools.decodeColorData(type, query.getString("color_data"), scope)
                            && ImageScopeTools.decodeOutline(null, type, query.getString("outline"), scope)) {
                        scope.setFile(query.getString("image_location"));
                        scope.setName(query.getString("name"));
                        scope.setScopeType(type);
                        scope.setColorScopeType(ImageScope.ColorScopeType.valueOf(query.getString("color_scope_type")));
                        scope.setColorDistance(query.getInt("color_distance"));
                        scope.setHsbDistance((float) query.getDouble("hsb_distance"));
                        scope.setAreaExcluded(query.getBoolean("area_excluded"));
                        scope.setColorExcluded(query.getBoolean("color_excluded"));
                        scope.setCreateTime(query.getTimestamp("create_time"));
                        scope.setModifyTime(query.getTimestamp("modify_time"));
                    } else {
                        continue;
                    }
                } catch (Exception e) {
                    MyBoxLog.console(e);
                    continue;
                }
                String file = scope.getFile();
                InfoNode scopeNode = new InfoNode()
                        .setCategory(InfoNode.ImageScope)
                        .setTitle(scope.getName())
                        .setInfo(ImageScopeTools.toXML(scope, ""))
                        .setUpdateTime(scope.getModifyTime());
                parentid = rootid;
                if (file != null && !file.isBlank()) {
                    if (parents.containsKey(file)) {
                        parentid = parents.get(file);
                    } else {
                        InfoNode parentNode = new InfoNode()
                                .setCategory(InfoNode.ImageScope)
                                .setParentid(rootid)
                                .setTitle(file)
                                .setUpdateTime(scope.getModifyTime());
                        parentNode = tableTreeNode.insertData(parentNode);
                        parentid = parentNode.getNodeid();
                        parents.put(file, parentid);
                    }
                }
                scopeNode.setParentid(parentid);
                tableTreeNode.insertData(scopeNode);
            }
            conn.commit();
            conn.setAutoCommit(true);

            statement.executeUpdate("DROP TABLE image_scope");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}