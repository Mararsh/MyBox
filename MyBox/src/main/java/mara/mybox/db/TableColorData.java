package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import mara.mybox.data.ColorData;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2019-9-28
 * @License Apache License Version 2.0
 */
public class TableColorData extends DerbyBase {

    public TableColorData() {
        Table_Name = "Color_Data";
        Keys = new ArrayList<>() {
            {
                add("rgba");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE Color_Data ( "
                + "  rgba  VARCHAR(16) NOT NULL, "
                + "  palette_index DOUBLE, "
                + "  color_name VARCHAR(1024) , "
                + "  color_value INT NOT NULL, "
                + "  rgb  VARCHAR(16) NOT NULL, "
                + "  srgb VARCHAR(100)  , "
                + "  hsb VARCHAR(100)  , "
                + "  adobeRGB VARCHAR(100)  , "
                + "  appleRGB VARCHAR(100)  , "
                + "  eciRGB VARCHAR(100)  , "
                + "  sRGBLinear VARCHAR(100)  , "
                + "  adobeRGBLinear VARCHAR(100)  , "
                + "  appleRGBLinear VARCHAR(100)  , "
                + "  calculatedCMYK VARCHAR(100)  , "
                + "  eciCMYK VARCHAR(100)  , "
                + "  adobeCMYK VARCHAR(100)  , "
                + "  xyz VARCHAR(100)  , "
                + "  cieLab VARCHAR(100)  , "
                + "  lchab VARCHAR(100)  , "
                + "  cieLuv VARCHAR(100)  , "
                + "  lchuv VARCHAR(100)  , "
                + "  PRIMARY KEY (rgba)"
                + " )";

    }

    public static int size() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            conn.setReadOnly(true);
            String sql = " SELECT count(rgba) FROM Color_Data";
            ResultSet results = conn.createStatement().executeQuery(sql);
            if (results.next()) {
                return results.getInt(1);
            } else {
                return 0;
            }
        } catch (Exception e) {
            failed(e);
            return 0;
        }
    }

    public static List<ColorData> read() {
        List<ColorData> palette = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            String sql = " SELECT * FROM Color_Data";
            ResultSet results = conn.createStatement().executeQuery(sql);
            while (results.next()) {
                ColorData data = read(results);
                palette.add(data);
            }
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
        }
        return palette;
    }

    public static List<ColorData> read(int offset, int number) {
        List<ColorData> palette = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            String sql = " SELECT * FROM Color_Data OFFSET "
                    + offset + " ROWS FETCH NEXT " + number + " ROWS ONLY";
            ResultSet results = conn.createStatement().executeQuery(sql);
            while (results.next()) {
                ColorData data = read(results);
                palette.add(data);
            }
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
        }
        return palette;
    }

    public static ColorData read(Color color) {
        if (color == null) {
            return null;
        }
        return read(color.toString());
    }

    public static ColorData read(String rgba) {
        if (rgba == null) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            String sql = " SELECT * FROM Color_Data WHERE rgba='" + rgba + "'";
            Statement statement = conn.createStatement();
            statement.setMaxRows(1);
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                ColorData data = read(results);
                return data;
            }
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
        }
        return null;
    }

    public static ColorData read(ResultSet results) {
        if (results == null) {
            return null;
        }
        try {
            ColorData data = new ColorData();
            data.setRgba(results.getString("rgba"));
            data.setPaletteIndex(results.getDouble("palette_index"));
            data.setInPalette(data.getPaletteIndex() > -1);
            data.setColorName(results.getString("color_name"));
            data.setColorValue(results.getInt("color_value"));
            data.setRgb(results.getString("rgb"));
            data.setSrgb(results.getString("srgb"));
            data.setHsb(results.getString("hsb"));
            data.setAdobeRGB(results.getString("adobeRGB"));
            data.setAppleRGB(results.getString("appleRGB"));
            data.setEciRGB(results.getString("eciRGB"));
            data.setSRGBLinear(results.getString("sRGBLinear"));
            data.setAdobeRGBLinear(results.getString("adobeRGBLinear"));
            data.setAppleRGBLinear(results.getString("appleRGBLinear"));
            data.setCalculatedCMYK(results.getString("calculatedCMYK"));
            data.setEciCMYK(results.getString("eciCMYK"));
            data.setAdobeCMYK(results.getString("adobeCMYK"));
            data.setXyz(results.getString("xyz"));
            data.setCieLab(results.getString("cieLab"));
            data.setLchab(results.getString("lchab"));
            data.setCieLuv(results.getString("cieLuv"));
            data.setLchuv(results.getString("lchuv"));
            data.bindInPalette();
            return data;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return null;
        }

    }

    public static ColorData write(String rgba, boolean replace) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            return write(conn, rgba, null, replace);
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static ColorData write(Color color, boolean replace) {
        try {
            return write(color.toString(), replace);
        } catch (Exception e) {
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static int writeColors(List<Color> colors, boolean replace) {
        if (colors == null || colors.isEmpty()) {
            return -1;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            int count = 0;
            conn.setAutoCommit(false);
            for (Color color : colors) {
                String rgba = color.toString();
                if (write(conn, rgba, null, replace) != null) {
                    count++;
                }
            }
            conn.commit();
            return count;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return -1;
        }
    }

    public static ColorData write(Connection conn, String rgba, String name, boolean replace) {
        if (conn == null) {
            return null;
        }
        try {
            String sql = " SELECT * FROM Color_Data WHERE rgba='" + rgba + "'";
            boolean exist;
            try ( ResultSet results = conn.createStatement().executeQuery(sql)) {
                exist = results.next();
            }
            if (exist) {
                if (replace) {
                    ColorData data = new ColorData(rgba, name).calculate();
                    update(conn, data);
                    return data;
                } else {
                    return null;
                }
            } else {
                ColorData data = new ColorData(rgba, name).calculate();
                insert(conn, data);
                return data;
            }
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static ColorData write(ColorData data, boolean replace) {
        if (data == null) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            return write(conn, data, replace);
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static int writeData(List<ColorData> dataList, boolean replace) {
        if (dataList == null) {
            return -1;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            int count = 0;
            conn.setAutoCommit(false);
            for (ColorData data : dataList) {
                if (write(conn, data, replace) != null) {
                    count++;
                }
            }
            conn.commit();
            return count;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return -1;
        }
    }

    public static ColorData write(Connection conn, ColorData data,
            boolean replace) {
        if (conn == null || data == null) {
            return null;
        }
        try {
            String sql = " SELECT * FROM Color_Data WHERE rgba='" + data.getRgba() + "'";
            Statement statement = conn.createStatement();
            statement.setMaxRows(1);
            boolean exist;
            try ( ResultSet results = statement.executeQuery(sql)) {
                exist = results.next();
            }
            if (exist) {
                if (replace) {
                    update(conn, data);
                } else {
                    return null;
                }
            } else {
                insert(conn, data);
            }
            return data;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static boolean insert(Connection conn, ColorData data) {
        if (conn == null || data == null) {
            return false;
        }
        try {
            String name = data.getColorName();
            if (name == null) {
                name = "";
            }
            if (data.getSrgb() == null) {
                data.calculate();
            }
            String sql = "INSERT INTO Color_Data "
                    + "(rgba, palette_index, color_name, color_value, rgb, srgb, hsb, adobeRGB ,appleRGB, eciRGB, "
                    + "sRGBLinear, adobeRGBLinear, appleRGBLinear,  calculatedCMYK,  eciCMYK, adobeCMYK, "
                    + " xyz,  cieLab, lchab, cieLuv,  lchuv ) VALUES ("
                    + " '" + data.getRgba() + "', " + data.getPaletteIndex() + ", '" + stringValue(name) + "', "
                    + data.getColorValue() + ", '" + data.getRgb() + "', "
                    + " '" + data.getSrgb() + "', '" + data.getHsb() + "', "
                    + " '" + data.getAdobeRGB() + "', '" + data.getAppleRGB() + "', '" + data.getEciRGB() + "', "
                    + " '" + data.getSRGBLinear() + "', '" + data.getAdobeRGBLinear() + "', "
                    + " '" + data.getAppleRGBLinear() + "', '" + data.getCalculatedCMYK() + "', "
                    + " '" + data.getEciCMYK() + "', '" + data.getAdobeCMYK() + "', "
                    + " '" + data.getXyz() + "', '" + data.getCieLab() + "', "
                    + " '" + data.getLchab() + "', '" + data.getCieLuv() + "', "
                    + " '" + data.getLchuv() + "' "
                    + " )";
            conn.createStatement().executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean update(Connection conn, ColorData data) {
        if (conn == null || data == null) {
            return false;
        }
        try {
            String name = data.getColorName();
            if (data.getSrgb() == null) {
                data.calculate();
            }
            String sql = "UPDATE Color_Data SET "
                    + " palette_index=" + data.getPaletteIndex()
                    + ", " + (name == null ? "" : " color_name='" + stringValue(name) + "', ")
                    + " color_value=" + data.getColorValue() + ", rgb='" + data.getRgb() + "', "
                    + " srgb='" + data.getSrgb() + "', hsb='" + data.getHsb() + "', "
                    + " adobeRGB='" + data.getAdobeRGB() + "', appleRGB='" + data.getAppleRGB() + "', eciRGB='" + data.getEciRGB() + "', "
                    + " sRGBLinear='" + data.getSRGBLinear() + "', adobeRGBLinear='" + data.getAdobeRGBLinear() + "', "
                    + " appleRGBLinear='" + data.getAppleRGBLinear() + "', calculatedCMYK='" + data.getCalculatedCMYK() + "', "
                    + " eciCMYK='" + data.getEciCMYK() + "', adobeCMYK='" + data.getAdobeCMYK() + "', "
                    + " xyz='" + data.getXyz() + "', cieLab='" + data.getCieLab() + "', "
                    + " lchab='" + data.getLchab() + "', cieLuv='" + data.getCieLuv() + "', "
                    + " lchuv='" + data.getLchuv() + "'  "
                    + " WHERE rgba='" + data.getRgba() + "'";
            conn.createStatement().executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean setName(Color color, String name) {
        if (color == null || name == null) {
            return false;
        }
        return setName(color.toString(), name);
    }

    public static boolean setName(String rgba, String name) {
        if (name == null) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            String sql = " SELECT * FROM Color_Data WHERE rgba='" + rgba + "'";
            Statement statement = conn.createStatement();
            statement.setMaxRows(1);
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                sql = "UPDATE Color_Data SET "
                        + " color_name='" + stringValue(name) + "' "
                        + " WHERE rgba='" + rgba + "'";
                conn.createStatement().executeUpdate(sql);
            } else {
                ColorData data = new ColorData(rgba).calculate();
                data.setColorName(name);
                insert(conn, data);
            }
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static List<ColorData> readPalette() {
        List<ColorData> palette = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            String sql = " SELECT * FROM Color_Data WHERE palette_index > -1 ORDER BY palette_index ASC";
            ResultSet results = conn.createStatement().executeQuery(sql);
            while (results.next()) {
                ColorData data = read(results);
                palette.add(data);
            }
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
        }
        return palette;
    }

    public static ColorData setPalette(Color color, int palette_index) {
        if (color == null) {
            return null;
        }
        return setPalette(color.toString(), palette_index);
    }

    public static boolean addInPalette(List<ColorData> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            String sql = "SELECT MAX(palette_index) as maxp FROM Color_Data WHERE palette_index > -1";
            double index;
            try ( ResultSet results = conn.createStatement().executeQuery(sql)) {
                index = 1;
                if (results.next()) {
                    try {
                        index = results.getDouble("maxp") + 1;
                    } catch (Exception e) {
                    }
                }
            }
            conn.setAutoCommit(false);
            for (int i = 0; i < values.size(); i++) {
                setPalette(conn, values.get(i).getRgba(), index + i);
            }
            conn.commit();
            trimPalette(conn);
            conn.commit();
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean removeFromPalette(Color color) {
        if (color == null) {
            return false;
        }
        return removeFromPalette(color.toString());
    }

    public static boolean removeFromPalette(String rgba) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            String sql = "UPDATE Color_Data SET palette_index=-1 WHERE rgba='" + rgba + "'";
            conn.createStatement().executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean removeFromPalette(List<ColorData> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            String inStr = "( '" + values.get(0).getRgba() + "' ";
            for (int i = 1; i < values.size(); ++i) {
                inStr += ", '" + values.get(i).getRgba() + "' ";
            }
            inStr += " )";
            String sql = "UPDATE Color_Data SET palette_index=-1 WHERE rgba IN " + inStr;
            conn.createStatement().executeUpdate(sql);
            conn.setAutoCommit(false);
            trimPalette(conn);
            conn.commit();
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static ColorData frontPalette(Color color) {
        if (color == null) {
            return null;
        }
        String rgba = color.toString();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            String sql = "SELECT MIN(palette_index) as minp FROM Color_Data WHERE palette_index > -1";
            ResultSet results = conn.createStatement().executeQuery(sql);
            if (results.next()) {
                try {
                    double index = results.getDouble("minp");
                    return setPalette(conn, rgba, index - (index + 1) / 100d);
                } catch (Exception e) {
                    return setPalette(conn, rgba, 1);
                }
            } else {
                return setPalette(conn, rgba, 1);
            }
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static ColorData endPalette(Color color) {
        if (color == null) {
            return null;
        }
        String rgba = color.toString();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            String sql = "SELECT MAX(palette_index) as maxp FROM Color_Data WHERE palette_index > -1";
            ResultSet results = conn.createStatement().executeQuery(sql);
            if (results.next()) {
                try {
                    double index = results.getDouble("maxp") + 1;
                    return setPalette(conn, rgba, index);
                } catch (Exception e) {
                    return setPalette(conn, rgba, 1);
                }
            } else {
                return setPalette(conn, rgba, 1);
            }
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static ColorData setPalette(String rgba, double palette_index) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {

            return setPalette(conn, rgba, palette_index);
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static ColorData setPalette(Connection conn, String rgba,
            double palette_index) {
        if (conn == null) {
            return null;
        }
        try {
            String sql = " SELECT * FROM Color_Data WHERE rgba='" + rgba + "'";
            Statement statement = conn.createStatement();
            statement.setMaxRows(1);
            ResultSet results = statement.executeQuery(sql);
            ColorData data;
            if (results.next()) {
                data = read(results);
                data.setPaletteIndex(palette_index);
                update(conn, data);
            } else {
                data = new ColorData(rgba).calculate();
                data.setPaletteIndex(palette_index);
                insert(conn, data);
            }
            return data;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static ColorData endPalette(ColorData data) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            String sql = "SELECT MAX(palette_index) as maxp FROM Color_Data WHERE palette_index > -1";
            ResultSet results = conn.createStatement().executeQuery(sql);
            double index = 1;
            if (results.next()) {
                try {
                    index = results.getDouble("maxp") + 1;
                } catch (Exception e) {
                }
            }
            data.setPaletteIndex(index);
            return write(conn, data, true);
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static ColorData frontPalette(ColorData data) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            String sql = "SELECT MIN(palette_index) as minp FROM Color_Data WHERE palette_index > -1";
            double index = 1;
            try ( ResultSet results = conn.createStatement().executeQuery(sql)) {
                if (results.next()) {
                    try {
                        double min = results.getDouble("minp");
                        index = min - (min + 1) / 100d;
                    } catch (Exception e) {
                    }
                }
            }
            data.setPaletteIndex(index);
            return write(conn, data, true);
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static boolean clearPalette() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            String sql = "UPDATE Color_Data SET palette_index=-1";
            conn.createStatement().executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean updatePaletteColor(List<Color> colors) {
        if (colors == null || colors.isEmpty()) {
            clearPalette();
            return true;
        }
        List<String> values = new ArrayList<>();
        for (Color color : colors) {
            values.add(color.toString());
        }
        return updatePalette(values);
    }

    public static boolean updatePaletteColorData(List<ColorData> colors) {
        if (colors == null || colors.isEmpty()) {
            clearPalette();
            return true;
        }
        List<String> values = new ArrayList<>();
        for (ColorData data : colors) {
            values.add(data.getRgba());
        }
        return updatePalette(values);
    }

    public static boolean updatePalette(List<String> values) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            conn.setAutoCommit(false);
            String sql = "UPDATE Color_Data SET palette_index=-1";
            conn.createStatement().executeUpdate(sql);
            conn.commit();
            if (values != null) {
                for (int i = 0; i < values.size(); ++i) {
                    setPalette(conn, values.get(i), i);
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean trimPalette() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            conn.setAutoCommit(false);
            trimPalette(conn);
            conn.commit();
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean trimPalette(Connection conn) {
        try {
            String sql = " SELECT rgba, palette_index FROM Color_Data WHERE palette_index > -1 ORDER BY palette_index ASC";
            List<String> values;
            try ( ResultSet results = conn.createStatement().executeQuery(sql)) {
                values = new ArrayList<>();
                while (results.next()) {
                    values.add(results.getString("rgba"));
                }
            }
            for (int i = 0; i < values.size(); i++) {
                sql = "UPDATE Color_Data SET palette_index=" + i + " WHERE rgba='" + values.get(i) + "'";
                conn.createStatement().executeUpdate(sql);
            }
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(String rgba) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            String sql = "DELETE FROM Color_Data WHERE rgba='" + rgba + "'";
            conn.createStatement().executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static int deleteData(List<ColorData> values) {
        if (values == null || values.isEmpty()) {
            return 0;
        }
        int count = 0;
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            String inStr = "( '" + values.get(0).getRgba() + "' ";
            for (int i = 1; i < values.size(); ++i) {
                inStr += ", '" + values.get(i).getRgba() + "' ";
            }
            inStr += " )";
            String sql = "DELETE FROM Color_Data WHERE rgba IN " + inStr;
            count += conn.createStatement().executeUpdate(sql);
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
        }
        return count;
    }

    public static boolean delete(List<String> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            String inStr = "( '" + values.get(0) + "' ";
            for (int i = 1; i < values.size(); ++i) {
                inStr += ", '" + values.get(i) + "' ";
            }
            inStr += " )";
            String sql = "DELETE FROM Color_Data WHERE rgba IN " + inStr;
            conn.createStatement().executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean migrate() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            String sql = " SELECT * FROM SRGB WHERE palette_index >= 0";
            ResultSet olddata = conn.createStatement().executeQuery(sql);
            List<ColorData> oldData = new ArrayList<>();
            while (olddata.next()) {
                ColorData data = new ColorData(olddata.getString("color_value")).calculate();
                String name = olddata.getString("color_name");
                if (name != null && !name.isEmpty()) {
                    data.setColorName(name);
                }
                data.setPaletteIndex(olddata.getInt("palette_index"));
                oldData.add(data);
            }
            for (ColorData data : oldData) {
                write(conn, data, true);
            }
            sql = "DROP TABLE SRGB";
            conn.createStatement().executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            logger.debug(e.toString());
            return false;
        }
    }

}
