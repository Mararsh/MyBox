package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import mara.mybox.color.SRGB;

/**
 * @Author Mara
 * @CreateDate 2019-9-28
 * @License Apache License Version 2.0
 */
public class TableSRGB extends DerbyBase {

    public TableSRGB() {
        Table_Name = "SRGB";
        Keys = new ArrayList<>() {
            {
                add("color_value");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE SRGB ( "
                + "  color_value  VARCHAR(15) NOT NULL, "
                + "  color_name VARCHAR(1024) , "
                + "  color_display VARCHAR(4096)  , "
                + "  PRIMARY KEY (color_value)"
                + " )";
    }

    public static SRGB read(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try (Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM SRGB WHERE color_value='" + value + "'";
            statement.setMaxRows(1);
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                SRGB srgb = new SRGB();
                srgb.setColorValue(results.getString("color_value"));
                srgb.setColorName(results.getString("color_name"));
                srgb.setColorDisplay(results.getString("color_display"));
                return srgb;
            }
        } catch (Exception e) {
//            // logger.debug(e.toString());
        }
        return null;
    }

    public static boolean write(String value, String name, String display) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        if (name == null) {
            return display(value, display);
        }
        if (display == null) {
            return name(value, name);
        }
        try (Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM SRGB WHERE color_value='" + value + "'";
            statement.setMaxRows(1);
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                sql = "UPDATE SRGB SET color_name='" + name
                        + "', color_display='" + display + "' WHERE color_value='" + value + "'";
            } else {
                sql = "INSERT INTO SRGB(color_value, color_name, color_display) VALUES('"
                        + value + "', '" + name + "' , '" + display + "')";
            }
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean name(String value, String name) {
        if (value == null || value.trim().isEmpty()
                || name == null || name.trim().isEmpty()) {
            return false;
        }
        try (Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM SRGB WHERE color_value='" + value + "'";
            statement.setMaxRows(1);
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                sql = "UPDATE SRGB SET color_name='" + name
                        + "' WHERE color_value='" + value + "'";
            } else {
                sql = "INSERT INTO SRGB(color_value, color_name) VALUES('"
                        + value + "', '" + name + "')";
            }
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
//            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean display(String value, String display) {
        if (value == null || value.trim().isEmpty()
                || display == null || display.trim().isEmpty()) {
            return false;
        }
        try (Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM SRGB WHERE color_value='" + value + "'";
            statement.setMaxRows(1);
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                sql = "UPDATE SRGB SET color_display='" + display
                        + "' WHERE color_value='" + value + "'";
            } else {
                sql = "INSERT INTO SRGB(color_value, color_display) VALUES('"
                        + value + "', '" + display + "')";
            }
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(String value) {
        try (Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM SRGB WHERE color_value='" + value + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
//            // logger.debug(e.toString());
            return false;
        }
    }

}
