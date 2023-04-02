package mara.mybox.db.table;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.controller.BaseTaskController;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.ImageEditHistory;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.WindowTools.recordError;
import static mara.mybox.fxml.WindowTools.recordInfo;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-10-15 9:31:28
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableImageEditHistory extends BaseTable<ImageEditHistory> {

    public static final int Default_Max_Histories = 20;

    public static final String FileQuery
            = "SELECT * FROM Image_Edit_History  WHERE image_location=? ORDER BY operation_time DESC";

    public static final String HistoryQuery
            = "SELECT * FROM Image_Edit_History  WHERE history_location=?";

    public static final String DeleteFile
            = "DELETE FROM Image_Edit_History  WHERE image_location=?";

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
        addColumn(new ColumnDefinition("iehid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("image_location", ColumnType.String, true).setLength(FilenameMaxLength));
        addColumn(new ColumnDefinition("history_location", ColumnType.String, true).setLength(FilenameMaxLength));
        addColumn(new ColumnDefinition("operation_time", ColumnType.Datetime, true));
        addColumn(new ColumnDefinition("update_type", ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("object_type", ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("op_type", ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("scope_type", ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("scope_name", ColumnType.String).setLength(StringMaxLength));
        return this;
    }

    public static List<ImageEditHistory> read(String filename) {
        List<ImageEditHistory> records = new ArrayList<>();
        if (filename == null || filename.trim().isEmpty()) {
            return records;
        }
        int max = UserConfig.getInt("MaxImageHistories", Default_Max_Histories);
        if (max <= 0) {
            max = TableImageEditHistory.Default_Max_Histories;
            UserConfig.setInt("MaxImageHistories", Default_Max_Histories);
        }
        try (Connection conn = DerbyBase.getConnection();
                Statement statement = conn.createStatement()) {
            List<ImageEditHistory> invalid = new ArrayList<>();
            String sql = " SELECT * FROM Image_Edit_History WHERE image_location='" + filename + "' ORDER BY operation_time DESC";
            try (ResultSet results = statement.executeQuery(sql)) {
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

                    if (!new File(his.getHistoryLocation()).exists() || records.size() >= max) {
                        invalid.add(his);
                    } else {
                        records.add(his);
                    }
                }
            }
            for (ImageEditHistory h : invalid) {
                deleteRecord(conn, h.getImage(), h.getHistoryLocation());
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
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(sql);
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
        }
        try {
            File hisFile = new File(hisname);
            FileDeleteTools.delete(hisFile);
            File thumbFile = new File(FileNameTools.append(hisname, "_thumbnail"));
            FileDeleteTools.delete(thumbFile);
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
        try (Connection conn = DerbyBase.getConnection()) {
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
        try (Connection conn = DerbyBase.getConnection()) {
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
        try (Connection conn = DerbyBase.getConnection()) {
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
        try (Connection conn = DerbyBase.getConnection()) {
            deleteRecord(conn, image, hisname);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public int clearAll() {
        try (Connection conn = DerbyBase.getConnection();
                Statement statement = conn.createStatement()) {
            String sql = " SELECT history_location FROM Image_Edit_History";
            try (ResultSet results = statement.executeQuery(sql)) {
                while (results.next()) {
                    FileDeleteTools.delete(results.getString("history_location"));
                }
            }
            String imageHistoriesPath = AppPaths.getImageHisPath();
            File path = new File(imageHistoriesPath);
            if (path.exists()) {
                File[] files = path.listFiles();
                if (files != null) {
                    for (File f : files) {
                        FileDeleteTools.delete(f);
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

    public int clearInvalid(BaseTaskController taskController, Connection conn) {
        int count = clearInvalidRows(taskController, conn);
        return count + clearInvalidFiles(taskController, conn);
    }

    public int clearInvalidRows(BaseTaskController taskController, Connection conn) {
        int rowCount = 0, invalidCount = 0;
        try {
            recordInfo(taskController, message("Check") + ": " + tableName);
            try (PreparedStatement query = conn.prepareStatement(queryAllStatement());
                    PreparedStatement delete = conn.prepareStatement(deleteStatement())) {
                conn.setAutoCommit(true);
                try (ResultSet results = query.executeQuery()) {
                    conn.setAutoCommit(false);
                    while (results.next()) {
                        rowCount++;
                        if (taskController != null && taskController.getTask() != null
                                && taskController.getTask().isCancelled()) {
                            return invalidCount;
                        }
                        ImageEditHistory data = readData(results);
                        String image = data.getImage();
                        String his = data.getHistoryLocation();
                        if (his == null || !new File(his).exists() || !exist(image)) {
                            if (FileDeleteTools.delete(his)) {
                                recordInfo(taskController, message("Delete") + ": " + his);
                            }
                            String hisThumb = FileNameTools.append(his, "_thumbnail");
                            if (FileDeleteTools.delete(new File(hisThumb))) {
                                recordInfo(taskController, message("Delete") + ": " + hisThumb);
                            }
                            if (setDeleteStatement(conn, delete, data)) {
                                delete.addBatch();
                                if (invalidCount > 0 && (invalidCount % Database.BatchSize == 0)) {
                                    int[] res = delete.executeBatch();
                                    for (int r : res) {
                                        if (r > 0) {
                                            invalidCount += r;
                                        }
                                    }
                                    conn.commit();
                                    delete.clearBatch();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    recordError(taskController, e.toString() + "\n" + tableName);
                }
                int[] res = delete.executeBatch();
                for (int r : res) {
                    if (r > 0) {
                        invalidCount += r;
                    }
                }
                conn.commit();
            } catch (Exception e) {
                recordError(taskController, e.toString() + "\n" + tableName);
            }
            conn.setAutoCommit(true);
        } catch (Exception e) {
            recordError(taskController, e.toString() + "\n" + tableName);
        }
        recordInfo(taskController, message("Checked") + ": " + rowCount + " "
                + message("Expired") + ": " + invalidCount);
        return invalidCount;
    }

    public int clearInvalidFiles(BaseTaskController taskController, Connection conn) {
        int rowCount = 0, invalidCount = 0;
        try {
            String ihRootpath = AppPaths.getImageHisPath();
            recordInfo(taskController, message("Check") + ": " + ihRootpath);
            String[] ihPaths = new File(ihRootpath).list();
            if (ihPaths == null || ihPaths.length == 0) {
                return invalidCount;
            }
            try (PreparedStatement query = conn.prepareStatement(HistoryQuery)) {
                conn.setAutoCommit(true);
                for (String pathname : ihPaths) {
                    if (taskController != null && taskController.getTask() != null
                            && taskController.getTask().isCancelled()) {
                        return invalidCount;
                    }
                    String path = ihRootpath + File.separator + pathname;
                    String[] names = new File(path).list();
                    if (names == null || names.length == 0) {
                        try {
                            new File(path).delete();
                            recordInfo(taskController, message("Delete") + ": " + path);
                        } catch (Exception ex) {
                        }
                        continue;
                    }
                    for (String name : names) {
                        rowCount++;
                        if (taskController != null && taskController.getTask() != null
                                && taskController.getTask().isCancelled()) {
                            return invalidCount;
                        }
                        String file = path + File.separator + name;
                        query.setString(1, file);
                        try (ResultSet results = query.executeQuery()) {
                            if (!results.next()) {
                                invalidCount++;
                                if (FileDeleteTools.delete(file)) {
                                    recordInfo(taskController, message("Delete") + ": " + file);
                                }
                            }
                        } catch (Exception e) {
                            recordError(taskController, e.toString() + "\n" + file);
                        }
                    }
                    names = new File(path).list();
                    if (names == null || names.length == 0) {
                        try {
                            new File(path).delete();
                            recordInfo(taskController, message("Delete") + ": " + path);
                        } catch (Exception ex) {
                        }
                    }
                }
            } catch (Exception ex) {
                recordError(taskController, ex.toString() + "\n" + tableName);
            }
        } catch (Exception exx) {
            recordError(taskController, exx.toString() + "\n" + tableName);
        }
        recordInfo(taskController, message("Checked") + ": " + rowCount + " "
                + message("Expired") + ": " + invalidCount);
        return invalidCount;
    }

    public boolean exist(String imagename) {
        if (imagename == null) {
            return false;
        }
        String fname = imagename;
        int pos = fname.lastIndexOf(".");
        if (pos > 0) {
            String suffix = fname.substring(pos);
            int pos2 = suffix.lastIndexOf("-frame");
            if (pos2 > 0) {
                fname = fname.substring(0, pos) + suffix.substring(0, pos2);
            }
        }
        return new File(fname).exists();
    }

}
