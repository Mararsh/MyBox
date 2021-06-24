package mara.mybox.db.table;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ImageEditHistory;
import mara.mybox.db.table.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.image.ImageScope;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2018-10-15 9:31:28
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableImageEditHistory extends BaseTable<ImageEditHistory> {

    public static final int Default_Max_Histories = 20;

    public TableImageEditHistory() {
        tableName = "Image_Edit_History";
        defineColumns();
    }

    public TableImageEditHistory(boolean defineColumns) {
        tableName = "Image_Edit_History";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableImageEditHistory defineColumns() {
        addColumn(new ColumnDefinition("iehid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("image_location", ColumnType.String, true).setLength(FilenameMaxLength));
        addColumn(new ColumnDefinition("history_location", ColumnType.String, true).setLength(FilenameMaxLength));
        addColumn(new ColumnDefinition("operation_time", ColumnType.Datetime, true));
        addColumn(new ColumnDefinition("update_type", ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("object_type", ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("op_type", ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("scope_type", ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("scope_name", ColumnType.String).setLength(4096));
        return this;
    }

    public static List<ImageEditHistory> read(String filename) {
        List<ImageEditHistory> records = new ArrayList<>();
        if (filename == null || filename.trim().isEmpty()) {
            return records;
        }
        int max = AppVariables.getUserConfigInt("MaxImageHistories", Default_Max_Histories);
        if (max <= 0) {
            max = TableImageEditHistory.Default_Max_Histories;
            AppVariables.setUserConfigInt("MaxImageHistories", Default_Max_Histories);
        }
        try ( Connection conn = DerbyBase.getConnection();
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM Image_Edit_History WHERE image_location='" + filename + "' ORDER BY operation_time DESC";
            try ( ResultSet results = statement.executeQuery(sql)) {
                while (results.next()) {
                    ImageEditHistory his = new ImageEditHistory();
                    his.setImage(filename);
                    his.setHistoryLocation(results.getString("history_location"));
                    his.setUpdateType(results.getString("update_type"));
                    his.setObjectType(results.getString("object_type"));
                    his.setOpType(results.getString("op_type"));
                    his.setScopeType(results.getString("scope_type"));
                    his.setScopeName(results.getString("scope_name"));
                    his.setOperationTime(results.getTimestamp("operation_time"));
                    records.add(his);
                }
            }
            List<ImageEditHistory> valid = new ArrayList<>();
            for (int i = 0; i < records.size(); ++i) {
                String hisname = records.get(i).getHistoryLocation();
                File hisFile = new File(hisname);
                if (!hisFile.exists()) {
                    deleteRecord(conn, filename, hisname);
                    continue;
                }
                valid.add(records.get(i));
            }
            if (valid.size() > max) {
                for (int i = max; i < valid.size(); ++i) {
                    deleteRecord(conn, filename, valid.get(i).getHistoryLocation());
                }
                records = valid.subList(0, max);
            } else {
                records = valid;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return records;
    }

    public static void deleteRecord(Connection conn, String image, String hisname) {
        if (conn == null || image == null || hisname == null) {
            return;
        }
        String sql = "DELETE FROM Image_Edit_History WHERE image_location='" + image
                + "' AND history_location='" + hisname + "'";
        try ( Statement statement = conn.createStatement()) {
            statement.executeUpdate(sql);
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
        }
        try {
            File hisFile = new File(hisname);
            FileTools.delete(hisFile);
            File thumbFile = new File(FileTools.appendName(hisname, "_thumbnail"));
            FileTools.delete(thumbFile);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public static List<ImageEditHistory> add(String image, String his_location, ImageScope scope) {
        return add(image, his_location, null, null, null, scope);
    }

    public static List<ImageEditHistory> add(String image, String his_location,
            String update_type, String object_type, String op_type, ImageScope scope) {
        if (image == null || image.trim().isEmpty()
                || his_location == null || his_location.trim().isEmpty()) {
            return read(image);
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            String fields = "image_location, history_location ,operation_time ";
            String values = " '" + image + "', '" + his_location + "', '" + DateTools.datetimeToString(new Date()) + "' ";
            if (update_type != null) {
                fields += ", update_type";
                values += ", '" + update_type + "' ";
            }
            if (object_type != null) {
                fields += ", object_type";
                values += ", '" + object_type + "' ";
            }
            if (op_type != null) {
                fields += ", op_type";
                values += ", '" + op_type + "' ";
            }
            if (scope != null) {
                if (scope.getScopeType() != null) {
                    fields += ", scope_type";
                    values += ", '" + scope.getScopeType().name() + "' ";
                }
                if (scope.getName() != null) {
                    fields += ", scope_name";
                    values += ", '" + scope.getName() + "' ";
                }
            }
            String sql = "INSERT INTO Image_Edit_History(" + fields + ") VALUES(" + values + ")";
//            MyBoxLog.debug(sql);
            conn.createStatement().executeUpdate(sql);
            return read(image);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return read(image);
        }
    }

    public static boolean add(ImageEditHistory his) {
        if (his == null || his.getImage() == null) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            String fields = "image_location, history_location ,operation_time ";
            String values = " '" + his.getImage() + "', '" + his.getHistoryLocation()
                    + "', '" + DateTools.datetimeToString(his.getOperationTime()) + "' ";
            if (his.getUpdateType() != null) {
                fields += ", update_type";
                values += ", '" + his.getUpdateType() + "' ";
            }
            if (his.getObjectType() != null) {
                fields += ", object_type";
                values += ", '" + his.getObjectType() + "' ";
            }
            if (his.getOpType() != null) {
                fields += ", op_type";
                values += ", '" + his.getOpType() + "' ";
            }
            if (his.getScopeType() != null) {
                fields += ", scope_type";
                values += ", '" + his.getScopeType() + "' ";
            }
            if (his.getScopeName() != null) {
                fields += ", scope_name";
                values += ", '" + his.getScopeName() + "' ";
            }
            String sql = "INSERT INTO Image_Edit_History(" + fields + ") VALUES(" + values + ")";
//            MyBoxLog.debug(sql);
            conn.createStatement().executeUpdate(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean clearImage(String image) {
        if (image == null) {
            return true;
        }
        List<ImageEditHistory> records = read(image);
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setAutoCommit(false);
            for (int i = 0; i < records.size(); ++i) {
                deleteRecord(conn, image, records.get(i).getHistoryLocation());
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean deleteHistory(String image, String hisname) {
        try ( Connection conn = DerbyBase.getConnection()) {
            deleteRecord(conn, image, hisname);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public int clear() {
        try ( Connection conn = DerbyBase.getConnection();
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT history_location FROM Image_Edit_History";
            try ( ResultSet results = statement.executeQuery(sql)) {
                while (results.next()) {
                    FileTools.delete(results.getString("history_location"));
                }
            }
            String imageHistoriesPath = AppVariables.getImageHisPath();
            File path = new File(imageHistoriesPath);
            if (path.exists()) {
                File[] files = path.listFiles();
                if (files != null) {
                    for (File f : files) {
                        FileTools.delete(f);
                    }
                }
            }
            sql = "DELETE FROM Image_Edit_History";
            return statement.executeUpdate(sql);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

}
