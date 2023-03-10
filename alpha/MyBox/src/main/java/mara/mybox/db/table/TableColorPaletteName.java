package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColorPaletteName;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-3-31
 * @License Apache License Version 2.0
 */
public class TableColorPaletteName extends BaseTable<ColorPaletteName> {

    protected TableColor tableColor;
    protected TableColorPalette tableColorPalette;

    public TableColorPaletteName() {
        tableName = "Color_Palette_Name";
        defineColumns();
    }

    public TableColorPaletteName(boolean defineColumns) {
        tableName = "Color_Palette_Name";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableColorPaletteName defineColumns() {
        addColumn(new ColumnDefinition("cpnid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("palette_name", ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("visit_time", ColumnType.Datetime, true));
        orderColumns = "visit_time DESC";
        return this;
    }

    public static final String Create_Unique_Index
            = "CREATE UNIQUE INDEX Color_Palette_Name_unique_index on Color_Palette_Name ( palette_name )";

    public static final String QueryName
            = "SELECT * FROM Color_Palette_Name WHERE palette_name=?";

    public ColorPaletteName find(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        ColorPaletteName colorPaletteName = null;
        try (Connection conn = DerbyBase.getConnection()) {
            colorPaletteName = find(conn, name);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return colorPaletteName;
    }

    public ColorPaletteName find(Connection conn, String name) {
        if (conn == null || name == null || name.isBlank()) {
            return null;
        }
        ColorPaletteName palette = null;
        try (PreparedStatement statement = conn.prepareStatement(QueryName)) {
            statement.setString(1, name);
            statement.setMaxRows(1);
            conn.setAutoCommit(true);
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    palette = readData(results);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        if (palette != null) {
            try {
                palette.setVisitTime(new Date());
                updateData(conn, palette);
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
        }
        return palette;
    }

    public ColorPaletteName findAndCreate(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            return findAndCreate(conn, name);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public ColorPaletteName findAndCreate(Connection conn, String name) {
        if (conn == null || name == null || name.isBlank()) {
            return null;
        }
        try {
            ColorPaletteName colorPaletteName = find(conn, name);
            if (colorPaletteName == null) {
                colorPaletteName = new ColorPaletteName(name);
                colorPaletteName = insertData(conn, colorPaletteName);
                conn.commit();
            }
            return colorPaletteName;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<ColorPaletteName> recentVisited(Connection conn) {
        return query(conn, queryAllStatement(), 10);
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

    public void setTableDataset(TableColor tableColor) {
        this.tableColor = tableColor;
    }

    public TableColorPalette getTableColorPalette() {
        if (tableColorPalette == null) {
            tableColorPalette = new TableColorPalette();
        }
        return tableColorPalette;
    }

    public void setTableColorPalette(TableColorPalette tableColorPalette) {
        this.tableColorPalette = tableColorPalette;
    }

}
