package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import mara.mybox.color.SRGB;
import static mara.mybox.value.AppVariables.logger;

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
                + "  palette_index  INT  , "
                + "  PRIMARY KEY (color_value)"
                + " )";
    }

    public static SRGB read(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM SRGB WHERE color_value='" + value + "'";
            statement.setMaxRows(1);
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                SRGB srgb = new SRGB();
                srgb.setColorValue(results.getString("color_value"));
                srgb.setColorName(results.getString("color_name"));
                srgb.setColorDisplay(results.getString("color_display"));
                srgb.setPaletteIndex(results.getInt("palette_index"));
                return srgb;
            }
        } catch (Exception e) {  failed(e);
//            // logger.debug(e.toString());
        }
        return null;
    }

    public static List<SRGB> readPalette() {
        List<SRGB> palette = new ArrayList();
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM SRGB WHERE palette_index >= 0 ORDER BY palette_index";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                SRGB srgb = new SRGB();
                srgb.setColorValue(results.getString("color_value"));
                srgb.setColorName(results.getString("color_name"));
                srgb.setColorDisplay(results.getString("color_display"));
                srgb.setPaletteIndex(results.getInt("palette_index"));
                palette.add(srgb);
            }
        } catch (Exception e) {  failed(e);
//            // logger.debug(e.toString());
        }
        return palette;
    }

    public static boolean updatePaletteColor(List<Color> colors) {
        if (colors == null || colors.isEmpty()) {
            clearPalette();
            return true;
        }
        List<String> values = new ArrayList();
        for (Color color : colors) {
            values.add(color.toString());
        }
        return updatePalette(values);
    }

    public static boolean updatePalette(List<String> colors) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            conn.setAutoCommit(false);
            String sql = "UPDATE SRGB SET palette_index= -1";
            statement.executeUpdate(sql);
            if (colors != null) {
                for (int i = 0; i < colors.size(); i++) {
                    String value = colors.get(i);
                    sql = " SELECT * FROM SRGB WHERE color_value='" + value + "'";
                    statement.setMaxRows(1);
                    ResultSet results = statement.executeQuery(sql);
                    if (results.next()) {
                        sql = "UPDATE SRGB SET  palette_index=" + i
                                + " WHERE color_value='" + value + "'";
                    } else {
                        sql = "INSERT INTO SRGB(color_value,palette_index) VALUES('"
                                + value + "', " + i + ")";
                    }
                    statement.executeUpdate(sql);
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {  failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean write(String value, String name, String display, int palette_index) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        if (name == null) {
            return setDisplay(value, display);
        }
        if (display == null) {
            return setName(value, name);
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM SRGB WHERE color_value='" + value + "'";
            statement.setMaxRows(1);
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                sql = "UPDATE SRGB SET color_name='" + name
                        + "', color_display='" + display
                        + "', palette_index=" + palette_index
                        + " WHERE color_value='" + value + "'";
            } else {
                sql = "INSERT INTO SRGB(color_value, color_name, color_display, palette_index) VALUES('"
                        + value + "', '" + name + "' , '" + display + "', " + palette_index + ")";
            }
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {  failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean setName(String value, String name) {
        if (value == null || value.trim().isEmpty()
                || name == null || name.trim().isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
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
        } catch (Exception e) {  failed(e);
//            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean setDisplay(String value, String display) {
        if (value == null || value.trim().isEmpty()
                || display == null || display.trim().isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
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
        } catch (Exception e) {  failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean setPalette(String value, int palette_index) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        value = value.trim();
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM SRGB WHERE color_value='" + value + "'";
            statement.setMaxRows(1);
            ResultSet results = statement.executeQuery(sql);
            if (!results.next()) {
                return false;
            }
            sql = "UPDATE SRGB SET palette_index=" + palette_index
                    + " WHERE color_value='" + value + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {  failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean clearPalette() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "UPDATE SRGB SET palette_index= -1";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {  failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(String value) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM SRGB WHERE color_value='" + value + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {  failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

}
