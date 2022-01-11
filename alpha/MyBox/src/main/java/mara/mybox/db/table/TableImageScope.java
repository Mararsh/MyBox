package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.ImageScope.ColorScopeType;
import mara.mybox.bufferedimage.ImageScope.ScopeType;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import static mara.mybox.db.table.BaseTable.FilenameMaxLength;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2018-10-15 9:31:28
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableImageScope extends BaseTable<ImageScope> {

    public static String DataSeparator = ",";

    public TableImageScope() {
        tableName = "image_scope";
        defineColumns();
    }

    public TableImageScope(boolean defineColumns) {
        tableName = "image_scope";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableImageScope defineColumns() {
        addColumn(new ColumnDefinition("image_location", ColumnDefinition.ColumnType.File, true, true).setLength(FilenameMaxLength));
        addColumn(new ColumnDefinition("name", ColumnDefinition.ColumnType.String, true, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("scope_type", ColumnDefinition.ColumnType.String, true).setLength(128));
        addColumn(new ColumnDefinition("color_scope_type", ColumnDefinition.ColumnType.String, true).setLength(128));
        addColumn(new ColumnDefinition("area_data", ColumnDefinition.ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("color_data", ColumnDefinition.ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("color_distance", ColumnDefinition.ColumnType.Integer, true));
        addColumn(new ColumnDefinition("hsb_distance", ColumnDefinition.ColumnType.Double, true));
        addColumn(new ColumnDefinition("area_excluded", ColumnDefinition.ColumnType.Integer, true));
        addColumn(new ColumnDefinition("color_excluded", ColumnDefinition.ColumnType.Integer, true));
        addColumn(new ColumnDefinition("outline", ColumnDefinition.ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("create_time", ColumnDefinition.ColumnType.Datetime, true));
        addColumn(new ColumnDefinition("modify_time", ColumnDefinition.ColumnType.Datetime, true));
        orderColumns = "modify_time DESC";
        return this;
    }

    public static final String Delete
            = "DELETE FROM image_scope WHERE image_location=? AND name=?";

    @Override
    public ImageScope readData(ResultSet results) {
        return decode(results);
    }

    public static List<ImageScope> read(String imageLocation) {
        List<ImageScope> records = new ArrayList<>();
        if (imageLocation == null || imageLocation.trim().isEmpty()) {
            return records;
        }
        try ( Connection conn = DerbyBase.getConnection();
                 Statement statement = conn.createStatement()) {
            conn.setReadOnly(true);
            String sql = " SELECT * FROM image_scope WHERE image_location='" + imageLocation + "' ORDER BY modify_time DESC";
            try ( ResultSet results = statement.executeQuery(sql)) {
                while (results.next()) {
                    ImageScope scope = decode(results);
                    if (scope != null) {
                        records.add(scope);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return records;
    }

    public static ImageScope read(ImageScope scope) {
        if (scope == null) {
            return null;
        }
        return read(scope.getFile(), scope.getName());
    }

    public static ImageScope read(String imageLocation, String name) {
        if (imageLocation == null || name == null) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection();
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM image_scope WHERE image_location='" + imageLocation
                    + "' AND name='" + DerbyBase.stringValue(name) + "'";
            try ( ResultSet results = statement.executeQuery(sql)) {
                if (results.next()) {
                    return decode(results);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }

    public static ImageScope decode(ResultSet results) {
        if (results == null) {
            return null;
        }
        ImageScope scope = new ImageScope();
        try {
            ScopeType type = ScopeType.valueOf(results.getString("scope_type"));
            if (decodeAreaData(type, results, scope)
                    && decodeColorData(type, results, scope)
                    && decodeOutline(type, results, scope)) {
                scope.setFile(results.getString("image_location"));
                scope.setName(results.getString("name"));
                scope.setScopeType(type);
                scope.setColorScopeType(ColorScopeType.valueOf(results.getString("color_scope_type")));
                scope.setColorDistance(results.getInt("color_distance"));
                scope.setHsbDistance((float) results.getDouble("hsb_distance"));
                scope.setAreaExcluded(results.getBoolean("area_excluded"));
                scope.setColorExcluded(results.getBoolean("color_excluded"));
                scope.setCreateTime(results.getTimestamp("create_time"));
                scope.setModifyTime(results.getTimestamp("modify_time"));
            } else {
                scope = null;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            scope = null;
        }
        return scope;
    }

    public static boolean decodeAreaData(ScopeType type, ResultSet results, ImageScope scope) {
        if (type == null || results == null || scope == null) {
            return false;
        }
        try {
            return ImageScope.decodeAreaData(type, results.getString("area_data"), scope);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean decodeColorData(ScopeType type, ResultSet results, ImageScope scope) {
        if (type == null || results == null || scope == null) {
            return false;
        }
        try {
            return ImageScope.decodeColorData(type, results.getString("color_data"), scope);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean decodeOutline(ScopeType type, ResultSet results, ImageScope scope) {
        if (type == null || results == null || scope == null) {
            return false;
        }
        if (type != ScopeType.Outline) {
            return true;
        }
        try {
            return ImageScope.decodeOutline(type, results.getString("outline"), scope);
        } catch (Exception e) {
            MyBoxLog.error(e);
//            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static int write(ImageScope scope) {
        if (scope == null || scope.getFile() == null || scope.getName() == null) {
            return -1;
        }
        int count = 0;
        try ( Connection conn = DerbyBase.getConnection();
                 Statement statement = conn.createStatement()) {
            String areaData = ImageScope.encodeAreaData(scope);
            String colorData = ImageScope.encodeColorData(scope);
            String outline = ImageScope.encodeOutline(scope);
            String sql = " SELECT * FROM image_scope WHERE image_location='" + scope.getFile()
                    + "' AND name='" + DerbyBase.stringValue(scope.getName()) + "'";
            boolean exist = false;
            try ( ResultSet results = statement.executeQuery(sql)) {
                if (results.next()) {
                    exist = true;
                }
            }
            if (exist) {
                sql = "UPDATE image_scope SET scope_type='" + scope.getScopeType().name()
                        + "' , color_scope_type='" + scope.getColorScopeType().name()
                        + "' , area_data='" + areaData
                        + "' , color_data='" + colorData
                        + "' , color_distance=" + scope.getColorDistance()
                        + " , hsb_distance=" + scope.getHsbDistance()
                        + " , area_excluded=" + (scope.isAreaExcluded() ? 1 : 0)
                        + " , color_excluded=" + (scope.isColorExcluded() ? 1 : 0)
                        + " , outline='" + outline
                        + "' , create_time='" + DateTools.datetimeToString(scope.getCreateTime())
                        + "' , modify_time='" + DateTools.datetimeToString(new Date())
                        + "' WHERE image_location='" + scope.getFile() + "'"
                        + " AND name='" + DerbyBase.stringValue(scope.getName()) + "'";

            } else {
                sql = "INSERT INTO image_scope(image_location, name , scope_type, color_scope_type, "
                        + " area_data, color_data, color_distance, hsb_distance, area_excluded, "
                        + " color_excluded, outline, create_time, modify_time) VALUES('"
                        + scope.getFile() + "', '" + DerbyBase.stringValue(scope.getName()) + "', '" + scope.getScopeType().name()
                        + "', '" + scope.getColorScopeType().name() + "', '" + areaData + "', '" + colorData + "', "
                        + scope.getColorDistance() + ", " + scope.getHsbDistance() + ", "
                        + (scope.isAreaExcluded() ? 1 : 0) + ", " + (scope.isColorExcluded() ? 1 : 0) + ", '"
                        + outline + "', '"
                        + DateTools.datetimeToString(new Date()) + "', '"
                        + DateTools.datetimeToString(new Date()) + "')";
            }
            count = statement.executeUpdate(sql);
        } catch (Exception e) {
            MyBoxLog.error(e);
            count = -1;
        }
        return count;
    }

}
