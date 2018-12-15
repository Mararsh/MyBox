package mara.mybox.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.ImageHistory;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2018-10-15 9:31:28
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableImageHistory extends DerbyBase {

    private static final int Default_Max_Histories = 20;

    public TableImageHistory() {
        Table_Name = "image_history";
        Keys = new ArrayList() {
            {
                add("image_location");
                add("history_location");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE image_history ( "
                + "  image_location  VARCHAR(1024) NOT NULL, "
                + "  operation_time TIMESTAMP NOT NULL, "
                + "  history_location  VARCHAR(1024) NOT NULL, "
                + "  update_type SMALLINT, "
                + "  PRIMARY KEY (image_location, history_location)"
                + " )";
    }

    public static List<ImageHistory> read(String image) {
        List<ImageHistory> records = new ArrayList<>();
        if (image == null || image.trim().isEmpty()) {
            return records;
        }
        int max = AppVaribles.getUserConfigInt("MaxImageHistories", Default_Max_Histories);
        try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM image_history WHERE image_location='" + image + "' ORDER BY operation_time DESC";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                ImageHistory his = new ImageHistory();
                his.setImage(image);
                his.setHistory_location(results.getString("history_location"));
                his.setUpdate_type(results.getInt("update_type"));
                his.setOperation_time(results.getTimestamp("operation_time"));
                records.add(his);
            }

            if (records.size() > max) {
                for (int i = max; i < records.size(); i++) {
                    sql = "DELETE FROM image_history WHERE image_location='" + image
                            + "' AND operation_time='" + DateTools.datetimeToString(records.get(i).getOperation_time()) + "'";
                    statement.executeUpdate(sql);
                    try {
                        new File(records.get(i).getHistory_location()).delete();
                    } catch (Exception e) {
                    }
                }
                return records.subList(0, max);
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }
        return records;
    }

    public static List<ImageHistory> add(String image, int update_type, String his_location) {
        List<ImageHistory> records = read(image);
        if (image == null || image.trim().isEmpty()
                || his_location == null || his_location.trim().isEmpty()) {
            return records;
        }
        try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                Statement statement = conn.createStatement()) {
            String sql = "INSERT INTO image_history(image_location, history_location , update_type, operation_time) VALUES('"
                    + image + "', '" + his_location + "', " + update_type + ", '"
                    + DateTools.datetimeToString(new Date()) + "')";
            statement.executeUpdate(sql);
            return read(image);
        } catch (Exception e) {
            logger.debug(e.toString());
            return records;
        }
    }

    public static boolean clearImage(String image) {
        try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM image_history WHERE image_location='" + image + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean clearHistory(String image, String his) {
        try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM image_history WHERE image_location='" + image + "' AND "
                    + "history_location='" + his + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    @Override
    public boolean clear() {
        try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                Statement statement = conn.createStatement()) {
            String sql = " SELECT history_location FROM image_history";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                try {
                    new File(results.getString("history_location")).delete();
                } catch (Exception e) {
                }
            }
            String imageHistoriesPath = AppVaribles.getImageHisPath();
            File path = new File(imageHistoriesPath);
            if (path.exists()) {
                File[] files = path.listFiles();
                for (File f : files) {
                    f.delete();
                }
            }
            sql = "DELETE FROM image_history";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

}
