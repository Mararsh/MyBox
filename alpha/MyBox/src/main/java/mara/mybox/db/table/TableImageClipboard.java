package mara.mybox.db.table;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.fxml.SingletonTask;
import static mara.mybox.fxml.WindowTools.recordError;
import static mara.mybox.fxml.WindowTools.recordInfo;
import mara.mybox.tools.FileDeleteTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-6-7
 * @License Apache License Version 2.0
 */
public class TableImageClipboard extends BaseTable<ImageClipboard> {

    public static final String FileQuery
            = "SELECT * FROM Image_Clipboard  WHERE image_file=? ORDER BY record_time DESC";

    public static final String DeleteFile
            = "DELETE FROM Image_Clipboard  WHERE image_file=?";

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

    public void clearImageClipboards(SingletonTask task, Connection conn, List<String> files) {
        if (conn == null || files == null || files.isEmpty()) {
            return;
        }
        recordInfo(task, FileQuery);
        try (PreparedStatement statement = conn.prepareStatement(FileQuery)) {
            conn.setAutoCommit(true);
            for (String file : files) {
                if (task != null && task.isCancelled()) {
                    return;
                }
                recordInfo(task, message("Check") + ": " + file);
                statement.setString(1, file);
                try (ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        if (task != null && task.isCancelled()) {
                            return;
                        }
                        ImageClipboard data = readData(results);
                        recordInfo(task, message("Delete") + ": " + data.getThumbnailFile());
                        FileDeleteTools.delete(data.getThumbnailFile());
                    }
                } catch (Exception e) {
                    recordError(task, e.toString() + "\n" + tableName);
                }
            }
        } catch (Exception e) {
            recordError(task, e.toString() + "\n" + tableName);
        }
        recordInfo(task, DeleteFile);
        if (task != null && task.isCancelled()) {
            return;
        }
        try (PreparedStatement statement = conn.prepareStatement(DeleteFile)) {
            for (String file : files) {
                if (task != null && task.isCancelled()) {
                    return;
                }
                recordInfo(task, message("Clear") + ": " + file);
                statement.setString(1, file);
                statement.executeUpdate();
            }
        } catch (Exception e) {
            recordError(task, e.toString() + "\n" + tableName);
        }
    }

    public int clearInvalid(SingletonTask task, Connection conn) {
        int count = 0;
        try {
            recordInfo(task, message("Check") + ": " + tableName);
            conn.setAutoCommit(true);
            List<ImageClipboard> invalid = new ArrayList<>();
            List<String> clear = new ArrayList<>();
            try (PreparedStatement query = conn.prepareStatement(queryAllStatement());
                    ResultSet results = query.executeQuery()) {
                while (results.next()) {
                    if (task != null && task.isCancelled()) {
                        return -1;
                    }
                    ImageClipboard data = readData(results);
                    File imageFile = data.getImageFile();
                    if (imageFile == null) {
                        invalid.add(data);
                    } else if (!imageFile.exists()) {
                        clear.add(imageFile.getAbsolutePath());
                        recordInfo(task, message("NotFound") + ": " + imageFile.getAbsolutePath());
                    } else {
                        File thumbnailFile = data.getThumbnailFile();
                        if (thumbnailFile == null || !thumbnailFile.exists()) {
                            invalid.add(data);
                            if (thumbnailFile != null) {
                                recordInfo(task, message("NotFound") + ": " + thumbnailFile.getAbsolutePath());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                recordError(task, e.toString() + "\n" + tableName);
            }
            count = clear.size() + invalid.size();
            if (count > 0) {
                recordInfo(task, message("Invalid") + ": " + clear.size() + " + " + invalid.size());
                clearImageClipboards(task, conn, clear);
                if (task != null && task.isCancelled()) {
                    return -1;
                }
                deleteData(conn, invalid);
            }
            conn.setAutoCommit(true);
        } catch (Exception e) {
            recordError(task, e.toString() + "\n" + tableName);
        }
        return count;
    }

}
