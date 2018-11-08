package mara.mybox.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import mara.mybox.controller.ImageManufactureController.ImageOperationType;
import mara.mybox.objects.ImageHistory;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2018-11-07
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableImageInit extends DerbyBase {

    public TableImageInit() {
        Table_Name = "image_init";
        Keys = new ArrayList() {
            {
                add("image_location");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE image_init ( "
                + "  image_location  VARCHAR(1024) NOT NULL, "
                + "  init_location  VARCHAR(1024) NOT NULL, "
                + "  init_time TIMESTAMP , "
                + "  PRIMARY KEY (image_location)"
                + " )";
    }

    public static ImageHistory read(String image) {
        ImageHistory record = null;
        if (image == null || image.trim().isEmpty()) {
            return record;
        }
        try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM image_init WHERE image_location='" + image + "'";
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                record = new ImageHistory();
                record.setImage(image);
                record.setHistory_location(results.getString("init_location"));
                record.setUpdate_type(ImageOperationType.Load);
                record.setOperation_time(results.getTimestamp("init_time"));
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
        return record;
    }

    public static ImageHistory write(String image, String his_location) {
        ImageHistory record = null;
        if (image == null || image.trim().isEmpty()) {
            return record;
        }
        try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                Statement statement = conn.createStatement()) {
            record = new ImageHistory();
            record.setImage(image);
            record.setUpdate_type(ImageOperationType.Load);

            String sql = " SELECT * FROM image_init WHERE image_location='" + image + "'";
            ResultSet results = statement.executeQuery(sql);
            if (results == null || !results.next()) {
                sql = "INSERT INTO image_init(image_location, init_location ,init_time) VALUES('"
                        + image + "', '" + his_location + "', '"
                        + DateTools.datetimeToString(new Date()) + "')";
                statement.executeUpdate(sql);
                record.setHistory_location(his_location);
                record.setOperation_time(new Date());
            } else {
                record.setHistory_location(results.getString("init_location"));
                record.setOperation_time(results.getTimestamp("init_time"));
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
        return record;
    }

    public static boolean clear(String image) {
        try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM image_init WHERE image_location='" + image + "'";
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                try {
                    new File(results.getString("init_location")).delete();
                } catch (Exception e) {
                }
            }
            sql = "DELETE FROM image_init WHERE image_location='" + image + "'";
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
            String sql = " SELECT init_location FROM image_init";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                try {
                    new File(results.getString("init_location")).delete();
                } catch (Exception e) {
                }
            }
            sql = "DELETE FROM image_init";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

}
