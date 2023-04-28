package mara.mybox.db.table;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import mara.mybox.controller.BaseTaskController;
import mara.mybox.db.Database;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.ImageClipboard;
import static mara.mybox.fxml.WindowTools.recordError;
import static mara.mybox.fxml.WindowTools.recordInfo;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-6-7
 * @License Apache License Version 2.0
 */
public class TableImageClipboard extends BaseTable<ImageClipboard> {

    public TableImageClipboard() {
        tableName = "Image_Clipboard";
        defineColumns();
    }

    public TableImageClipboard(boolean defineColumns) {
        tableName = "Image_Clipboard";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableImageClipboard defineColumns() {
        addColumn(new ColumnDefinition("icid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("image_file", ColumnType.File, true).setLength(FilenameMaxLength));
        addColumn(new ColumnDefinition("thumbnail_file", ColumnType.File).setLength(FilenameMaxLength));
        addColumn(new ColumnDefinition("width", ColumnType.Integer));
        addColumn(new ColumnDefinition("height", ColumnType.Integer));
        addColumn(new ColumnDefinition("source", ColumnType.Short));
        addColumn(new ColumnDefinition("create_time", ColumnType.Datetime));
        orderColumns = "create_time DESC";
        return this;
    }

    public static final String FileQuery
            = "SELECT * FROM Image_Clipboard  WHERE image_file=? OR thumbnail_file=?";

    public static final String DeleteFile
            = "DELETE FROM Image_Clipboard  WHERE image_file=?";

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
                        ImageClipboard data = readData(results);
                        File imageFile = data.getImageFile();
                        File thumbnailFile = data.getThumbnailFile();
                        if (imageFile == null || !imageFile.exists()
                                || thumbnailFile == null || !thumbnailFile.exists()) {
                            if (imageFile != null) {
                                FileDeleteTools.delete(imageFile);
                                recordInfo(taskController, message("Delete") + ": " + imageFile);
                            }
                            if (thumbnailFile != null) {
                                FileDeleteTools.delete(thumbnailFile);
                                recordInfo(taskController, message("Delete") + ": " + thumbnailFile);
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
            String icpath = AppPaths.getImageClipboardPath();
            recordInfo(taskController, message("Check") + ": " + icpath);
            String[] files = new File(icpath).list();
            if (files == null || files.length == 0) {
                return invalidCount;
            }
            try (PreparedStatement query = conn.prepareStatement(FileQuery)) {
                conn.setAutoCommit(true);
                for (String name : files) {
                    rowCount++;
                    if (taskController != null && taskController.getTask() != null
                            && taskController.getTask().isCancelled()) {
                        return invalidCount;
                    }
                    String fname = icpath + File.separator + name;
                    query.setString(1, fname);
                    query.setString(2, fname);
                    try (ResultSet results = query.executeQuery()) {
                        if (!results.next()) {
                            invalidCount++;
                            FileDeleteTools.delete(fname);
                            recordInfo(taskController, message("Delete") + ": " + fname);
                        }
                    } catch (Exception e) {
                        recordError(taskController, e.toString() + "\n" + name);
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

}
