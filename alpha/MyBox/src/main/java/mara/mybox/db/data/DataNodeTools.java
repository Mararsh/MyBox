package mara.mybox.db.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import javafx.scene.paint.Color;
import mara.mybox.controller.BaseController;
import mara.mybox.db.Database;
import mara.mybox.db.table.BaseNodeTable;
import static mara.mybox.db.table.BaseNodeTable.RootID;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.JsonTools;
import static mara.mybox.value.AppValues.Indent;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2024-11-1
 * @License Apache License Version 2.0
 */
public class DataNodeTools {

    public static String toHtml(FxTask fxTask, Connection conn,
            BaseController controller, BaseNodeTable dataTable,
            String parentName, DataNode node, List<DataNodeTag> tags,
            boolean withId, boolean withTime, boolean withOrder) {
        try {
            StringBuilder s = new StringBuilder();
            String indent2 = Indent + Indent;
            String indent3 = indent2 + Indent;
            s.append(indent2).append("<DIV id=\"").append(node.getNodeid()).append("\">\n");
            if (withId) {
                if (node.getNodeid() >= 0) {
                    s.append(indent3).append("<H5>")
                            .append(message("ID")).append(": ").append(node.getNodeid())
                            .append("</H5>\n");
                }
                if (node.getParentid() >= 0) {
                    s.append(indent3).append("<H5>")
                            .append(message("ParentID")).append(": ").append(node.getParentid())
                            .append("</H5>\n");
                }
            }
            if (parentName != null) {
                s.append(indent3).append("<H5>")
                        .append(message("Parent")).append(": <PRE><CODE>").append(parentName)
                        .append("</CODE></PRE></H5>\n");
            }
            if (withOrder) {
                s.append(indent3).append("<H5>")
                        .append(message("OrderNumber")).append(": ").append(node.getOrderNumber())
                        .append("</CODE></PRE></H5>\n");
            }
            if (withTime && node.getUpdateTime() != null) {
                s.append(indent3).append("<H5>")
                        .append(DateTools.datetimeToString(node.getUpdateTime()))
                        .append("</H5>\n");
            }
            if (tags != null && !tags.isEmpty()) {
                s.append(indent3).append("<H4>");
                for (DataNodeTag nodeTag : tags) {
                    Color color = nodeTag.getTag().getColor();
                    if (color == null) {
                        color = FxColorTools.randomColor();
                    }
                    s.append("<SPAN style=\"border-radius:4px; padding: 2px; font-size:0.8em;  background-color: ")
                            .append(FxColorTools.color2rgb(color))
                            .append("; color: ").append(FxColorTools.color2rgb(FxColorTools.foreColor(color)))
                            .append(";\">").append(nodeTag.getTag().getTag())
                            .append("</SPAN>\n");
                }
                s.append("</H4>\n");
            }
            s.append(indent3).append("<H4><PRE><CODE>")
                    .append(node.getTitle()).append("</CODE></PRE></H4>\n");
            String valuesHtml = dataTable.valuesHtml(fxTask, conn, controller, node);
            if (valuesHtml != null && !valuesHtml.isBlank()) {
                s.append(indent3).append(valuesHtml).append("\n");
            }
            s.append(indent2).append("</DIV><HR>\n\n");
            return s.toString();
        } catch (Exception e) {
            if (fxTask != null) {
                fxTask.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
    }

    public static String toXML(FxTask fxTask, Connection conn,
            BaseController controller, BaseNodeTable dataTable,
            String prefix, String parentName, DataNode node, List<DataNodeTag> tags,
            boolean withId, boolean withTime, boolean withOrder) {
        try {
            StringBuilder s = new StringBuilder();
            String prefix2 = prefix + Indent;
            String prefix3 = prefix2 + Indent;
            s.append(prefix).append("<Attributes>\n");
            if (withId) {
                if (node.getNodeid() >= 0) {
                    s.append(prefix2).append("<nodeid>").append(node.getNodeid()).append("</nodeid>\n");
                }
                if (node.getParentid() >= 0) {
                    s.append(prefix2).append("<parentid>").append(node.getParentid()).append("</parentid>\n");
                }
            }
            if (parentName != null) {
                s.append(prefix2).append("<parent_name>\n");
                s.append(prefix2).append("<![CDATA[").append(parentName).append("]]>\n");
                s.append(prefix2).append("</parent_name>\n");
            }
            if (node.getTitle() != null) {
                s.append(prefix2).append("<title>\n");
                s.append(prefix2).append("<![CDATA[").append(node.getTitle()).append("]]>\n");
                s.append(prefix2).append("</title>\n");
            }
            if (withOrder) {
                s.append(prefix2).append("<order_number>").append(node.getOrderNumber()).append("</order_number>\n");
            }
            if (withTime && node.getUpdateTime() != null) {
                s.append(prefix2).append("<updateTime>")
                        .append(DateTools.datetimeToString(node.getUpdateTime()))
                        .append("</updateTime>\n");
            }
            s.append(prefix).append("</Attributes>\n");
            String valuesXml = dataTable.valuesXml(fxTask, conn, controller, prefix, node);
            if (valuesXml != null && !valuesXml.isBlank()) {
                s.append(valuesXml);
            }
            if (tags != null && !tags.isEmpty()) {
                s.append(prefix).append("<Tags>\n");
                for (DataNodeTag tag : tags) {
                    s.append(prefix2).append("<Tag>\n");
                    s.append(prefix3).append("<![CDATA[").append(tag.getTag().getTag()).append("]]>\n");
                    s.append(prefix2).append("</Tag>\n");
                }
                s.append(prefix).append("</Tags>\n");
            }
            return s.toString();
        } catch (Exception e) {
            if (fxTask != null) {
                fxTask.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
    }

    public static String toJson(FxTask fxTask, Connection conn,
            BaseController controller, BaseNodeTable dataTable,
            String parentName, DataNode node, List<DataNodeTag> tags,
            boolean withId, boolean withTime, boolean withOrder) {
        try {
            StringBuilder s = new StringBuilder();
            String indent2 = Indent + Indent;
            s.append(Indent).append("{").append("\n");
            if (withId) {
                if (node.getNodeid() >= 0) {
                    s.append(indent2)
                            .append("\"").append(message("ID")).append("\": ")
                            .append(node.getNodeid());
                }
                if (node.getParentid() >= 0) {
                    s.append(",\n");
                    s.append(indent2)
                            .append("\"").append(message("ParentID")).append("\": ")
                            .append(node.getParentid());
                }
            }
            if (parentName != null) {
                s.append(",\n");
                s.append(indent2)
                        .append("\"").append(message("Parent")).append("\": \"")
                        .append(parentName).append("\"");
            }
            if (node.getTitle() != null) {
                s.append(",\n");
                s.append(indent2)
                        .append("\"").append(message("Title")).append("\": \"")
                        .append(node.getTitle()).append("\"");
            }
            if (withOrder) {
                s.append(",\n");
                s.append(indent2)
                        .append("\"").append(message("OrderNumber")).append("\": \"")
                        .append(node.getOrderNumber()).append("\"");
            }
            if (withTime && node.getUpdateTime() != null) {
                s.append(",\n");
                s.append(indent2)
                        .append("\"").append(message("UpdateTime")).append("\": \"")
                        .append(node.getUpdateTime()).append("\"");
            }
            if (tags != null && !tags.isEmpty()) {
                String t = null;
                for (DataNodeTag tag : tags) {
                    String v = tag.getTag().getTag();
                    if (t == null) {
                        t = v;
                    } else {
                        t += DataNode.TagSeparater + v;
                    }
                }
                s.append(",\n");
                s.append(indent2)
                        .append("\"").append(message("Tags")).append("\": ")
                        .append(JsonTools.encode(t));
            }
            String valuesJson = dataTable.valuesJson(fxTask, conn, controller, indent2, node);
            if (valuesJson != null && !valuesJson.isBlank()) {
                s.append(valuesJson);
            }
            s.append("\n");
            s.append(Indent).append("}").append("\n");
            return s.toString();
        } catch (Exception e) {
            if (fxTask != null) {
                fxTask.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
    }

    public static void updateIn682_move(Connection conn, BaseNodeTable dataTable, String category) {
        String tname = dataTable.getTableName();
        // for debug.Remove this block later
        try (Statement statement = conn.createStatement()) {
            conn.setAutoCommit(true);
            statement.executeUpdate("DROP TABLE " + tname + "_Node_Tag");
            statement.executeUpdate("DROP TABLE " + tname + "_Tag");
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate("DROP TABLE " + tname);
            dataTable.createTable(conn);
        } catch (Exception e) {
            MyBoxLog.console(e);
        }

        try (Statement statement = conn.createStatement()) {
            conn.setAutoCommit(true);
            statement.executeUpdate("DROP TABLE MYBOX_TMP_TREE_Migration682");
            statement.executeUpdate("DROP TABLE MYBOX_TMP_TAG_Migration682");
        } catch (Exception e) {
            MyBoxLog.console(e);
        }

        try (Statement statement = conn.createStatement()) {
            conn.setAutoCommit(true);

            statement.executeUpdate("CREATE TABLE MYBOX_TMP_TREE_Migration682"
                    + " ( old_nodeid BIGINT, old_parentid BIGINT, new_nodeid BIGINT)");
            ResultSet query = conn.createStatement().executeQuery(
                    "SELECT * FROM tree_node WHERE category='" + category + "' ORDER BY nodeid");
            conn.setAutoCommit(false);
            long count = 0;
            while (query.next()) {
                try {
                    DataNode node = fromText(tname, query.getString("info"));
                    if (node == null) {
                        continue;
                    }
                    long nodeid = query.getLong("nodeid");
                    long parentid = query.getLong("parentid");
                    node.setNodeid(-1).setParentid(RootID)
                            .setTitle(query.getString("title"));
                    node = dataTable.insertData(conn, node);
                    long newNodeid = node.getNodeid();
                    if (newNodeid >= 0) {
                        if (++count % Database.BatchSize == 0) {
                            conn.commit();
                        }
                        statement.executeUpdate("INSERT INTO MYBOX_TMP_TREE_Migration682 VALUES ("
                                + nodeid + ", " + parentid + ", " + newNodeid + ")");
                    }

                } catch (Exception e) {
                    MyBoxLog.console(e);
                }
            }
            conn.commit();

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

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    // only for migration
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
                    node.setValue("text", text);
                    break;
                case "Node_Html":
                    node.setValue("html", text);
                    break;
                case InfoNode.WebFavorite:
                    node.setValue("Address", null);
                    node.setValue("Icon", null);
                    if (info != null) {
                        if (info.contains(ValueSeparater)) {
                            String[] ss = info.split(ValueSeparater);
                            if (ss.length > 0) {
                                node.setValue("Address", ss[0].trim());
                            }
                            if (ss.length > 1) {
                                node.setValue("Icon", ss[1].trim());
                            }
                        } else if (info.contains(MoreSeparater)) {
                            String[] ss = info.split(MoreSeparater);
                            if (ss.length > 0) {
                                node.setValue("Address", ss[0].trim());
                            }
                            if (ss.length > 1) {
                                node.setValue("Icon", ss[1].trim());
                            }
                        } else {
                            node.setValue("Address", info);
                        }
                    }
                    break;
                case InfoNode.JShellCode:
                    node.setValue("Codes", info);
                    break;
                case InfoNode.SQL:
                    node.setValue("SQL", info);
                    break;
                case InfoNode.JavaScript:
                    node.setValue("Script", info);
                    break;
                case InfoNode.JEXLCode:
                    node.setValue("Script", null);
                    node.setValue("Context", null);
                    node.setValue("Parameters", null);
                    if (info != null) {
                        String[] ss = null;
                        if (info.contains(ValueSeparater)) {
                            ss = info.split(ValueSeparater);
                        } else if (info.contains(MoreSeparater)) {
                            ss = info.split(MoreSeparater);
                        }
                        if (ss != null) {
                            if (ss.length > 0) {
                                node.setValue("Script", ss[0].trim());
                            }
                            if (ss.length > 1) {
                                node.setValue("Context", ss[1].trim());
                            }
                            if (ss.length > 2) {
                                node.setValue("Parameters", ss[2].trim());
                            }
                        } else {
                            node.setValue("Script", info);
                        }
                    }
                    break;
                case InfoNode.RowFilter:
                    node.setValue("Script", null);
                    node.setValue("Condition", "true");
                    node.setValue("Maximum", "-1");
                    if (info != null) {
                        if (info.contains(ValueSeparater)) {
                            String[] ss = info.split(ValueSeparater);
                            if (ss.length > 0) {
                                node.setValue("Script", ss[0].trim());
                            }
                            if (ss.length > 1) {
                                node.setValue("Condition", ss[1].trim());
                            }
                            if (ss.length > 2) {
                                node.setValue("Maximum", ss[2].trim());
                            }
                        } else if (info.contains(MoreSeparater)) {
                            String[] ss = info.split(MoreSeparater);
                            node.setValue("Script", ss[0].trim());
                            if (ss.length > 1) {
                                ss = ss[1].split(";;;");
                                if (ss.length > 0) {
                                    node.setValue("Condition", ss[0].trim());
                                }
                                if (ss.length > 1) {
                                    node.setValue("Maximum", ss[1].trim());
                                }
                            }
                        } else {
                            node.setValue("Script", info);
                        }
                    }
                    break;
                case InfoNode.MathFunction:
                    node.setValue("MathFunctionName", null);
                    node.setValue("Variables", null);
                    node.setValue("Expression", null);
                    node.setValue("FunctionDomain", null);
                    if (info != null) {
                        if (info.contains(ValueSeparater)) {
                            String[] ss = info.split(ValueSeparater);
                            if (ss.length > 0) {
                                node.setValue("MathFunctionName", ss[0].trim());
                            }
                            if (ss.length > 1) {
                                node.setValue("Variables", ss[1].trim());
                            }
                            if (ss.length > 2) {
                                node.setValue("Expression", ss[2].trim());
                            }
                            if (ss.length > 3) {
                                node.setValue("FunctionDomain", ss[3].trim());
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
                                    node.setValue("MathFunctionName", names.substring(0, pos));
                                    String vs = names.substring(pos).trim();
                                    if (vs.length() > 0) {
                                        node.setValue("Variables", vs.substring(1));
                                    }
                                } else {
                                    node.setValue("MathFunctionName", names);
                                }
                            }
                            if (info != null && info.contains(MoreSeparater)) {
                                String[] ss = info.split(MoreSeparater);
                                node.setValue("Expression", ss[0].trim());
                                if (ss.length > 1) {
                                    node.setValue("FunctionDomain", ss[1].trim());
                                }
                            } else {
                                node.setValue("Expression", info);
                            }
                        }
                    }
                    break;
                case InfoNode.ImageMaterial:
                    node.setValue("Value", info);
                    break;
                case InfoNode.Data2DDefinition:
                case InfoNode.ImageScope:
                    node.setValue("XML", info);
                    break;
            }
            return node;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
