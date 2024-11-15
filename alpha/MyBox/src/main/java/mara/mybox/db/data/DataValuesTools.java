package mara.mybox.db.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import mara.mybox.db.Database;
import mara.mybox.db.table.BaseDataTable;
import static mara.mybox.db.table.BaseTable.StringMaxLength;
import mara.mybox.db.table.TableDataNode;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.db.table.TableDataTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.JsonTools;

/**
 * @Author Mara
 * @CreateDate 2024-11-1
 * @License Apache License Version 2.0
 */
public class DataValuesTools {

    public static String toXml(DataValues data, String prefix) {
        String xml = prefix + "<node>\n";
        String info = (String) data.getValue("info");
        String title = (String) data.getValue("title");
        if (title != null && !title.isBlank()) {
            xml += prefix + prefix + "<title>\n"
                    + prefix + prefix + prefix + "<![CDATA[" + title.trim() + "]]>\n"
                    + prefix + prefix + "</title>\n";
        }
        if (info != null && !info.isBlank()) {
            xml += prefix + prefix + "<info>\n"
                    + prefix + prefix + prefix + "<![CDATA[" + info.trim() + "]]>\n"
                    + prefix + prefix + "</info>\n";
        }
        xml += prefix + "</node>\n";
        return xml;
    }

    public static String toHtml(DataValues data) {
        String html = "";
        String info = (String) data.getValue("info");
        String title = (String) data.getValue("title");
        if (title != null && !title.isBlank()) {
            html += "<H2>" + title.trim() + "</H2>\n";
        }
        if (info != null && !info.isBlank()) {
            html += info.trim();
        }
        return html;
    }

    public static String toJson(DataValues data, String prefix) {
        String json = "";
        String info = (String) data.getValue("info");
        String title = (String) data.getValue("title");
        if (title != null && !title.isBlank()) {
            json += prefix + ",\n"
                    + prefix + "\"title\": " + JsonTools.encode(title.trim());
        }
        if (info != null && !info.isBlank()) {
            json += prefix + ",\n"
                    + prefix + "\"info\": " + JsonTools.encode(info.trim());
        }
        return json;
    }

    public static void updateIn682_move(Connection conn, BaseDataTable dataTable, String category) {
        String tname = dataTable.getTableName();
        // for debug.Remove this block later
        try (Statement statement = conn.createStatement()) {
            conn.setAutoCommit(true);
            statement.executeUpdate("DROP TABLE " + tname + "_Node_Tag");
            statement.executeUpdate("DROP TABLE " + tname + "_Node");
            statement.executeUpdate("DROP TABLE " + tname + "_Tag");
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate("DROP TABLE " + tname);
            dataTable.createTable(conn);
            dataTable.initTreeTables(conn);
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
                    + " ( old_nodeid BIGINT, title VARCHAR(" + StringMaxLength + "), old_parentid BIGINT, new_nodeid BIGINT)");
            ResultSet query = conn.createStatement().executeQuery("SELECT * FROM tree_node WHERE category='" + category + "' ORDER BY nodeid");
            conn.setAutoCommit(false);
            long count = 0;
            while (query.next()) {
                try {
                    String title = query.getString("title");
                    DataValues values = DataValuesTools.fromText(dataTable, query.getString("info"));
                    if (values == null) {
                        continue;
                    }
                    values = (DataValues) dataTable.insertData(conn, values);
                    long newid = values.getId(dataTable);
                    if (newid >= 0) {
                        if (++count % Database.BatchSize == 0) {
                            conn.commit();
                        }
                        statement.executeUpdate("INSERT INTO MYBOX_TMP_TREE_Migration682 VALUES ("
                                + query.getLong("nodeid") + ", '" + (title != null ? title : "") + "', "
                                + query.getLong("parentid") + ", " + newid + ")");
                    }

                } catch (Exception e) {
                    MyBoxLog.console(e);
                }
            }
            conn.commit();
            conn.setAutoCommit(true);

            query = conn.createStatement().executeQuery("select A.new_nodeid AS nodeid,"
                    + " A.title AS title, B.new_nodeid AS parentid "
                    + " from MYBOX_TMP_TREE_Migration682 A, MYBOX_TMP_TREE_Migration682 AS B "
                    + " WHERE A.old_parentid=B.old_nodeid");
            conn.setAutoCommit(false);
            count = 0;
            TableDataNode tableTree = new TableDataNode(dataTable);
            while (query.next()) {
                try {
                    DataNode treeNode = new DataNode()
                            .setNodeid(query.getLong("nodeid"))
                            .setTitle(query.getString("title"))
                            .setParentid(query.getLong("parentid"));
                    tableTree.insertData(conn, treeNode);
                    if (++count % Database.BatchSize == 0) {
                        conn.commit();
                    }
                } catch (Exception e) {
                    MyBoxLog.console(e);
                }
            }
            conn.commit();
            conn.setAutoCommit(true);

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
                    if (++count % Database.BatchSize == 0) {
                        conn.commit();
                    }
                    statement.executeUpdate("INSERT INTO MYBOX_TMP_TAG_Migration682 VALUES ("
                            + query.getLong("tgid") + ", " + tag.getTagid() + ")");
                } catch (Exception e) {
                    MyBoxLog.console(e);
                }
            }
            conn.commit();
            conn.setAutoCommit(true);

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
    public static DataValues fromText(BaseDataTable dataTable, String text) {
        try {
            if (dataTable == null) {
                return null;
            }
            DataValues values = new DataValues();
            values.setId(dataTable, -1);
            if (text == null || text.isBlank()) {
                return values;
            }
            String ValueSeparater = "_:;MyBoxNodeValue;:_";
            String MoreSeparater = "MyBoxTreeNodeMore:";
            String info = text.trim();
            switch (dataTable.getTableName()) {
                case "Info_In_Tree":
                    values.setValue("info", text);
                    break;
                case "Note":
                    values.setValue("note", text);
                    break;
                case InfoNode.WebFavorite:
                    values.setValue("Address", null);
                    values.setValue("Icon", null);
                    if (info != null) {
                        if (info.contains(ValueSeparater)) {
                            String[] ss = info.split(ValueSeparater);
                            if (ss.length > 0) {
                                values.setValue("Address", ss[0].trim());
                            }
                            if (ss.length > 1) {
                                values.setValue("Icon", ss[1].trim());
                            }
                        } else if (info.contains(MoreSeparater)) {
                            String[] ss = info.split(MoreSeparater);
                            if (ss.length > 0) {
                                values.setValue("Address", ss[0].trim());
                            }
                            if (ss.length > 1) {
                                values.setValue("Icon", ss[1].trim());
                            }
                        } else {
                            values.setValue("Address", info);
                        }
                    }
                    break;
                case InfoNode.JShellCode:
                    values.setValue("Codes", info);
                    break;
                case InfoNode.SQL:
                    values.setValue("SQL", info);
                    break;
                case InfoNode.JavaScript:
                    values.setValue("Script", info);
                    break;
                case InfoNode.InformationInTree:
                    values.setValue("Info", info);
                    break;
                case InfoNode.JEXLCode:
                    values.setValue("Script", null);
                    values.setValue("Context", null);
                    values.setValue("Parameters", null);
                    if (info != null) {
                        String[] ss = null;
                        if (info.contains(ValueSeparater)) {
                            ss = info.split(ValueSeparater);
                        } else if (info.contains(MoreSeparater)) {
                            ss = info.split(MoreSeparater);
                        }
                        if (ss != null) {
                            if (ss.length > 0) {
                                values.setValue("Script", ss[0].trim());
                            }
                            if (ss.length > 1) {
                                values.setValue("Context", ss[1].trim());
                            }
                            if (ss.length > 2) {
                                values.setValue("Parameters", ss[2].trim());
                            }
                        } else {
                            values.setValue("Script", info);
                        }
                    }
                    break;
                case InfoNode.RowFilter:
                    values.setValue("Script", null);
                    values.setValue("Condition", "true");
                    values.setValue("Maximum", "-1");
                    if (info != null) {
                        if (info.contains(ValueSeparater)) {
                            String[] ss = info.split(ValueSeparater);
                            if (ss.length > 0) {
                                values.setValue("Script", ss[0].trim());
                            }
                            if (ss.length > 1) {
                                values.setValue("Condition", ss[1].trim());
                            }
                            if (ss.length > 2) {
                                values.setValue("Maximum", ss[2].trim());
                            }
                        } else if (info.contains(MoreSeparater)) {
                            String[] ss = info.split(MoreSeparater);
                            values.setValue("Script", ss[0].trim());
                            if (ss.length > 1) {
                                ss = ss[1].split(";;;");
                                if (ss.length > 0) {
                                    values.setValue("Condition", ss[0].trim());
                                }
                                if (ss.length > 1) {
                                    values.setValue("Maximum", ss[1].trim());
                                }
                            }
                        } else {
                            values.setValue("Script", info);
                        }
                    }
                    break;
                case InfoNode.MathFunction:
                    values.setValue("MathFunctionName", null);
                    values.setValue("Variables", null);
                    values.setValue("Expression", null);
                    values.setValue("FunctionDomain", null);
                    if (info != null) {
                        if (info.contains(ValueSeparater)) {
                            String[] ss = info.split(ValueSeparater);
                            if (ss.length > 0) {
                                values.setValue("MathFunctionName", ss[0].trim());
                            }
                            if (ss.length > 1) {
                                values.setValue("Variables", ss[1].trim());
                            }
                            if (ss.length > 2) {
                                values.setValue("Expression", ss[2].trim());
                            }
                            if (ss.length > 3) {
                                values.setValue("FunctionDomain", ss[3].trim());
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
                                    values.setValue("MathFunctionName", names.substring(0, pos));
                                    String vs = names.substring(pos).trim();
                                    if (vs.length() > 0) {
                                        values.setValue("Variables", vs.substring(1));
                                    }
                                } else {
                                    values.setValue("MathFunctionName", names);
                                }
                            }
                            if (info != null && info.contains(MoreSeparater)) {
                                String[] ss = info.split(MoreSeparater);
                                values.setValue("Expression", ss[0].trim());
                                if (ss.length > 1) {
                                    values.setValue("FunctionDomain", ss[1].trim());
                                }
                            } else {
                                values.setValue("Expression", info);
                            }
                        }
                    }
                    break;
                case InfoNode.ImageMaterial:
                    values.setValue("Value", info);
                    break;
                case InfoNode.Data2DDefinition:
                case InfoNode.ImageScope:
                    values.setValue("XML", info);
                    break;
            }
            return values;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
