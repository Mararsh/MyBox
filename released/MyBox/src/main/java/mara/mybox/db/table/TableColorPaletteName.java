package mara.mybox.db.table;

import mara.mybox.db.data.ColumnDefinition;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.scene.paint.Color;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.data.ColorPalette;
import mara.mybox.db.data.ColorPaletteName;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-3-31
 * @License Apache License Version 2.0
 */
public class TableColorPaletteName extends BaseTable<ColorPaletteName> {

    public final static String DefaultPalette = Languages.message("DefaultPalette");
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
        addColumn(new ColumnDefinition("cpnid", ColumnType.Long, true, true).setIsID(true));
        addColumn(new ColumnDefinition("palette_name", ColumnType.String, true).setLength(4096));
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
        try ( Connection conn = DerbyBase.getConnection()) {
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
        try ( PreparedStatement statement = conn.prepareStatement(QueryName)) {
            statement.setString(1, name);
            statement.setMaxRows(1);
            try ( ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return readData(results);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }

    public ColorPaletteName findAndCreate(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
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

    public ColorPaletteName defaultPalette() {
        try ( Connection conn = DerbyBase.getConnection()) {
            return defaultPalette(conn);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public ColorPaletteName defaultPalette(Connection conn) {
        try {
            ColorPaletteName palette = findAndCreate(conn, DefaultPalette);
            long paletteid = palette.getCpnid();
            if (getTableColorPalette().size(paletteid) == 0) {
                insert(conn, paletteid, FxColorTools.color2rgba(Color.WHITE), Languages.message("White"), 1f);
                insert(conn, paletteid, FxColorTools.color2rgba(Color.BLACK), Languages.message("Black"), 2f);
                insert(conn, paletteid, FxColorTools.color2rgba(Color.RED), Languages.message("Red"), 3f);
                insert(conn, paletteid, FxColorTools.color2rgba(Color.GREEN), Languages.message("Green"), 4f);
                insert(conn, paletteid, FxColorTools.color2rgba(Color.BLUE), Languages.message("Blue"), 5f);
                insert(conn, paletteid, FxColorTools.color2rgba(Color.YELLOW), Languages.message("Yellow"), 6f);
                insert(conn, paletteid, FxColorTools.color2rgba(Color.PURPLE), Languages.message("Purple"), 6f);
                insert(conn, paletteid, FxColorTools.color2rgba(Color.CYAN), Languages.message("Cyan"), 6f);
                insert(conn, paletteid, FxColorTools.color2rgba(Color.TRANSPARENT), Languages.message("Transparent"), 7f);
            }
            conn.commit();
            return palette;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public ColorPalette insert(Connection conn, long paletteid, String rgba, String name, float orderNumber) {
        if (conn == null) {
            return null;
        }
        try {
            ColorData color = getTableColor().findAndCreate(conn, rgba, name);
            ColorPalette colorPalette = new ColorPalette()
                    .setData(color).setName(color.getColorName())
                    .setColorValue(color.getColorValue())
                    .setPaletteid(paletteid).setOrderNumber(orderNumber);
            colorPalette = getTableColorPalette().insertData(conn, colorPalette);
            return colorPalette;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
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
