package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.dev.MyBoxLog;

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
        addColumn(new ColumnDefinition("icid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("image_file", ColumnType.File, true).setLength(FilenameMaxLength));
        addColumn(new ColumnDefinition("thumbnail_file", ColumnType.File).setLength(FilenameMaxLength));
        addColumn(new ColumnDefinition("width", ColumnType.Integer));
        addColumn(new ColumnDefinition("height", ColumnType.Integer));
        addColumn(new ColumnDefinition("source", ColumnType.Short));
        addColumn(new ColumnDefinition("create_time", ColumnType.Datetime));
        orderColumns = "create_time DESC";
        return this;
    }

    public int clearInvalid(Connection conn) {
        int count = 0;
        try {
            conn.setAutoCommit(true);
            List<ImageClipboard> invalid = new ArrayList<>();
            try ( PreparedStatement query = conn.prepareStatement(queryAllStatement());
                     ResultSet results = query.executeQuery()) {
                while (results.next()) {
                    ImageClipboard data = readData(results);
                    if (data.getImageFile() == null || !data.getImageFile().exists()) {
                        invalid.add(data);
                    }
                }

            } catch (Exception e) {
                MyBoxLog.debug(e, tableName);
            }
            count = invalid.size();
            deleteData(conn, invalid);
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e, tableName);
        }
        return count;
    }

}
