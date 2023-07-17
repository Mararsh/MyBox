package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.data.MediaInformation;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import static mara.mybox.db.table.BaseTable.StringMaxLength;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2019-12-8
 * @License Apache License Version 2.0
 */
public class TableMedia extends BaseTable<MediaInformation> {

    public TableMedia() {
        tableName = "media";
        defineColumns();
    }

    public TableMedia(boolean defineColumns) {
        tableName = "media";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableMedia defineColumns() {
        addColumn(new ColumnDefinition("address", ColumnDefinition.ColumnType.String, true, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("video_encoding", ColumnDefinition.ColumnType.String).setLength(1024));
        addColumn(new ColumnDefinition("audio_encoding", ColumnDefinition.ColumnType.String).setLength(1024));
        addColumn(new ColumnDefinition("duration", ColumnDefinition.ColumnType.Long));
        addColumn(new ColumnDefinition("size", ColumnDefinition.ColumnType.Long));
        addColumn(new ColumnDefinition("width", ColumnDefinition.ColumnType.Integer));
        addColumn(new ColumnDefinition("height", ColumnDefinition.ColumnType.Integer));
        addColumn(new ColumnDefinition("info", ColumnDefinition.ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("html", ColumnDefinition.ColumnType.Clob));
        addColumn(new ColumnDefinition("modify_time", ColumnDefinition.ColumnType.Datetime, true));
        return this;
    }

    public static MediaInformation read(String address) {
        if (address == null || address.trim().isEmpty()) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection();
                 Statement statement = conn.createStatement()) {
            conn.setReadOnly(true);
            statement.setMaxRows(1);
            String sql = " SELECT * FROM media WHERE address='" + DerbyBase.stringValue(address) + "'";
            try ( ResultSet results = statement.executeQuery(sql)) {
                if (results.next()) {
                    MediaInformation media = new MediaInformation(address);
                    media.setVideoEncoding(results.getString("video_encoding"));
                    media.setAudioEncoding(results.getString("audio_encoding"));
                    media.setFileSize(results.getLong("size"));
                    media.setDuration(results.getLong("duration"));
                    media.setWidth(results.getInt("width"));
                    media.setHeight(results.getInt("height"));
                    media.setInfo(results.getString("info"));
                    media.setHtml(results.getString("html"));
                    return media;
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);

        }
        return null;
    }

    public static List<MediaInformation> read(List<String> addresses) {
        List<MediaInformation> medias = new ArrayList();
        if (addresses == null || addresses.isEmpty()) {
            return medias;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            medias = read(conn, addresses);
        } catch (Exception e) {
            MyBoxLog.error(e);

        }
        return medias;
    }

    public static List<MediaInformation> read(Connection conn, List<String> addresses) {
        List<MediaInformation> medias = new ArrayList();
        if (conn == null || addresses == null || addresses.isEmpty()) {
            return medias;
        }
        try ( Statement statement = conn.createStatement()) {
            String inStr = "( '" + addresses.get(0) + "'";
            for (int i = 1; i < addresses.size(); ++i) {
                inStr += ", '" + addresses.get(i) + "'";
            }
            inStr += " )";
            String sql = " SELECT * FROM media WHERE address IN " + inStr;
            try ( ResultSet results = statement.executeQuery(sql)) {
                while (results.next()) {
                    MediaInformation media = new MediaInformation(results.getString("address"));
                    media.setVideoEncoding(results.getString("video_encoding"));
                    media.setAudioEncoding(results.getString("audio_encoding"));
                    media.setFileSize(results.getLong("size"));
                    media.setDuration(results.getLong("duration"));
                    media.setWidth(results.getInt("width"));
                    media.setHeight(results.getInt("height"));
                    media.setInfo(results.getString("info"));
                    media.setHtml(results.getString("html"));
                    medias.add(media);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);

        }
        return medias;
    }

    public static boolean write(MediaInformation media) {
        if (media == null || media.getAddress() == null) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection();
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM media WHERE address='" + DerbyBase.stringValue(media.getAddress()) + "'";
            statement.executeUpdate(sql);
            sql = "INSERT INTO media(address,video_encoding,audio_encoding,duration,size,width,height,info,html,modify_time) VALUES('"
                    + DerbyBase.stringValue(media.getAddress()) + "', '" + media.getVideoEncoding() + "', '" + media.getAudioEncoding() + "', "
                    + media.getDuration() + ", " + media.getFileSize() + ", " + media.getWidth() + ", "
                    + media.getHeight() + ", '" + DerbyBase.stringValue(media.getInfo()) + "', '" + DerbyBase.stringValue(media.getHtml()) + "', '"
                    + DateTools.datetimeToString(new Date()) + "')";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
//            MyBoxLog.debug(e);
            return false;
        }
    }

    public static boolean write(List<MediaInformation> medias) {
        if (medias == null || medias.isEmpty()) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return write(conn, medias);
        } catch (Exception e) {
            MyBoxLog.error(e);
//            MyBoxLog.debug(e);
            return false;
        }
    }

    public static boolean write(Connection conn, List<MediaInformation> medias) {
        if (conn == null || medias == null || medias.isEmpty()) {
            return false;
        }
        try ( Statement statement = conn.createStatement()) {
            String sql;
            conn.setAutoCommit(false);
            for (MediaInformation media : medias) {
                sql = "DELETE FROM media WHERE address='" + media.getAddress() + "'";
                statement.executeUpdate(sql);
                sql = "INSERT INTO media(address,video_encoding,audio_encoding,duration,size,width,height,info,html,modify_time) VALUES('"
                        + DerbyBase.stringValue(media.getAddress()) + "', '" + media.getVideoEncoding() + "', '" + media.getAudioEncoding() + "', "
                        + media.getDuration() + ", " + media.getFileSize() + ", " + media.getWidth() + ", "
                        + media.getHeight() + ", '" + DerbyBase.stringValue(media.getInfo()) + "', '" + DerbyBase.stringValue(media.getHtml()) + "', '"
                        + DateTools.datetimeToString(new Date()) + "')";
                statement.executeUpdate(sql);
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean delete(List<String> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return true;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return delete(conn, addresses);
        } catch (Exception e) {
            MyBoxLog.error(e);
//            MyBoxLog.debug(e);
            return false;
        }
    }

    public static boolean delete(Connection conn, List<String> addresses) {
        if (conn == null || addresses == null || addresses.isEmpty()) {
            return true;
        }
        try ( Statement statement = conn.createStatement()) {
            String inStr = "( '" + DerbyBase.stringValue(addresses.get(0)) + "'";
            for (int i = 1; i < addresses.size(); ++i) {
                inStr += ", '" + DerbyBase.stringValue(addresses.get(i)) + "'";
            }
            inStr += " )";
            String sql = "DELETE FROM media WHERE address IN " + inStr;
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
//            MyBoxLog.debug(e);
            return false;
        }
    }

    public static boolean delete(String address) {
        try ( Connection conn = DerbyBase.getConnection();
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM media WHERE address='" + DerbyBase.stringValue(address) + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
//            MyBoxLog.debug(e);
            return false;
        }
    }

}
