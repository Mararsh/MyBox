package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.data.ColorPalette;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-3-31
 * @License Apache License Version 2.0
 */
public class TableColorPalette extends BaseTable<ColorPalette> {

    protected TableColor tableColor;

    public TableColorPalette() {
        tableName = "Color_Palette";
        defineColumns();
    }

    public TableColorPalette(boolean defineColumns) {
        tableName = "Color_Palette";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableColorPalette defineColumns() {
        addColumn(new ColumnDefinition("cpid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("name_in_palette", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("order_number", ColumnType.Float));
        addColumn(new ColumnDefinition("paletteid", ColumnType.Long, true)
                .setReferName("Color_Palette_palette_fk").setReferTable("Color_Palette_Name").setReferColumn("cpnid")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        addColumn(new ColumnDefinition("cvalue", ColumnType.Integer, true)
                .setReferName("Color_Palette_color_fk").setReferTable("Color").setReferColumn("color_value")
                .setOnDelete(ColumnDefinition.OnDelete.Cascade)
        );
        return this;
    }

    public static final String Create_Unique_Index
            = "CREATE UNIQUE INDEX Color_Palette_unique_index on Color_Palette ( paletteid, cvalue )";

    public static final String CreateView
            = " CREATE VIEW Color_Palette_View AS "
            + " SELECT Color_Palette.*, Color.* "
            + " FROM Color_Palette JOIN Color ON Color_Palette.cvalue=Color.color_value";

    public static final String QueryPaletteColor
            = "SELECT * FROM Color_Palette WHERE paletteid=? AND cvalue=?";

    public static final String QueryPalette
            = "SELECT * FROM Color_Palette_View WHERE paletteid=? ORDER BY order_number";

    public static final String MaxOrder
            = "SELECT max(order_number) FROM Color_Palette_View WHERE paletteid=?";

    public static final String DeletePaletteColor
            = "DELETE FROM Color_Palette WHERE paletteid=? AND cvalue=?";

    public static final String ClearPalette
            = "DELETE FROM Color_Palette WHERE paletteid=?";

    @Override
    public Object readForeignValue(ResultSet results, String column) {
        if (results == null || column == null) {
            return null;
        }
        try {
            if ("cvalue".equals(column) && results.findColumn("color_value") > 0) {
                return getTableColor().readData(results);
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public boolean setForeignValue(ColorPalette data, String column, Object value) {
        if (data == null || column == null || value == null) {
            return true;
        }
        if ("cvalue".equals(column) && value instanceof ColorData) {
            data.setData((ColorData) value);
        }
        return true;
    }

    public ColorPalette find(long paletteid, int color) {
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return find(conn, paletteid, color);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }

    public ColorPalette find(Connection conn, long paletteid, int color) {
        if (conn == null) {
            return null;
        }
        ColorPalette data = null;
        try ( PreparedStatement statement = conn.prepareStatement(QueryPaletteColor)) {
            data = find(statement, paletteid, color);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return data;
    }

    public ColorPalette find(PreparedStatement statement, long paletteid, int color) {
        if (statement == null) {
            return null;
        }
        ColorPalette data = null;
        try {
            statement.setLong(1, paletteid);
            statement.setInt(2, color);
            statement.setMaxRows(1);
            try ( ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    data = readData(results);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return data;
    }

    public ColorPalette findAndCreate(long paletteid, int color) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return findAndCreate(conn, paletteid, color);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public ColorPalette findAndCreate(Connection conn, long paletteid, int color) {
        return findAndCreate(conn, paletteid, new ColorData(color), false);
    }

    public ColorPalette findAndCreate(long paletteid, ColorData color, boolean keepOrder) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return findAndCreate(conn, paletteid, color, keepOrder);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public ColorPalette findAndCreate(Connection conn, long paletteid, ColorData color, boolean keepOrder) {
        if (conn == null || color == null) {
            return null;
        }
        try {
            int colorValue = color.getColorValue();
            ColorPalette colorPalette = find(conn, paletteid, colorValue);
            if (colorPalette == null) {
//                ColorData existColor = getTableColor().findAndCreate(conn, colorValue, color.getColorName());
//                if (existColor == null) {
//                    return null;
//                }
                float order = keepOrder ? color.getOrderNumner() : Float.MAX_VALUE;
                order = order == Float.MAX_VALUE ? max(conn, paletteid) + 1 : order;
                colorPalette = new ColorPalette()
                        .setData(color).setName(color.getColorName()).setColorValue(colorValue)
                        .setPaletteid(paletteid).setOrderNumber(order);
                insertData(conn, colorPalette);
            }
            return colorPalette;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<ColorData> colors(long paletteid, long start, long size) {
        if (start < 0 || size <= 0) {
            return new ArrayList<>();
        }
        String condition = " WHERE paletteid=" + paletteid + " ORDER BY order_number "
                + " OFFSET " + start + " ROWS FETCH NEXT " + size + " ROWS ONLY";
        return colors(condition);
    }

    public List<ColorData> colors(long paletteid) {
        if (paletteid < 0) {
            return new ArrayList<>();
        }
        String condition = " WHERE paletteid=" + paletteid + " ORDER BY order_number ";
        return colors(condition);
    }

    public List<ColorData> colors(String condition) {
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return colors(conn, condition);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<ColorData> colors(Connection conn, long paletteid) {
        if (paletteid < 0) {
            return new ArrayList<>();
        }
        String condition = " WHERE paletteid=" + paletteid + " ORDER BY order_number ";
        return colors(conn, condition);
    }

    public List<ColorData> colors(Connection conn, String condition) {
        List<ColorData> colors = new ArrayList<>();
        if (conn == null) {
            return colors;
        }
        String sql = "SELECT * FROM Color_Palette_View "
                + (condition == null || condition.isBlank() ? "" : condition);
        try ( PreparedStatement statement = conn.prepareStatement(sql)) {
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    ColorPalette data = readData(results);
                    ColorData color = data.getData();
                    color.setColorName(data.getName());
                    color.setOrderNumner(data.getOrderNumber());
                    color.setPaletteid(data.getCpid());
                    colors.add(color);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e, sql);
        }
        return colors;

    }

    public ColorPalette setOrder(long paletteid, ColorData color, float orderNumber) {
        if (color == null) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return setOrder(conn, paletteid, color, orderNumber);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public ColorPalette setOrder(Connection conn, long paletteid, ColorData color, float orderNumber) {
        if (conn == null || color == null) {
            return null;
        }
        try {
            int colorValue = color.getColorValue();
            ColorPalette data = find(conn, paletteid, colorValue);
            if (data != null) {
                data.setOrderNumber(orderNumber);
                data = updateData(conn, data);
            } else {
//                ColorData existColor = getTableColor().findAndCreate(conn, colorValue, color.getColorName());
//                if (existColor == null) {
//                    return null;
//                }
                data = new ColorPalette()
                        .setData(color).setName(color.getColorName()).setColorValue(colorValue)
                        .setPaletteid(paletteid).setOrderNumber(orderNumber);
                data = insertData(conn, data);
            }
            conn.commit();
            return data;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public ColorPalette setName(long paletteid, ColorData color, String name) {
        if (color == null) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return setName(conn, paletteid, color, name);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public ColorPalette setName(Connection conn, long paletteid, ColorData color, String name) {
        if (conn == null || color == null) {
            return null;
        }
        try {
            int colorValue = color.getColorValue();
            ColorPalette data = find(conn, paletteid, colorValue);
            if (data != null) {
                data.setName(name);
                data = updateData(conn, data);
            } else {
                data = new ColorPalette()
                        .setData(color).setName(name).setColorValue(colorValue)
                        .setPaletteid(paletteid).setOrderNumber(max(conn, paletteid) + 1);
                data = insertData(conn, data);
            }
            conn.commit();
            return data;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<ColorPalette> write(long paletteid, List<ColorData> colors, boolean keepOrder) {
        if (colors == null || colors.isEmpty()) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return write(conn, paletteid, colors, keepOrder);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<ColorPalette> write(Connection conn, long paletteid, List<ColorData> colors, boolean keepOrder) {
        if (conn == null || colors == null || colors.isEmpty()) {
            return null;
        }
        List<ColorPalette> cpList = new ArrayList<>();
        try ( PreparedStatement statement = conn.prepareStatement(QueryPaletteColor)) {
            conn.setAutoCommit(false);
            for (ColorData color : colors) {
                ColorPalette cp = write(conn, statement, paletteid, color, keepOrder);
                if (cp != null) {
                    cpList.add(cp);
                }
            }
            conn.commit();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return cpList;
    }

    public ColorPalette write(Connection conn, PreparedStatement statement,
            long paletteid, ColorData color, boolean keepOrder) {
        if (conn == null || color == null) {
            return null;
        }
        try {
            int colorValue = color.getColorValue();
            ColorPalette colorPalette = find(statement, paletteid, colorValue);
            float order = keepOrder ? color.getOrderNumner() : Float.MAX_VALUE;
            order = order == Float.MAX_VALUE ? max(conn, paletteid) + 1 : order;
            if (colorPalette == null) {
//                ColorData existColor = getTableColor().findAndCreate(conn, colorValue, color.getColorName());
//                if (existColor == null) {
//                    return null;
//                }
                colorPalette = new ColorPalette()
                        .setData(color).setName(color.getColorName()).setColorValue(colorValue)
                        .setPaletteid(paletteid).setOrderNumber(order);
                return insertData(conn, colorPalette);
            } else {
                colorPalette.setData(color).setName(color.getColorName())
                        .setOrderNumber(order);
                return updateData(conn, colorPalette);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<ColorPalette> writeColors(long paletteid, List<Color> colors) {
        if (colors == null || colors.isEmpty()) {
            return null;
        }
        List<ColorData> data = new ArrayList<>();
        for (Color color : colors) {
            data.add(new ColorData(color));
        }
        return write(paletteid, data, false);
    }

    public int delete(long paletteid, ColorData color) {
        if (color == null) {
            return -1;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return delete(conn, paletteid, color);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public int delete(Connection conn, long paletteid, ColorData color) {
        if (conn == null || color == null) {
            return -1;
        }
        int count = 0;
        try ( PreparedStatement statement = conn.prepareStatement(DeletePaletteColor)) {
            statement.setLong(1, paletteid);
            statement.setInt(2, color.getColorValue());
            count = statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return count;
    }

    public int delete(long paletteid, List<ColorData> colors) {
        if (colors == null || colors.isEmpty()) {
            return -1;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return delete(conn, paletteid, colors);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public int delete(Connection conn, long paletteid, List<ColorData> colors) {
        if (conn == null || colors == null || colors.isEmpty()) {
            return -1;
        }
        int count = 0;
        try ( PreparedStatement statement = conn.prepareStatement(DeletePaletteColor)) {
            conn.setAutoCommit(false);
            for (ColorData color : colors) {
                statement.setLong(1, paletteid);
                statement.setInt(2, color.getColorValue());
                count += statement.executeUpdate();
            }
            conn.commit();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return count;
    }

    public int clear(long paletteid) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return clear(conn, paletteid);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public int clear(Connection conn, long paletteid) {
        if (conn == null) {
            return -1;
        }
        int count = 0;
        try ( PreparedStatement statement = conn.prepareStatement(ClearPalette)) {
            statement.setLong(1, paletteid);
            count = statement.executeUpdate();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return count;
    }

    public boolean trim(long paletteid) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return trim(conn, paletteid);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean trim(Connection conn, long paletteid) {
        if (paletteid < 0) {
            return false;
        }
        try ( PreparedStatement statement = conn.prepareStatement(QueryPalette)) {
            statement.setLong(1, paletteid);
            float order = 1f;
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    ColorPalette data = readData(results);
                    data.setOrderNumber(order);
                    updateData(conn, data);
                    order += 1;
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
        return true;
    }

    public int size(long paletteid) {
        return conditionSize("paletteid=" + paletteid);
    }

    /*
        static methods
     */
    public static float max(Connection conn, long paletteid) {
        try ( PreparedStatement statement = conn.prepareStatement(MaxOrder)) {
            statement.setLong(1, paletteid);
            try ( ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getFloat(1);
                }
            }
        } catch (Exception e) {
        }
        return 1f;
    }

    /*
        get/set
     */
    public TableColor getTableColor() {
        if (tableColor == null) {
            tableColor = new TableColor();
        }
        return tableColor;
    }

    public void setTableColor(TableColor tableColor) {
        this.tableColor = tableColor;
    }

}
