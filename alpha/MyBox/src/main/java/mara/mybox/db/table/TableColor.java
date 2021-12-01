package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2019-9-28
 * @License Apache License Version 2.0
 */
public class TableColor extends BaseTable<ColorData> {

    public TableColor() {
        tableName = "Color";
        defineColumns();
    }

    public TableColor(boolean defineColumns) {
        tableName = "Color";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableColor defineColumns() {
        addColumn(new ColumnDefinition("color_value", ColumnDefinition.ColumnType.Integer, true, true));
        addColumn(new ColumnDefinition("rgba", ColumnDefinition.ColumnType.String, true).setLength(16));
        addColumn(new ColumnDefinition("color_name", ColumnDefinition.ColumnType.String).setLength(1024));
        addColumn(new ColumnDefinition("rgb", ColumnDefinition.ColumnType.String, true).setLength(16));
        addColumn(new ColumnDefinition("srgb", ColumnDefinition.ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("hsb", ColumnDefinition.ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("adobeRGB", ColumnDefinition.ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("appleRGB", ColumnDefinition.ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("eciRGB", ColumnDefinition.ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("sRGBLinear", ColumnDefinition.ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("adobeRGBLinear", ColumnDefinition.ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("appleRGBLinear", ColumnDefinition.ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("calculatedCMYK", ColumnDefinition.ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("eciCMYK", ColumnDefinition.ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("adobeCMYK", ColumnDefinition.ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("xyz", ColumnDefinition.ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("cieLab", ColumnDefinition.ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("lchab", ColumnDefinition.ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("cieLuv", ColumnDefinition.ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("lchuv", ColumnDefinition.ColumnType.String).setLength(128));
        orderColumns = "color_value DESC";
        return this;
    }

    public static final String Create_RGBA_Unique_Index
            = "CREATE UNIQUE INDEX Color_rgba_unique_index on Color ( rgba )";

    public static final String QueryRGBA
            = "SELECT * FROM Color WHERE rgba=?";

    public static final String QueryValue
            = "SELECT * FROM Color WHERE color_value=?";

    public static final String Delete
            = "DELETE FROM Color WHERE color_value=?";

    public ColorData read(int value) {
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return read(conn, value);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }

    public ColorData read(Connection conn, int value) {
        if (conn == null) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(QueryValue)) {
            statement.setInt(1, value);
            statement.setMaxRows(1);
            try ( ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    ColorData data = readData(results);
                    return data;
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }

    public ColorData read(String web) {
        try {
            int value = FxColorTools.web2Value(web);
            if (value == AppValues.InvalidInteger) {
                return null;
            }
            return read(value);
        } catch (Exception e) {
            return null;
        }
    }

    public ColorData read(Connection conn, String web) {
        try {
            int value = FxColorTools.web2Value(web);
            if (value == AppValues.InvalidInteger) {
                return null;
            }
            return read(conn, value);
        } catch (Exception e) {
            return null;
        }
    }

    public ColorData read(Color color) {
        if (color == null) {
            return null;
        }
        return read(FxColorTools.color2rgba(color));
    }

    public ColorData findAndCreate(int value, String name) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return findAndCreate(conn, value, name);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public ColorData findAndCreate(Connection conn, int value, String name) {
        try {
            boolean ac = conn.getAutoCommit();
            conn.setAutoCommit(true);
            ColorData data = read(conn, value);
            if (data == null) {
                data = new ColorData(value).calculate().setColorName(name);
                insertData(conn, data);
            } else if (name != null && !name.equals(data.getColorName())) {
                data.setColorName(name);
                updateData(conn, data.calculate());
            }
            conn.setAutoCommit(ac);
            return data;
        } catch (Exception e) {
            MyBoxLog.error(e, value + "");
            return null;
        }
    }

    public ColorData findAndCreate(String web, String name) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return findAndCreate(conn, web, name);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public ColorData findAndCreate(Connection conn, String web, String name) {
        try {
            int value = FxColorTools.web2Value(web);
            if (value == AppValues.InvalidInteger) {
                return null;
            }
            return findAndCreate(conn, value, name);
        } catch (Exception e) {
            MyBoxLog.error(e, web);
            return null;
        }
    }

    public ColorData write(String rgba, boolean replace) {
        try ( Connection conn = DerbyBase.getConnection();) {
            return write(conn, rgba, null, replace);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public ColorData write(Connection conn, String rgba, String name, boolean replace) {
        if (conn == null || rgba == null) {
            return null;
        }
        ColorData exist = read(conn, rgba);
        if (exist != null) {
            if (replace) {
                ColorData data = new ColorData(rgba, name).calculate();
                return updateData(conn, data);
            } else {
                return exist;
            }
        } else {
            ColorData data = new ColorData(rgba, name).calculate();
            return insertData(conn, data);
        }
    }

    public ColorData write(Connection conn, String rgba, boolean replace) {
        return write(conn, rgba, null, replace);
    }

    public ColorData write(Color color, boolean replace) {
        try {
            return write(new ColorData(color), replace);
        } catch (Exception e) {
            return null;
        }
    }

    public ColorData write(ColorData data, boolean replace) {
        if (data == null) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection();) {
            return write(conn, data, replace);
        } catch (Exception e) {
            MyBoxLog.error(e);

            return null;
        }
    }

    public ColorData write(Connection conn, ColorData data, boolean replace) {
        if (conn == null || data == null) {
            return null;
        }
        ColorData exist = read(conn, data.getColorValue());
        if (exist != null) {
            if (replace) {
                return updateData(conn, data.calculate());
            } else {
                return exist;
            }
        } else {
            return insertData(conn, data.calculate());
        }
    }

    public List<ColorData> writeColors(List<Color> colors, boolean replace) {
        if (colors == null || colors.isEmpty()) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return writeColors(conn, colors, replace);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<ColorData> writeColors(Connection conn, List<Color> colors, boolean replace) {
        if (conn == null || colors == null || colors.isEmpty()) {
            return null;
        }
        List<ColorData> updateList = new ArrayList<>();
        try {
            boolean ac = conn.getAutoCommit();
            conn.setAutoCommit(false);
            for (Color color : colors) {
                ColorData data = new ColorData(color);
                data = write(conn, data, replace);
                if (data != null) {
                    updateList.add(data);
                }
            }
            conn.commit();
            conn.setAutoCommit(ac);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return updateList;
    }

    public List<ColorData> writeData(List<ColorData> dataList, boolean replace) {
        if (dataList == null) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection();) {
            return writeData(conn, dataList, replace);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<ColorData> writeData(Connection conn, List<ColorData> dataList, boolean replace) {
        if (conn == null || dataList == null) {
            return null;
        }
        List<ColorData> updateList = new ArrayList<>();
        try {
            boolean ac = conn.getAutoCommit();
            conn.setAutoCommit(false);
            for (ColorData data : dataList) {
                ColorData updated = write(conn, data, replace);
                if (updated != null) {
                    updateList.add(updated);
                }
            }
            conn.commit();
            conn.setAutoCommit(ac);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return updateList;
    }

    public boolean setName(Color color, String name) {
        if (color == null || name == null) {
            return false;
        }
        return setName(FxColorTools.color2Value(color), name);
    }

    public boolean setName(String rgba, String name) {
        if (name == null || rgba == null) {
            return false;
        }
        return setName(Color.web(rgba), name);
    }

    public boolean setName(int value, String name) {
        if (name == null) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection();
                 Statement statement = conn.createStatement()) {
            ColorData exist = read(conn, value);
            if (exist != null) {
                String sql = "UPDATE Color SET "
                        + " color_name='" + DerbyBase.stringValue(name) + "' "
                        + " WHERE color_value=" + value;
                return statement.executeUpdate(sql) > 0;
            } else {
                ColorData data = new ColorData(value).calculate();
                data.setColorName(name);
                return insertData(conn, data) != null;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);

            return false;
        }
    }

    /*
        static methods
     */
    public static boolean delete(String web) {
        if (web == null) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection();) {
            return delete(conn, web);
        } catch (Exception e) {
            MyBoxLog.error(e);

            return false;
        }
    }

    public static boolean delete(Connection conn, String web) {
        if (conn == null || web == null) {
            return false;
        }
        try ( PreparedStatement delete = conn.prepareStatement(Delete)) {
            int value = FxColorTools.web2Value(web);
            if (value == AppValues.InvalidInteger) {
                return false;
            }
            delete.setInt(1, value);
            return delete.executeUpdate() >= 0;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static int delete(List<String> webList) {
        int count = 0;
        if (webList == null || webList.isEmpty()) {
            return count;
        }
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement delete = conn.prepareStatement(Delete)) {
            boolean ac = conn.getAutoCommit();
            conn.setAutoCommit(false);
            for (String web : webList) {
                int value = FxColorTools.web2Value(web);
                if (value == AppValues.InvalidInteger) {
                    continue;
                }
                delete.setInt(1, value);
                int ret = delete.executeUpdate();
                if (ret > 0) {
                    count += ret;
                }
            }
            conn.commit();
            conn.setAutoCommit(ac);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return count;
    }

}
