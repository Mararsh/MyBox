package mara.mybox.db.migration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import mara.mybox.controller.MyBoxLoadingController;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.data2d.TmpTable;
import mara.mybox.data2d.tools.Data2DDefinitionTools;
import mara.mybox.db.Database;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.DataNodeTag;
import mara.mybox.db.data.DataTag;
import mara.mybox.db.table.BaseNodeTable;
import static mara.mybox.db.table.BaseNodeTable.RootID;
import static mara.mybox.db.table.BaseTable.StringMaxLength;
import mara.mybox.db.table.TableData2DDefinition;
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
import mara.mybox.value.AppVariables;
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
            if (lastVersion < 6008001) {
                updateIn681(conn);
            }
            if (lastVersion < 6008002) {
                updateIn682(controller, conn);
            }
            if (lastVersion < 6008003) {
                updateIn683(controller, conn, lang);
            }
            if (lastVersion < 6008005) {
                updateIn685(controller, conn, lang);
            }
            if (lastVersion < 6008006) {
                updateIn686(controller, conn, lang);
            }
            if (lastVersion < 6008008) {
                updateIn688(controller, conn, lang);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn688(MyBoxLoadingController controller,
            Connection conn, String lang) {
        MyBoxLog.info("Updating tables in 6.8.8...");
        controller.info("Updating tables in 6.8.8...");
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate("ALTER TABLE Data2D_Column ADD COLUMN label VARCHAR(" + StringMaxLength + ")");
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    public static void updateIn686(MyBoxLoadingController controller,
            Connection conn, String lang) {
        MyBoxLog.info("Updating tables in 6.8.6...");
        controller.info("Updating tables in 6.8.6...");
        try {
            conn.setAutoCommit(false);
            try (Statement statement = conn.createStatement();
                    ResultSet defResult = statement.executeQuery("select * from Data2D_Definition WHERE data_type=4");
                    PreparedStatement rowQuery = conn.prepareStatement("select * from Data2D_Cell WHERE dcdid=? AND row=?")) {
                TableData2DDefinition tableData2DDefinition = new TableData2DDefinition();
                double value;
                String line;
                ResultSet rowResult;
                double[] row;
                Charset charset = Charset.forName("UTF-8");
                while (defResult.next()) {
                    try {
                        Data2DDefinition def = tableData2DDefinition.readData(defResult);
                        long dataid = def.getDataID();
                        long colsNumber = def.getColsNumber();
                        long rowsNumber = def.getRowsNumber();
                        String dataname = dataid + "";
                        File file = DataMatrix.file(dataname);
                        rowQuery.setLong(1, dataid);
                        controller.info("Moving matrix:" + file);
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, charset, false))) {
                            for (int rowID = 0; rowID < rowsNumber; rowID++) {
                                try {
                                    rowQuery.setLong(2, rowID);
                                    row = new double[(int) colsNumber];
                                    rowResult = rowQuery.executeQuery();
                                    while (rowResult.next()) {
                                        try {
                                            value = Double.parseDouble(rowResult.getString("value").replaceAll(",", ""));
                                            row[(int) rowResult.getLong("col")] = value;
                                        } catch (Exception exx) {
                                        }
                                    }
                                    line = row[0] + "";
                                    for (int c = 1; c < row.length; c++) {
                                        line += DataMatrix.MatrixDelimiter + row[c];
                                    }
                                    writer.write(line + "\n");
                                } catch (Exception exx) {
                                }
                            }
                            writer.flush();
                        } catch (Exception ex) {
                        }
                        def.setFile(file).setSheet("Double")
                                .setDataName(dataname)
                                .setCharset(charset).setHasHeader(false)
                                .setDelimiter(DataMatrix.MatrixDelimiter);
                        tableData2DDefinition.updateData(conn, def);
                        conn.commit();
                    } catch (Exception e) {
                    }
                }
                defResult.close();

            } catch (Exception e) {
                MyBoxLog.console(e);
            }

            conn.setAutoCommit(true);

            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("ALTER TABLE  Data2D_Cell DROP CONSTRAINT  Data2D_Cell_fk");
                statement.executeUpdate("DROP TABLE Data2D_Cell");
            } catch (Exception e) {
                MyBoxLog.console(e);
            }

            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("ALTER TABLE Node_Image_Scope ADD COLUMN color_threshold_tmp  DOUBLE");
                statement.executeUpdate("UPDATE Node_Image_Scope SET color_threshold_tmp=color_threshold");
                statement.executeUpdate("ALTER TABLE Node_Image_Scope DROP COLUMN color_threshold");
                statement.executeUpdate("ALTER TABLE Node_Image_Scope ADD COLUMN color_threshold  DOUBLE");
                statement.executeUpdate("UPDATE Node_Image_Scope SET color_threshold=color_threshold_tmp");
                statement.executeUpdate("ALTER TABLE Node_Image_Scope DROP COLUMN color_threshold_tmp");
            } catch (Exception e) {
                MyBoxLog.console(e);
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }

    }

    public static void updateIn685(MyBoxLoadingController controller,
            Connection conn, String lang) {
        try (Statement exeStatement = conn.createStatement()) {
            MyBoxLog.info("Updating tables in 6.8.5...");
            controller.info("Updating tables in 6.8.5...");

            exeStatement.executeUpdate("ALTER TABLE Node_Image_Scope ADD COLUMN shape_type VARCHAR(128)");
            exeStatement.executeUpdate("ALTER TABLE Node_Image_Scope ADD COLUMN color_algorithm VARCHAR(128)");
            exeStatement.executeUpdate("ALTER TABLE Node_Image_Scope ADD COLUMN shape_excluded  BOOLEAN");
            exeStatement.executeUpdate("ALTER TABLE Node_Image_Scope ADD COLUMN color_threshold  BIGINT");
            exeStatement.executeUpdate("ALTER TABLE Node_Image_Scope ADD COLUMN color_weights  VARCHAR(" + StringMaxLength + ")");
            exeStatement.executeUpdate("ALTER TABLE Node_Image_Scope ADD COLUMN shape_data  CLOB");

            exeStatement.executeUpdate("UPDATE Node_Image_Scope SET shape_type=scope_type");
            exeStatement.executeUpdate("UPDATE Node_Image_Scope SET shape_type='Matting8' WHERE scope_type='Matting'");
            exeStatement.executeUpdate("UPDATE Node_Image_Scope SET shape_type='Whole' WHERE scope_type='COLORS'");
            exeStatement.executeUpdate("UPDATE Node_Image_Scope SET color_algorithm=color_type");
            exeStatement.executeUpdate("UPDATE Node_Image_Scope SET color_algorithm='RGBRoughWeightedEuclidean' "
                    + " WHERE color_type='AllColor' OR color_type='Color' ");
            exeStatement.executeUpdate("UPDATE Node_Image_Scope SET shape_excluded=area_excluded");
            exeStatement.executeUpdate("UPDATE Node_Image_Scope SET color_threshold=color_distance");
            exeStatement.executeUpdate("UPDATE Node_Image_Scope SET shape_data=area_data");

            exeStatement.executeUpdate("ALTER TABLE Node_Image_Scope DROP COLUMN scope_type");
            exeStatement.executeUpdate("ALTER TABLE Node_Image_Scope DROP COLUMN color_type");
            exeStatement.executeUpdate("ALTER TABLE Node_Image_Scope DROP COLUMN color_distance");
            exeStatement.executeUpdate("ALTER TABLE Node_Image_Scope DROP COLUMN area_excluded");
            exeStatement.executeUpdate("ALTER TABLE Node_Image_Scope DROP COLUMN area_data");

            File dir = new File(AppVariables.MyboxDataPath + File.separator + "buttons");
            File[] list = dir.listFiles();
            if (list != null) {
                for (File file : list) {
                    if (file.isDirectory()) {
                        continue;
                    }
                    file.delete();
                }
            }

        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    public static void updateIn683(MyBoxLoadingController controller,
            Connection conn, String lang) {
        try {
            MyBoxLog.info("Updating tables in 6.8.3...");
            controller.info("Updating tables in 6.8.3...");

            try (Statement statement = conn.createStatement()) {
                String sql = "ALTER TABLE Data2D_Column DROP COLUMN invalid_as";
                statement.executeUpdate(sql);
            } catch (Exception e) {
                MyBoxLog.console(e);
            }

            TableNodeGeographyCode gcTable = new TableNodeGeographyCode();
            String tname = gcTable.getTableName();
            controller.info("Moving data: " + gcTable.getTreeName());

            // for debug.Remove this block later
//            try (Statement statement = conn.createStatement()) {
//                conn.setAutoCommit(true);
//                statement.executeUpdate("DROP TABLE " + tname + "_Node_Tag");
//                statement.executeUpdate("DROP TABLE " + tname + "_Tag");
//            } catch (Exception e) {
//                MyBoxLog.console(e);
//            }
//            try (Statement statement = conn.createStatement()) {
//                statement.executeUpdate("DROP TABLE " + tname);
//            } catch (Exception e) {
            ////                MyBoxLog.console(e);
//            }
//            try {
//                gcTable.createTable(conn);
//            } catch (Exception e) {
////                MyBoxLog.console(e);
//            }

            String tmpTreeTable = TmpTable.TmpTablePrefix + "TREE_Migration683";
            try (Statement statement = conn.createStatement()) {
                conn.setAutoCommit(true);
                statement.executeUpdate("DROP TABLE " + tmpTreeTable);
            } catch (Exception e) {
//            MyBoxLog.console(e);
            }

            try (Statement exeStatement = conn.createStatement();
                    PreparedStatement idQuery = conn.prepareStatement(
                            "SELECT chinese_name,english_name FROM Geography_Code WHERE gcid=? FETCH FIRST ROW ONLY")) {
                idQuery.setMaxRows(1);

                conn.setAutoCommit(true);
                exeStatement.executeUpdate("CREATE TABLE " + tmpTreeTable
                        + " ( old_nodeid BIGINT, old_parentid BIGINT, new_nodeid BIGINT)");

                ResultSet result = conn.createStatement().executeQuery(
                        "SELECT * FROM Geography_Code ORDER BY gcid");
                conn.setAutoCommit(false);
                long count = 0;
                float orderNum = 0;
                DataNode node;
                String name;
                boolean isChinese = Languages.isChinese(lang);
                while (result.next()) {
                    try {
                        long nodeid = result.getLong("gcid");
                        long parentid = result.getLong("owner");
                        String chinese_name = result.getString("chinese_name");
                        String english_name = result.getString("english_name");
                        if (isChinese) {
                            name = chinese_name != null ? chinese_name : english_name;
                        } else {
                            name = english_name != null ? english_name : chinese_name;
                        }
                        node = DataNode.create()
                                .setNodeid(-1).setParentid(RootID)
                                .setOrderNumber(++orderNum)
                                .setTitle(name);
                        node.setValue("level", result.getShort("level") - 1);
                        node.setValue("coordinate_system", result.getShort("coordinate_system"));
                        node.setValue("longitude", result.getDouble("longitude"));
                        node.setValue("latitude", result.getDouble("latitude"));
                        node.setValue("altitude", result.getDouble("altitude"));
                        node.setValue("precision", result.getDouble("precision"));
                        node.setValue("chinese_name", chinese_name);
                        node.setValue("english_name", english_name);
                        node.setValue("code1", result.getString("code1"));
                        node.setValue("code1", result.getString("code1"));
                        node.setValue("code1", result.getString("code1"));
                        node.setValue("code2", result.getString("code2"));
                        node.setValue("code3", result.getString("code3"));
                        node.setValue("code4", result.getString("code4"));
                        node.setValue("code5", result.getString("code5"));
                        node.setValue("alias1", result.getString("alias1"));
                        node.setValue("alias2", result.getString("alias2"));
                        node.setValue("alias3", result.getString("alias3"));
                        node.setValue("alias4", result.getString("alias4"));
                        node.setValue("alias5", result.getString("alias5"));
                        node.setValue("area", result.getLong("area") + 0d);
                        node.setValue("population", result.getLong("population"));
                        node.setValue("description", result.getString("comments"));
                        node.setValue("continent", updateIn683_queryName(idQuery, result.getLong("continent"), isChinese));
                        node.setValue("country", updateIn683_queryName(idQuery, result.getLong("country"), isChinese));
                        node.setValue("province", updateIn683_queryName(idQuery, result.getLong("province"), isChinese));
                        node.setValue("city", updateIn683_queryName(idQuery, result.getLong("city"), isChinese));
                        node.setValue("county", updateIn683_queryName(idQuery, result.getLong("county"), isChinese));
                        node.setValue("town", updateIn683_queryName(idQuery, result.getLong("town"), isChinese));
                        node.setValue("village", updateIn683_queryName(idQuery, result.getLong("village"), isChinese));
                        node.setValue("building", updateIn683_queryName(idQuery, result.getLong("building"), isChinese));

                        node = gcTable.insertData(conn, node);
                        long newNodeid = node.getNodeid();
                        if (newNodeid >= 0) {
                            if (++count % Database.BatchSize == 0) {
                                conn.commit();
                            }
                            exeStatement.executeUpdate("INSERT INTO " + tmpTreeTable + " VALUES ("
                                    + nodeid + ", " + parentid + ", " + newNodeid + ")");
                        }
                    } catch (Exception e) {
                        MyBoxLog.console(e);
                    }
                }
                conn.commit();
                controller.info("Saved: " + gcTable.getTreeName() + " count:" + count);

                result = conn.createStatement().executeQuery("select A.new_nodeid AS nodeid,"
                        + " B.new_nodeid AS parentid "
                        + " from " + tmpTreeTable + " A, " + tmpTreeTable + " AS B "
                        + " WHERE A.old_parentid=B.old_nodeid");
                conn.setAutoCommit(false);
                count = 0;
                while (result.next()) {
                    try {
                        long nodeid = result.getLong("nodeid");
                        long parentid = result.getLong("parentid");
                        if (nodeid == parentid) {
                            parentid = RootID;
                        }
                        String sql = "UPDATE " + tname + " SET parentid=" + parentid + " WHERE nodeid=" + nodeid;
                        exeStatement.executeUpdate(sql);
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

            try (Statement statement = conn.createStatement()) {
                conn.setAutoCommit(true);
                statement.executeUpdate("DROP TABLE " + tmpTreeTable);
                statement.executeUpdate("DROP TABLE Geography_Code");
            } catch (Exception e) {
                MyBoxLog.console(e);
            }
        } catch (Exception e) {
//            MyBoxLog.console(e);
        }
    }

    public static String updateIn683_queryName(PreparedStatement idQuery,
            long id, boolean isChinese) {
        try {
            if (idQuery == null || id < 0) {
                return null;
            }
            idQuery.setLong(1, id);
            ResultSet idresults = idQuery.executeQuery();
            if (idresults == null || !idresults.next()) {
                return null;
            }
            String chinese_name = idresults.getString("chinese_name");
            String english_name = idresults.getString("english_name");
            if (isChinese) {
                return chinese_name != null ? chinese_name : english_name;
            } else {
                return english_name != null ? english_name : chinese_name;
            }
        } catch (Exception e) {
            return null;
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

        String tmpTreeTable = TmpTable.TmpTablePrefix + "TREE_Migration682";
        String tmpTagTable = TmpTable.TmpTablePrefix + "TAG_Migration682";
        try (Statement statement = conn.createStatement()) {
            conn.setAutoCommit(true);
            statement.executeUpdate("DROP TABLE " + tmpTreeTable);
            statement.executeUpdate("DROP TABLE " + tmpTagTable);
        } catch (Exception e) {
//            MyBoxLog.console(e);
        }

        try (Statement statement = conn.createStatement()) {
            conn.setAutoCommit(true);

            statement.executeUpdate("CREATE TABLE " + tmpTreeTable
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
                        statement.executeUpdate("INSERT INTO " + tmpTreeTable + " VALUES ("
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
                    + " from " + tmpTreeTable + " A, " + tmpTreeTable + " AS B "
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

            statement.executeUpdate("CREATE TABLE " + tmpTagTable
                    + " ( old_tagid BIGINT, new_tagid BIGINT)");
            query = conn.createStatement().executeQuery(
                    "select * from tag where category='" + category + "' ORDER BY tgid ");
            conn.setAutoCommit(false);
            count = 0;
            TableDataTag tableTreeTag = new TableDataTag(dataTable);
            while (query.next()) {
                try {
                    DataTag tag = new DataTag()
                            .setTag(query.getString("tag"))
                            .setColorString(query.getString("color"));
                    tag = tableTreeTag.insertData(conn, tag);
                    statement.executeUpdate("INSERT INTO " + tmpTagTable + " VALUES ("
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
                    + " from tree_node_tag A, " + tmpTagTable + " AS B, " + tmpTreeTable + " AS C"
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

        try (Statement statement = conn.createStatement()) {
            conn.setAutoCommit(true);
            statement.executeUpdate("DROP TABLE " + tmpTreeTable);
            statement.executeUpdate("DROP TABLE " + tmpTagTable);
        } catch (Exception e) {
//            MyBoxLog.console(e);
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
                    OldImageScope scope = OldImageScopeTools.fromXML(null, null, info);
                    return OldImageScopeTools.toDataNode(node, scope);
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

    // new comlun type was inserted at index 4 which may cause types migrated from 6.8 to 6.8.1 messed.
    public static void updateIn681(Connection conn) {
        try (Statement statement = conn.createStatement()) {
            MyBoxLog.info("Updating tables in 6.8.1...");

            conn.setAutoCommit(true);

            for (int i = 18; i > 3; i--) {
                String sql = "UPDATE Data2D_Column SET column_type=" + (i + 1) + " WHERE column_type=" + i;
                statement.executeUpdate(sql);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
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
                OldImageScope scope = new OldImageScope();
                try {
                    OldImageScope.ScopeType type = OldImageScopeTools.scopeType(query.getString("scope_type"));
                    if (OldImageScopeTools.decodeAreaData(type, query.getString("area_data"), scope)
                            && OldImageScopeTools.decodeColorData(type, query.getString("color_data"), scope)
                            && OldImageScopeTools.decodeOutline(null, type, query.getString("outline"), scope)) {
                        scope.setFile(query.getString("image_location"));
                        scope.setName(query.getString("name"));
                        scope.setScopeType(type);
                        scope.setColorScopeType(OldImageScope.ColorScopeType.valueOf(query.getString("color_scope_type")));
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
                        .setInfo(OldImageScopeTools.toXML(scope, ""))
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
