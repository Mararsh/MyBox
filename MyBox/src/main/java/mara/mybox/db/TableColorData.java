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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT count(rgba) FROM Color_Data";
            ResultSet results = statement.executeQuery(sql);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM Color_Data";
            ResultSet results = statement.executeQuery(sql);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM Color_Data OFFSET "
                    + offset + " ROWS FETCH NEXT " + number + " ROWS ONLY";
            ResultSet results = statement.executeQuery(sql);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM Color_Data WHERE rgba='" + rgba + "'";
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            return write(statement, rgba, replace);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            int count = 0;
            conn.setAutoCommit(false);
            for (Color color : colors) {
                String rgba = color.toString();
                if (write(statement, rgba, replace) != null) {
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

    public static ColorData write(Statement statement, String rgba,
            boolean replace) {
        if (statement == null) {
            return null;
        }
        try {
            String sql = " SELECT * FROM Color_Data WHERE rgba='" + rgba + "'";
            boolean exist;
            try ( ResultSet results = statement.executeQuery(sql)) {
                exist = results.next();
            }
            if (exist) {
                if (replace) {
                    ColorData data = new ColorData(rgba).calculate();
                    update(statement, data);
                    return data;
                } else {
                    return null;
                }
            } else {
                ColorData data = new ColorData(rgba).calculate();
                insert(statement, data);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            return write(statement, data, replace);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            int count = 0;
            conn.setAutoCommit(false);
            for (ColorData data : dataList) {
                if (write(statement, data, replace) != null) {
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

    public static ColorData write(Statement statement, ColorData data,
            boolean replace) {
        if (statement == null || data == null) {
            return null;
        }
        try {
            String sql = " SELECT * FROM Color_Data WHERE rgba='" + data.getRgba() + "'";
            statement.setMaxRows(1);
            boolean exist;
            try ( ResultSet results = statement.executeQuery(sql)) {
                exist = results.next();
            }
            if (exist) {
                if (replace) {
                    update(statement, data);
                } else {
                    return null;
                }
            } else {
                insert(statement, data);
            }
            return data;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static boolean insert(Statement statement, ColorData data) {
        if (statement == null || data == null) {
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
                    + " '" + data.getRgba() + "', " + data.getPaletteIndex() + ", '" + name + "', "
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
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean update(Statement statement, ColorData data) {
        if (statement == null || data == null) {
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
            String sql = "UPDATE Color_Data SET "
                    + " palette_index=" + data.getPaletteIndex() + ", " + " color_name='" + name + "', "
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
            statement.executeUpdate(sql);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM Color_Data WHERE rgba='" + rgba + "'";
            statement.setMaxRows(1);
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                sql = "UPDATE Color_Data SET "
                        + " color_name='" + name + "' "
                        + " WHERE rgba='" + rgba + "'";
                statement.executeUpdate(sql);
            } else {
                ColorData data = new ColorData(rgba).calculate();
                data.setColorName(name);
                insert(statement, data);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM Color_Data WHERE palette_index > -1 ORDER BY palette_index ASC";
            ResultSet results = statement.executeQuery(sql);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT MAX(palette_index) as maxp FROM Color_Data WHERE palette_index > -1";
            double index;
            try ( ResultSet results = statement.executeQuery(sql)) {
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
                setPalette(statement, values.get(i).getRgba(), index + i);
            }
            conn.commit();
            trimPalette(statement);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "UPDATE Color_Data SET palette_index=-1 WHERE rgba='" + rgba + "'";
            statement.executeUpdate(sql);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String inStr = "( '" + values.get(0).getRgba() + "' ";
            for (int i = 1; i < values.size(); ++i) {
                inStr += ", '" + values.get(i).getRgba() + "' ";
            }
            inStr += " )";
            String sql = "UPDATE Color_Data SET palette_index=-1 WHERE rgba IN " + inStr;
            statement.executeUpdate(sql);
            conn.setAutoCommit(false);
            trimPalette(statement);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT MIN(palette_index) as minp FROM Color_Data WHERE palette_index > -1";
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                try {
                    double index = results.getDouble("minp");
                    return setPalette(statement, rgba, index - (index + 1) / 100d);
                } catch (Exception e) {
                    return setPalette(statement, rgba, 1);
                }
            } else {
                return setPalette(statement, rgba, 1);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT MAX(palette_index) as maxp FROM Color_Data WHERE palette_index > -1";
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                try {
                    double index = results.getDouble("maxp") + 1;
                    return setPalette(statement, rgba, index);
                } catch (Exception e) {
                    return setPalette(statement, rgba, 1);
                }
            } else {
                return setPalette(statement, rgba, 1);
            }
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static ColorData setPalette(String rgba, double palette_index) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {

            return setPalette(statement, rgba, palette_index);
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static ColorData setPalette(Statement statement, String rgba,
            double palette_index) {
        if (statement == null) {
            return null;
        }
        try {
            String sql = " SELECT * FROM Color_Data WHERE rgba='" + rgba + "'";
            statement.setMaxRows(1);
            ResultSet results = statement.executeQuery(sql);
            ColorData data;
            if (results.next()) {
                data = read(results);
                data.setPaletteIndex(palette_index);
                update(statement, data);
            } else {
                data = new ColorData(rgba).calculate();
                data.setPaletteIndex(palette_index);
                insert(statement, data);
            }
            return data;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static ColorData endPalette(ColorData data) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT MAX(palette_index) as maxp FROM Color_Data WHERE palette_index > -1";
            ResultSet results = statement.executeQuery(sql);
            double index = 1;
            if (results.next()) {
                try {
                    index = results.getDouble("maxp") + 1;
                } catch (Exception e) {
                }
            }
            data.setPaletteIndex(index);
            return write(statement, data, true);
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static ColorData frontPalette(ColorData data) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT MIN(palette_index) as minp FROM Color_Data WHERE palette_index > -1";
            double index = 1;
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                try {
                    double min = results.getDouble("minp");
                    index = min - (min + 1) / 100d;
                } catch (Exception e) {
                }
            }
            results.close();
            data.setPaletteIndex(index);
            return write(statement, data, true);
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }
    }

    public static boolean clearPalette() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "UPDATE Color_Data SET palette_index=-1";
            statement.executeUpdate(sql);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            conn.setAutoCommit(false);
            String sql = "UPDATE Color_Data SET palette_index=-1";
            statement.executeUpdate(sql);
            conn.commit();
            if (values != null) {
                for (int i = 0; i < values.size(); ++i) {
                    setPalette(statement, values.get(i), i);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            conn.setAutoCommit(false);
            trimPalette(statement);
            conn.commit();
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean trimPalette(Statement statement) {
        try {
            String sql = " SELECT rgba, palette_index FROM Color_Data WHERE palette_index > -1 ORDER BY palette_index ASC";
            List<String> values;
            try ( ResultSet results = statement.executeQuery(sql)) {
                values = new ArrayList<>();
                while (results.next()) {
                    values.add(results.getString("rgba"));
                }
            }
            for (int i = 0; i < values.size(); i++) {
                sql = "UPDATE Color_Data SET palette_index=" + i + " WHERE rgba='" + values.get(i) + "'";
                statement.executeUpdate(sql);
            }
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(String rgba) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM Color_Data WHERE rgba='" + rgba + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean deleteData(List<ColorData> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String inStr = "( '" + values.get(0).getRgba() + "' ";
            for (int i = 1; i < values.size(); ++i) {
                inStr += ", '" + values.get(i).getRgba() + "' ";
            }
            inStr += " )";
            String sql = "DELETE FROM Color_Data WHERE rgba IN " + inStr;
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(List<String> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String inStr = "( '" + values.get(0) + "' ";
            for (int i = 1; i < values.size(); ++i) {
                inStr += ", '" + values.get(i) + "' ";
            }
            inStr += " )";
            String sql = "DELETE FROM Color_Data WHERE rgba IN " + inStr;
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean migrate() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM SRGB WHERE palette_index >= 0";
            ResultSet olddata = statement.executeQuery(sql);
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
                write(statement, data, true);
            }
            sql = "DROP TABLE SRGB";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            logger.debug(e.toString());
            return false;
        }
    }

}
