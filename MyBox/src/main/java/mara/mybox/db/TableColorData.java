package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import mara.mybox.data.ColorData;
import mara.mybox.fxml.FxmlColor;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

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
                + "  rgba  VARCHAR(16) NOT NULL, " // upper-case
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

    public static final String UniqueQeury
            = "SELECT * FROM Color_Data WHERE rgba=?";

    public static final String RemoveFromPalette
            = "UPDATE Color_Data SET palette_index=-1 WHERE rgba=?";

    public static final String Delete
            = "DELETE FROM Color_Data WHERE rgba=?";

    public static int size() {
        int v = 0;
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            conn.setReadOnly(true);
            try ( ResultSet results = statement.executeQuery(" SELECT count(rgba) FROM Color_Data")) {
                if (results.next()) {
                    v = results.getInt(1);
                }
            }
        } catch (Exception e) {
            failed(e);
        }
        return v;
    }

    public static List<ColorData> readAll() {
        String sql = " SELECT * FROM Color_Data";
        return readList(sql);
    }

    public static List<ColorData> readPage(int offset, int number) {
        String sql = " SELECT * FROM Color_Data OFFSET "
                + offset + " ROWS FETCH NEXT " + number + " ROWS ONLY";
        return readList(sql);
    }

    public static List<ColorData> readList(String sql) {
        List<ColorData> palette = new ArrayList<>();
        if (sql == null) {
            return palette;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            try ( Statement statement = conn.createStatement();
                     ResultSet results = statement.executeQuery(sql)) {
                while (results.next()) {
                    ColorData data = read(results);
                    palette.add(data);
                }
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
        }
        return palette;
    }

    public static ColorData read(String rgba) {
        if (rgba == null) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            return read(conn, rgba);
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
        }
        return null;
    }

    public static ColorData read(Connection conn, String rgba) {
        if (conn == null || rgba == null) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(UniqueQeury)) {
            statement.setString(1, rgba.toUpperCase());
            statement.setMaxRows(1);
            try ( ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    ColorData data = read(results);
                    return data;
                }
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
        }
        return null;
    }

    public static ColorData read(ResultSet results) {
        if (results == null) {
            return null;
        }
        try {
            ColorData data = new ColorData();
            data.setWeb(results.getString("rgba"));
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

    public static ColorData read(Color color) {
        if (color == null) {
            return null;
        }
        return read(FxmlColor.color2rgba(color));
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

    public static ColorData write(Connection conn, String rgba, String name, boolean replace) {
        if (conn == null || rgba == null) {
            return null;
        }
        ColorData exist = read(conn, rgba);
        if (exist != null) {
            if (replace) {
                ColorData data = new ColorData(rgba, name).calculate();
                if (update(conn, data)) {
                    return data;
                } else {
                    return null;
                }
            } else {
                return exist;
            }
        } else {
            ColorData data = new ColorData(rgba, name).calculate();
            if (insert(conn, data)) {
                return data;
            } else {
                return null;
            }
        }
    }

    public static ColorData write(Connection conn, String rgba, boolean replace) {
        return write(conn, rgba, null, replace);
    }

    public static ColorData write(Color color, boolean replace) {
        try {
            return write(FxmlColor.color2rgba(color), replace);
        } catch (Exception e) {
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

    public static ColorData write(Connection conn, ColorData data, boolean replace) {
        if (conn == null || data == null) {
            return null;
        }
        ColorData exist = read(conn, data.getRgba());
        if (exist != null) {
            if (replace) {
                if (update(conn, data)) {
                    return data;
                } else {
                    return null;
                }
            } else {
                return exist;
            }
        } else {
            if (insert(conn, data)) {
                return data;
            } else {
                return null;
            }
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
                String rgba = FxmlColor.color2rgba(color);
                if (write(conn, rgba, replace) != null) {
                    count++;
                }
            }
            conn.commit();
            return count;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return -1;
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
        return setName(FxmlColor.color2rgba(color), name);
    }

    public static boolean setName(String rgba, String name) {
        if (name == null || rgba == null) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            ColorData exist = read(conn, rgba);
            if (exist != null) {
                String sql = "UPDATE Color_Data SET "
                        + " color_name='" + stringValue(name) + "' "
                        + " WHERE rgba='" + rgba.toUpperCase() + "'";
                return statement.executeUpdate(sql) > 0;
            } else {
                ColorData data = new ColorData(rgba).calculate();
                data.setColorName(name);
                return insert(conn, data);
            }
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(String rgba) {
        if (rgba == null) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            return delete(conn, rgba);
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(Connection conn, String rgba) {
        if (conn == null || rgba == null) {
            return false;
        }
        try {
            String sql = "DELETE FROM Color_Data WHERE rgba='" + rgba.toUpperCase() + "'";
            conn.createStatement().executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static int deleteData(List<ColorData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }
        int count = 0;
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 PreparedStatement delete = conn.prepareStatement(Delete)) {
            conn.setAutoCommit(false);
            for (ColorData data : dataList) {
                delete.setString(1, data.getRgba());
                int ret = delete.executeUpdate();
                if (ret > 0) {
                    count += ret;
                }
            }
            conn.commit();
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
        }
        return count;
    }

    public static int delete(List<String> rbgaList) {
        int count = 0;
        if (rbgaList == null || rbgaList.isEmpty()) {
            return count;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 PreparedStatement delete = conn.prepareStatement(Delete)) {
            conn.setAutoCommit(false);
            for (String rgba : rbgaList) {
                delete.setString(1, rgba.toUpperCase());
                int ret = delete.executeUpdate();
                if (ret > 0) {
                    count += ret;
                }
            }
            conn.commit();
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
        }
        return count;
    }

    // Palette
    public static List<ColorData> readPalette() {
        List<ColorData> palette = new ArrayList<>();
        String sql = " SELECT * FROM Color_Data WHERE palette_index > -1 ORDER BY palette_index ASC";
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement();
                 ResultSet results = statement.executeQuery(sql)) {
            while (results.next()) {
                ColorData data = read(results);
                palette.add(data);
            }
            if (palette.isEmpty()) {
                palette.add(new ColorData(Color.TRANSPARENT.toString(), message("Transparent")).calculate());
                palette.add(new ColorData(Color.BLACK.toString(), message("Black")).calculate());
                palette.add(new ColorData(Color.WHITE.toString(), message("White")).calculate());
                palette.add(new ColorData(Color.RED.toString(), message("Red")).calculate());
                palette.add(new ColorData(Color.GREEN.toString(), message("Green")).calculate());
                palette.add(new ColorData(Color.BLUE.toString(), message("Blue")).calculate());
                addDataInPalette(conn, palette, true);
            }
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
        }
        return palette;
    }

    public static double maxPaletteIndex(Connection conn) {
        double maxp = 1;
        if (conn == null) {
            return maxp;
        }
        String sql = "SELECT MAX(palette_index) as maxp FROM Color_Data WHERE palette_index > 0";
        try ( Statement statement = conn.createStatement();
                 ResultSet results = statement.executeQuery(sql)) {
            if (results.next()) {
                try {
                    maxp = results.getDouble("maxp") + 1;
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
        }
        return maxp;
    }

    public static double minPaletteIndex(Connection conn) {
        double minp = 1;
        if (conn == null) {
            return minp;
        }
        String sql = "SELECT MIN(palette_index) as minp FROM Color_Data WHERE palette_index > 0";
        try ( Statement statement = conn.createStatement();
                 ResultSet results = statement.executeQuery(sql)) {
            if (results.next()) {
                try {
                    minp = results.getDouble("minp");
                    minp = minp - minp / 1000d;
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
        }
        return minp;
    }

    public static ColorData addInPalette(Connection conn, String rgba, double index, boolean end) {
        if (rgba == null || rgba.isEmpty()) {
            return null;
        }
        try {
            ColorData data = read(conn, rgba);
            if (data != null) {
                if (data.getPaletteIndex() > 0) {
                    return data;
                }
                if (index < 0) {
                    index = end ? maxPaletteIndex(conn) : minPaletteIndex(conn);
                }
                data.setPaletteIndex(index);
                if (update(conn, data)) {
                    return data;
                } else {
                    return null;
                }
            } else {
                data = new ColorData(rgba).calculate();
                if (index < 0) {
                    index = end ? maxPaletteIndex(conn) : minPaletteIndex(conn);
                }
                data.setPaletteIndex(index);
                if (insert(conn, data)) {
                    return data;
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return null;
        }
    }

    public static ColorData endPalette(Connection conn, String rgba, double index) {
        return addInPalette(conn, rgba, index, true);
    }

    public static ColorData endPalette(Connection conn, String rgba) {
        return endPalette(conn, rgba, -1);
    }

    public static ColorData endPalette(String rgba) {
        if (rgba == null || rgba.isEmpty()) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return endPalette(conn, rgba);
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return null;
        }
    }

    public static ColorData endPalette(Color color) {
        if (color == null) {
            return null;
        }
        return endPalette(FxmlColor.color2rgba(color));
    }

    public static ColorData frontPalette(Connection conn, String rgba, double index) {
        return addInPalette(conn, rgba, index, false);
    }

    public static ColorData frontPalette(Connection conn, String rgba) {
        return frontPalette(conn, rgba, -1);
    }

    public static ColorData frontPalette(String rgba) {
        if (rgba == null || rgba.isEmpty()) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return frontPalette(conn, rgba);
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return null;
        }
    }

    public static ColorData frontPalette(Color color) {
        if (color == null) {
            return null;
        }
        return frontPalette(FxmlColor.color2rgba(color));
    }

    public static ColorData addInPalette(Connection conn, ColorData data, double index, boolean end, boolean replace) {
        if (conn == null || data == null) {
            return null;
        }
        try {
            ColorData exist = read(conn, data.getRgba());
            if (exist != null) {
                if (exist.getPaletteIndex() > 0) {
                    if (replace) {
                        data.setPaletteIndex(exist.getPaletteIndex());
                        if (update(conn, data)) {
                            return data;
                        } else {
                            return null;
                        }
                    } else {
                        return exist;
                    }
                } else {
                    if (!replace) {
                        data = exist;
                    }
                    if (index < 0) {
                        index = end ? maxPaletteIndex(conn) : minPaletteIndex(conn);
                    }
                    data.setPaletteIndex(index);
                    if (update(conn, data)) {
                        return data;
                    } else {
                        return null;
                    }
                }

            } else {
                if (index < 0) {
                    index = end ? maxPaletteIndex(conn) : minPaletteIndex(conn);
                }
                data.setPaletteIndex(index);
                if (insert(conn, data)) {
                    return data;
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return null;
        }
    }

    public static ColorData endPalette(Connection conn, ColorData data, double index, boolean replace) {
        return addInPalette(conn, data, index, true, replace);
    }

    public static ColorData endPalette(Connection conn, ColorData data, boolean replace) {
        return endPalette(conn, data, -1, replace);
    }

    public static ColorData endPalette(ColorData data, boolean replace) {
        if (data == null) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return endPalette(conn, data, replace);
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return null;
        }
    }

    public static ColorData frontPalette(Connection conn, ColorData data, double index, boolean replace) {
        return addInPalette(conn, data, index, false, replace);
    }

    public static ColorData frontPalette(Connection conn, ColorData data, boolean replace) {
        return frontPalette(conn, data, -1, replace);
    }

    public static ColorData frontPalette(ColorData data, boolean replace) {
        if (data == null) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return frontPalette(conn, data, replace);
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return null;
        }
    }

    public static boolean addInPalette(List<String> rgbaList, boolean replace) {
        if (rgbaList == null || rgbaList.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setAutoCommit(false);
            double maxp = maxPaletteIndex(conn);
            for (String rgba : rgbaList) {
                endPalette(conn, rgba, maxp++);
            }
            conn.commit();
            trimPalette(conn);
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean addColorsInPalette(List<Color> colors) {
        if (colors == null || colors.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            addColorsInPalette(conn, colors);
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean addColorsInPalette(Connection conn, List<Color> colors) {
        if (conn == null || colors == null || colors.isEmpty()) {
            return false;
        }
        try {
            conn.setAutoCommit(false);
            double maxp = maxPaletteIndex(conn);
            for (Color color : colors) {
                endPalette(conn, FxmlColor.color2rgba(color), maxp++);
            }
            conn.commit();
            trimPalette(conn);
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean addDataInPalette(List<ColorData> dataList, boolean replace) {
        if (dataList == null || dataList.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return addDataInPalette(conn, dataList, replace);
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean addDataInPalette(Connection conn, List<ColorData> dataList, boolean replace) {
        if (conn == null || dataList == null || dataList.isEmpty()) {
            return false;
        }
        try {
            conn.setAutoCommit(false);
            double maxp = maxPaletteIndex(conn);
            for (ColorData data : dataList) {
                endPalette(conn, data, maxp++, replace);
            }
            conn.commit();
            trimPalette(conn);
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean removeFromPalette(String rgba) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 PreparedStatement statement = conn.prepareStatement(RemoveFromPalette)) {
            statement.setString(1, rgba.toUpperCase());
            return statement.executeUpdate() > 0;
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
        return removeFromPalette(FxmlColor.color2rgba(color));
    }

    public static boolean removeFromPalette(List<ColorData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 PreparedStatement statement = conn.prepareStatement(RemoveFromPalette)) {
            conn.setAutoCommit(false);
            for (ColorData data : dataList) {
                statement.setString(1, data.getRgba());
                statement.executeUpdate();
            }
            conn.commit();
            trimPalette(conn);
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
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

    public static boolean setPalette(List<String> rgbaList) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            String sql = "UPDATE Color_Data SET palette_index=-1";
            conn.createStatement().executeUpdate(sql);
            conn.commit();
            if (rgbaList == null || rgbaList.isEmpty()) {
                return true;
            }
            conn.setAutoCommit(false);
            double index = 10;
            for (String rgba : rgbaList) {
                endPalette(conn, rgba, index++);
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean setPaletteColors(List<Color> colors) {
        if (colors == null || colors.isEmpty()) {
            clearPalette();
            return true;
        }
        List<String> rgbList = new ArrayList<>();
        for (Color color : colors) {
            rgbList.add(FxmlColor.color2rgba(color));
        }
        return setPalette(rgbList);
    }

    public static boolean setPaletteColors(List<ColorData> colors, boolean replace) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);) {
            String sql = "UPDATE Color_Data SET palette_index=-1";
            conn.createStatement().executeUpdate(sql);
            conn.commit();
            if (colors == null || colors.isEmpty()) {
                return true;
            }
            conn.setAutoCommit(false);
            double index = 10;
            for (ColorData data : colors) {
                endPalette(conn, data, index++, replace);
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
        if (conn == null) {
            return false;
        }
        try ( Statement statement = conn.createStatement()) {
            conn.setAutoCommit(false);
            String sql = " SELECT * FROM Color_Data WHERE palette_index > 0 ORDER BY palette_index ASC";
            double index = 10;
            try ( ResultSet results = statement.executeQuery(sql)) {
                while (results.next()) {
                    ColorData data = read(results);
                    data.setPaletteIndex(index++);
                    update(conn, data);
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

}
