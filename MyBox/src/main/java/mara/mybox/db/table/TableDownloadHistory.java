package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import mara.mybox.data.DownloadHistory;
import mara.mybox.db.DerbyBase;
import static mara.mybox.db.DerbyBase.dbHome;
import mara.mybox.db.table.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.HtmlTools;

/**
 * @Author Mara
 * @CreateDate 2020-10-15
 * @License Apache License Version 2.0
 */
public class TableDownloadHistory extends BaseTable<DownloadHistory> {

    public TableDownloadHistory() {
        tableName = "Download_History";
        defineColumns();
    }

    public TableDownloadHistory(boolean defineColumns) {
        tableName = "Download_History";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableDownloadHistory defineColumns() {
        addColumn(new ColumnDefinition("dhid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("url", ColumnType.String, true).setLength(4096));
        addColumn(new ColumnDefinition("title", ColumnType.String).setLength(4096));
        addColumn(new ColumnDefinition("name", ColumnType.String).setLength(4096));
        addColumn(new ColumnDefinition("index", ColumnType.Integer));
        addColumn(new ColumnDefinition("filename", ColumnType.String).setLength(4096));
        addColumn(new ColumnDefinition("download_time", ColumnType.Datetime, true));
        return this;
    }

    public static final String Create_Index_url
            = "CREATE INDEX Download_History_url_index on Download_History ( url , download_time DESC)";

    public static final String Create_Index_filename
            = "CREATE INDEX Download_History_filename_index on Download_History ( filename , download_time DESC )";

    public static final String IndexQeury
            = "SELECT * FROM Download_History WHERE url=? ORDER BY filename, index ASC, download_time DESC";

    public static final String UrlQeury
            = "SELECT * FROM Download_History WHERE url=? ORDER BY download_time DESC";

    public static final String FilenameQeury
            = "SELECT * FROM Download_History WHERE filename=? ORDER BY download_time DESC";

    public static final String PathQeury
            = "SELECT * FROM Download_History WHERE filename like ? ORDER BY filename, index ASC, download_time DESC";

    public static final String DeleteUrlLike
            = "DELETE FROM Download_History WHERE url like ?";

    public int deleteAddressHistory(String address) {
        if (address == null) {
            return 0;
        }
        String addressPath = HtmlTools.fullPath(address);
        try ( Connection conn = DriverManager.getConnection(DerbyBase.protocol + dbHome() + DerbyBase.login);
                 PreparedStatement delete = conn.prepareStatement(DeleteUrlLike)) {
            delete.setString(1, "%" + addressPath);
            return delete.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            MyBoxLog.debug(address + " -- > " + addressPath);
            return 0;
        }
    }

}
