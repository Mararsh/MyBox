package mara.mybox.db;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.IntPoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.image.ImageScope;
import mara.mybox.image.ImageScope.ColorScopeType;
import mara.mybox.image.ImageScope.ScopeType;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2018-10-15 9:31:28
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableImageScope extends DerbyBase {

    public static String DataSeparator = ",";

    public TableImageScope() {
        Table_Name = "image_scope";
        Keys = new ArrayList<>() {
            {
                add("image_location");
                add("name");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE image_scope ( "
                + "  image_location  VARCHAR(1024) NOT NULL, "
                + "  name  VARCHAR(1024) NOT NULL, "
                + "  scope_type VARCHAR(128) NOT NULL, "
                + "  color_scope_type VARCHAR(128) NOT NULL, "
                + "  area_data VARCHAR(32672) NOT NULL, "
                + "  color_data VARCHAR(32672) NOT NULL, "
                + "  color_distance INTEGER NOT NULL, "
                + "  hsb_distance DOUBLE NOT NULL, "
                + "  area_excluded INTEGER NOT NULL, "
                + "  color_excluded INTEGER NOT NULL, "
                + "  outline VARCHAR(1024) , "
                + "  create_time TIMESTAMP NOT NULL, "
                + "  modify_time TIMESTAMP NOT NULL, "
                + "  PRIMARY KEY (image_location, name)"
                + " )";
    }

    public static final String Delete
            = "DELETE FROM image_scope WHERE image_location=? AND name=?";

    public static List<ImageScope> read(String imageLocation) {
        List<ImageScope> records = new ArrayList<>();
        if (imageLocation == null || imageLocation.trim().isEmpty()) {
            return records;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM image_scope WHERE image_location='" + imageLocation
                    + "' AND name='" + stringValue(name) + "'";
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
            String areaData = results.getString("area_data");
            switch (type) {
                case Matting: {
                    String[] items = areaData.split(DataSeparator);
                    for (int i = 0; i < items.length / 2; ++i) {
                        int x = (int) Double.parseDouble(items[i * 2]);
                        int y = (int) Double.parseDouble(items[i * 2 + 1]);
                        scope.addPoint(x, y);
                    }
                }
                break;
                case Rectangle:
                case RectangleColor:
                case Outline: {
                    String[] items = areaData.split(DataSeparator);
                    if (items.length == 4) {
                        DoubleRectangle rect = new DoubleRectangle(
                                Double.parseDouble(items[0]), Double.parseDouble(items[1]),
                                Double.parseDouble(items[2]), Double.parseDouble(items[3])
                        );
                        scope.setRectangle(rect);
                    } else {
                        return false;
                    }
                }
                break;
                case Circle:
                case CircleColor: {
                    String[] items = areaData.split(DataSeparator);
                    if (items.length == 3) {
                        DoubleCircle circle = new DoubleCircle(
                                Double.parseDouble(items[0]), Double.parseDouble(items[1]),
                                Double.parseDouble(items[2])
                        );
                        scope.setCircle(circle);
                    } else {
                        return false;
                    }
                }
                break;
                case Ellipse:
                case EllipseColor: {
                    String[] items = areaData.split(DataSeparator);
                    if (items.length == 4) {
                        DoubleEllipse ellipse = new DoubleEllipse(
                                Double.parseDouble(items[0]), Double.parseDouble(items[1]),
                                Double.parseDouble(items[2]), Double.parseDouble(items[3])
                        );
                        scope.setEllipse(ellipse);
                    } else {
                        return false;
                    }
                }
                break;
                case Polygon:
                case PolygonColor: {
                    String[] items = areaData.split(DataSeparator);
                    DoublePolygon polygon = new DoublePolygon();
                    for (int i = 0; i < items.length / 2; ++i) {
                        int x = (int) Double.parseDouble(items[i * 2]);
                        int y = (int) Double.parseDouble(items[i * 2 + 1]);
                        polygon.add(x, y);
                    }
                    scope.setPolygon(polygon);
                }
                break;

            }
            return true;
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
            String colorData = results.getString("color_data");
            switch (type) {
                case Color:
                case RectangleColor:
                case CircleColor:
                case EllipseColor:
                case PolygonColor: {
                    String[] items = colorData.split(DataSeparator);
                    List<Color> colors = new ArrayList<>();
                    for (String item : items) {
                        try {
                            colors.add(new Color((int) Double.parseDouble(item)));
                        } catch (Exception e) {
                            MyBoxLog.error(e);
                        }
                    }
                    scope.setColors(colors);
                }
            }
            return true;
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
            String outline = results.getString("outline");
            if (outline == null || outline.trim().isEmpty()) {
                return false;
            }
            BufferedImage image = ImageFileReaders.readImage(new File(outline));
            scope.setOutlineSource(image);
            return image != null;
        } catch (Exception e) {
            MyBoxLog.error(e);
//            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static List<ImageScope> write(ImageScope scope) {
        if (scope == null || scope.getFile() == null || scope.getName() == null) {
            return new ArrayList<>();
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String areaData = encodeAreaData(scope);
            String colorData = encodeColorData(scope);
            String outline = encodeOutline(scope);
            String sql = " SELECT * FROM image_scope WHERE image_location='" + scope.getFile()
                    + "' AND name='" + stringValue(scope.getName()) + "'";
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
                        + " AND name='" + stringValue(scope.getName()) + "'";

            } else {
                sql = "INSERT INTO image_scope(image_location, name , scope_type, color_scope_type, "
                        + " area_data, color_data, color_distance, hsb_distance, area_excluded, "
                        + " color_excluded, outline, create_time, modify_time) VALUES('"
                        + scope.getFile() + "', '" + stringValue(scope.getName()) + "', '" + scope.getScopeType().name()
                        + "', '" + scope.getColorScopeType().name() + "', '" + areaData + "', '" + colorData + "', "
                        + scope.getColorDistance() + ", " + scope.getHsbDistance() + ", "
                        + (scope.isAreaExcluded() ? 1 : 0) + ", " + (scope.isColorExcluded() ? 1 : 0) + ", '"
                        + outline + "', '"
                        + DateTools.datetimeToString(new Date()) + "', '"
                        + DateTools.datetimeToString(new Date()) + "')";
            }
//            MyBoxLog.debug(sql);
            statement.executeUpdate(sql);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return read(scope.getFile());
    }

    public static String encodeAreaData(ImageScope scope) {
        if (scope == null || scope.getScopeType() == null) {
            return "";
        }
        String s = "";
        try {
            switch (scope.getScopeType()) {
                case Matting: {
                    List<IntPoint> points = scope.getPoints();
                    if (points != null) {
                        for (IntPoint p : points) {
                            s += p.getX() + DataSeparator + p.getY() + DataSeparator;
                        }
                        if (s.endsWith(DataSeparator)) {
                            s = s.substring(0, s.length() - DataSeparator.length());
                        }
                    }
                }
                break;
                case Rectangle:
                case RectangleColor:
                case Outline:
                    DoubleRectangle rect = scope.getRectangle();
                    if (rect != null) {
                        s = (int) rect.getSmallX() + DataSeparator + (int) rect.getSmallY() + DataSeparator
                                + (int) rect.getBigX() + DataSeparator + (int) rect.getBigY();
                    }
                    break;
                case Circle:
                case CircleColor:
                    DoubleCircle circle = scope.getCircle();
                    if (circle != null) {
                        s = (int) circle.getCenterX() + DataSeparator + (int) circle.getCenterY()
                                + DataSeparator + (int) circle.getRadius();
                    }

                    break;
                case Ellipse:
                case EllipseColor:
                    DoubleEllipse ellipse = scope.getEllipse();
                    if (ellipse != null) {
                        DoubleRectangle erect = ellipse.getRectangle();
                        if (erect != null) {
                            s = (int) (erect.getSmallX()) + DataSeparator + (int) erect.getSmallY() + DataSeparator
                                    + (int) erect.getBigX() + DataSeparator + (int) erect.getBigY();
                        }
                    }
                    break;
                case Polygon:
                case PolygonColor:
                    DoublePolygon polygon = scope.getPolygon();
                    if (polygon != null) {
                        for (Double d : polygon.getData()) {
                            s += Math.round(d) + DataSeparator;
                        }
                        if (s.endsWith(DataSeparator)) {
                            s = s.substring(0, s.length() - DataSeparator.length());
                        }
                    }
                    break;
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
            s = "";
        }
        return s;
    }

    public static String encodeColorData(ImageScope scope) {
        if (scope == null || scope.getScopeType() == null) {
            return "";
        }
        String s = "";
        try {
            switch (scope.getScopeType()) {
                case Color:
                case RectangleColor:
                case CircleColor:
                case EllipseColor:
                case PolygonColor:
                    List<Color> colors = scope.getColors();
                    if (colors != null) {
                        for (Color color : colors) {
                            s += color.getRGB() + DataSeparator;
                        }
                        if (s.endsWith(DataSeparator)) {
                            s = s.substring(0, s.length() - DataSeparator.length());
                        }
                    }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            s = "";
        }
        return s;
    }

    public static String encodeOutline(ImageScope scope) {
        if (scope == null || scope.getScopeType() != ScopeType.Outline || scope.getOutline() == null) {
            return "";
        }
        String s = "";
        try {
            String filename = AppVariables.getImageScopePath() + File.separator
                    + scope.getScopeType() + "_" + (new Date().getTime())
                    + "_" + new Random().nextInt(1000) + ".png";
            while (new File(filename).exists()) {
                filename = AppVariables.getImageScopePath() + File.separator
                        + scope.getScopeType() + "_" + (new Date().getTime())
                        + "_" + new Random().nextInt(1000) + ".png";
            }
            if (ImageFileWriters.writeImageFile(scope.getOutlineSource(), "png", filename)) {
                s = filename;
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
            s = "";
        }
        return s;
    }

    public static boolean clearScopes(String imageLocation) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM image_scope WHERE image_location='" + imageLocation + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean delete(ImageScope scope) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 PreparedStatement statement = conn.prepareStatement(Delete)) {
            statement.setString(1, scope.getFile());
            statement.setString(2, scope.getName());
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean delete(List<ImageScope> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return true;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setAutoCommit(false);
            try ( PreparedStatement statement = conn.prepareStatement(Delete)) {
                for (ImageScope scope : scopes) {
                    statement.setString(1, scope.getFile());
                    statement.setString(2, scope.getName());
                    statement.executeUpdate();
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

}
