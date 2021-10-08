package mara.mybox.bufferedimage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javafx.scene.image.Image;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.IntPoint;
import mara.mybox.db.data.BaseData;
import static mara.mybox.db.table.TableImageScope.DataSeparator;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.value.AppPaths;
import mara.mybox.value.AppValues;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2018-8-1 16:22:41
 * @License Apache License Version 2.0
 */
public class ImageScope extends BaseData {

    protected String file, name, areaData, colorData, outlineName;
    protected ScopeType scopeType;
    protected ColorScopeType colorScopeType;
    protected List<Color> colors;
    protected Color color;
    protected List<IntPoint> points;
    protected DoubleRectangle rectangle;
    protected DoubleCircle circle;
    protected DoubleEllipse ellipse;
    protected DoublePolygon polygon;
    protected int colorDistance, colorDistanceSquare;
    protected float hsbDistance;
    protected boolean areaExcluded, colorExcluded, eightNeighbor, distanceSquareRoot;
    protected Image image, clip;
    protected double opacity;
    protected Date createTime, modifyTime;
    protected BufferedImage outlineSource, outline;

    public enum ScopeType {
        Invalid, All, Matting, Rectangle, Circle, Ellipse, Polygon, Color,
        RectangleColor,
        CircleColor, EllipseColor, PolygonColor, Outline, Operate
    }

    public enum ColorScopeType {
        Invalid, AllColor, Color, Red, Green, Blue, Brightness, Saturation, Hue
    }

    public ImageScope() {
        init();
    }

    public ImageScope(Image image) {
        this.image = image;
        init();
    }

    public ImageScope(Image image, ScopeType type) {
        this.image = image;
        init();
        scopeType = type;
    }

    private void init() {
        scopeType = ScopeType.All;
        colorScopeType = ColorScopeType.AllColor;
        colors = new ArrayList<>();
        points = new ArrayList<>();
        colorDistance = 50;
        colorDistanceSquare = colorDistance * colorDistance;
        hsbDistance = 0.5f;
        opacity = 0.3;
        areaExcluded = colorExcluded = distanceSquareRoot = false;
        eightNeighbor = true;
        if (image != null) {
            rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                    image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
            circle = new DoubleCircle(image.getWidth() / 2, image.getHeight() / 2,
                    Math.min(image.getWidth(), image.getHeight()) / 4);
            ellipse = new DoubleEllipse(rectangle);
        } else {
            rectangle = new DoubleRectangle();
            circle = new DoubleCircle();
            ellipse = new DoubleEllipse();
        }
        polygon = new DoublePolygon();
        createTime = new Date();

    }

    public ImageScope cloneValues() {
        return ImageScopeTools.cloneAll(this);
    }

    public void addPoint(IntPoint point) {
        if (point == null) {
            return;
        }
        if (points == null) {
            points = new ArrayList<>();
        }
        if (!points.contains(point)) {
            points.add(point);
        }
    }

    public void addPoint(int x, int y) {
        if (x < 0 || y < 0) {
            return;
        }
        IntPoint point = new IntPoint(x, y);
        addPoint(point);
    }

    public void clearPoints() {
        points = new ArrayList<>();
    }

    public boolean addColor(Color color) {
        if (color == null) {
            return false;
        }
        if (colors == null) {
            colors = new ArrayList<>();
        }
        if (!colors.contains(color)) {
            colors.add(color);
            return true;
        } else {
            return false;
        }
    }

    public void clearColors() {
        colors = new ArrayList<>();
    }

    public void setCircleCenter(int x, int y) {
        if (circle == null) {
            circle = new DoubleCircle();
        }
        circle.setCenterX(x);
        circle.setCenterY(y);
    }

    public void setCircleRadius(int r) {
        if (circle == null) {
            circle = new DoubleCircle();
        }
        circle.setRadius(r);
    }

    public String getScopeText() {
        String s = "";
        if (null != scopeType) {
            switch (scopeType) {
                case All:
                    s = Languages.message("WholeImage");
                    break;
                case Matting:
                    String pointsString = "";
                    if (points.isEmpty()) {
                        pointsString += Languages.message("None");
                    } else {
                        for (IntPoint p : points) {
                            pointsString += "(" + p.getX() + "," + p.getY() + ") ";
                        }
                    }
                    s = Languages.message("Points") + ":" + pointsString;
                    s += " " + Languages.message("ColorDistance") + ":" + colorDistance;
                    break;
                case Color:
                    s = getScopeColorText();
                    break;
                default:
                    break;
            }
        }
        return s;
    }

    public String getScopeColorText() {
        String s = "";
        String colorString = "";
        if (colors.isEmpty()) {
            colorString += Languages.message("None") + " ";
        } else {
            for (Color c : colors) {
                colorString += c.toString() + " ";
            }
        }
        switch (colorScopeType) {
            case AllColor:
                s += " " + Languages.message("AllColors");
                break;
            case Color:
            case Red:
            case Green:
            case Blue:
                s += " " + Languages.message("SelectedColors") + ":" + colorString;
                s += Languages.message("ColorDistance") + ":" + colorDistance;
                if (colorExcluded) {
                    s += " " + Languages.message("Excluded");
                }
                break;
            case Brightness:
            case Saturation:
                s += " " + Languages.message("SelectedColors") + ":" + colorString;
                s += Languages.message("ColorDistance") + ":" + (int) (hsbDistance * 100);
                if (colorExcluded) {
                    s += " " + Languages.message("Excluded");
                }
                break;
            case Hue:
                s += " " + Languages.message("SelectedColors") + ":" + colorString;
                s += Languages.message("HueDistance") + ":" + (int) (hsbDistance * 360);
                if (colorExcluded) {
                    s += " " + Languages.message("Excluded");
                }
                break;
            default:
                break;
        }
        return s;
    }

    /*
        SubClass should implement this
     */
    protected boolean inScope(int x, int y, Color color) {
        return true;
    }

    public boolean inColorMatch(Color color1, Color color2) {
        return true;
    }

    /*
        Static methods
     */
    public static ImageScope create() {
        return new ImageScope();
    }

    public static boolean setValue(ImageScope data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "image_location":
                    data.setFile(value == null ? null : (String) value);
                    return true;
                case "name":
                    data.setName(value == null ? null : (String) value);
                    return true;
                case "scope_type":
                    data.setScopeType(value == null ? null : ScopeType.valueOf((String) value));
                    return true;
                case "color_scope_type":
                    data.setColorScopeType(value == null ? null : ColorScopeType.valueOf((String) value));
                    return true;
                case "area_data":
                    data.setAreaData(value == null ? null : (String) value);
                    return true;
                case "color_data":
                    data.setColorData(value == null ? null : (String) value);
                    return true;
                case "color_distance":
                    data.setColorDistance(value == null ? AppValues.InvalidInteger : (int) value);
                    return true;
                case "hsb_distance":
                    data.setHsbDistance(value == null ? AppValues.InvalidInteger : Float.valueOf(value + ""));
                    return true;
                case "area_excluded":
                    data.setAreaExcluded(value == null ? false : (int) value > 0);
                    return true;
                case "color_excluded":
                    data.setColorExcluded(value == null ? false : (int) value > 0);
                    return true;
                case "outline":
                    data.setOutlineName(value == null ? null : (String) value);
                    return true;
                case "create_time":
                    data.setCreateTime(value == null ? null : (Date) value);
                    return true;
                case "modify_time":
                    data.setCreateTime(value == null ? null : (Date) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(ImageScope data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "image_location":
                return data.getFile();
            case "name":
                return data.getName();
            case "scope_type":
                return data.getScopeType().name();
            case "color_scope_type":
                return data.getColorScopeType().name();
            case "area_data":
                return data.getAreaData();
            case "color_data":
                return data.getColorData();
            case "color_distance":
                return data.getColorDistance();
            case "hsb_distance":
                return data.getHsbDistance();
            case "area_excluded":
                return data.isAreaExcluded() ? 1 : 0;
            case "color_excluded":
                return data.isColorExcluded() ? 1 : 0;
            case "outline":
                return data.getOutline();
            case "create_time":
                return data.getCreateTime();
            case "modify_time":
                return data.getModifyTime();
        }
        return null;
    }

    public static boolean valid(ImageScope data) {
        return data != null && data.getScopeType() != null;
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
            scope.setAreaData(s);
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
            scope.setColorData(s);
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
            String filename = AppPaths.getImageScopePath() + File.separator
                    + scope.getScopeType() + "_" + (new Date().getTime())
                    + "_" + new Random().nextInt(1000) + ".png";
            while (new File(filename).exists()) {
                filename = AppPaths.getImageScopePath() + File.separator
                        + scope.getScopeType() + "_" + (new Date().getTime())
                        + "_" + new Random().nextInt(1000) + ".png";
            }
            if (ImageFileWriters.writeImageFile(scope.getOutlineSource(), "png", filename)) {
                s = filename;
            }
            scope.setOutlineName(filename);
        } catch (Exception e) {
            MyBoxLog.error(e);
            s = "";
        }
        return s;
    }

    public static boolean decodeAreaData(ScopeType type, String areaData, ImageScope scope) {
        if (type == null || areaData == null || scope == null) {
            return false;
        }
        try {
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
            scope.setAreaData(areaData);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean decodeColorData(ScopeType type, String colorData, ImageScope scope) {
        if (type == null || colorData == null || scope == null) {
            return false;
        }
        try {
            switch (type) {
                case Color:
                case RectangleColor:
                case CircleColor:
                case EllipseColor:
                case PolygonColor: {
                    List<Color> colors = new ArrayList<>();
                    if (colorData != null && !colorData.isBlank()) {
                        String[] items = colorData.split(DataSeparator);
                        for (String item : items) {
                            try {
                                colors.add(new Color(Integer.parseInt(item), true));
                            } catch (Exception e) {
                                MyBoxLog.error(e);
                            }
                        }
                    }
                    scope.setColors(colors);
                    scope.setColorData(colorData);
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean decodeOutline(ScopeType type, String outline, ImageScope scope) {
        if (type == null || outline == null || scope == null) {
            return false;
        }
        if (type != ScopeType.Outline) {
            return true;
        }
        try {
            scope.setOutlineName(outline);
            BufferedImage image = ImageFileReaders.readImage(new File(outline));
            scope.setOutlineSource(image);
            return image != null;
        } catch (Exception e) {
            MyBoxLog.error(e);
//            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    /*
        customized get/set
     */
    public void setColorDistance(int colorDistance) {
        this.colorDistance = colorDistance;
        this.colorDistanceSquare = colorDistance * colorDistance;
    }

    public void setColorDistanceSquare(int colorDistanceSquare) {
        this.colorDistanceSquare = colorDistanceSquare;
        this.colorDistance = (int) Math.sqrt(colorDistanceSquare);
    }

    /*
        get/set
     */
    public List<Color> getColors() {
        return colors;
    }

    public void setColors(List<Color> colors) {
        this.colors = colors;
    }

    public DoubleRectangle getRectangle() {
        return rectangle;
    }

    public void setRectangle(DoubleRectangle rectangle) {
        this.rectangle = rectangle;
    }

    public DoubleCircle getCircle() {
        return circle;
    }

    public void setCircle(DoubleCircle circle) {
        this.circle = circle;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public int getColorDistance() {
        return colorDistance;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    public ScopeType getScopeType() {
        return scopeType;
    }

    public void setScopeType(ScopeType scopeType) {
        this.scopeType = scopeType;
    }

    public ColorScopeType getColorScopeType() {
        return colorScopeType;
    }

    public void setColorScopeType(ColorScopeType colorScopeType) {
        this.colorScopeType = colorScopeType;
    }

    public boolean isAreaExcluded() {
        return areaExcluded;
    }

    public void setAreaExcluded(boolean areaExcluded) {
        this.areaExcluded = areaExcluded;
    }

    public boolean isColorExcluded() {
        return colorExcluded;
    }

    public void setColorExcluded(boolean colorExcluded) {
        this.colorExcluded = colorExcluded;
    }

    public List<IntPoint> getPoints() {
        return points;
    }

    public void setPoints(List<IntPoint> points) {
        this.points = points;
    }

    public int getColorDistanceSquare() {
        return colorDistanceSquare;
    }

    public float getHsbDistance() {
        return hsbDistance;
    }

    public void setHsbDistance(float hsbDistance) {
        this.hsbDistance = hsbDistance;
    }

    public DoubleEllipse getEllipse() {
        return ellipse;
    }

    public void setEllipse(DoubleEllipse ellipse) {
        this.ellipse = ellipse;
    }

    public DoublePolygon getPolygon() {
        return polygon;
    }

    public void setPolygon(DoublePolygon polygon) {
        this.polygon = polygon;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public BufferedImage getOutline() {
        return outline;
    }

    public void setOutline(BufferedImage outline) {
        this.outline = outline;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public BufferedImage getOutlineSource() {
        return outlineSource;
    }

    public void setOutlineSource(BufferedImage outlineSource) {
        this.outlineSource = outlineSource;
    }

    public Image getClip() {
        return clip;
    }

    public void setClip(Image clip) {
        this.clip = clip;
    }

    public boolean isEightNeighbor() {
        return eightNeighbor;
    }

    public void setEightNeighbor(boolean eightNeighbor) {
        this.eightNeighbor = eightNeighbor;
    }

    public boolean isDistanceSquareRoot() {
        return distanceSquareRoot;
    }

    public void setDistanceSquareRoot(boolean distanceSquareRoot) {
        this.distanceSquareRoot = distanceSquareRoot;
    }

    public String getAreaData() {
        return areaData;
    }

    public void setAreaData(String areaData) {
        this.areaData = areaData;
    }

    public String getColorData() {
        return colorData;
    }

    public void setColorData(String colorData) {
        this.colorData = colorData;
    }

    public String getOutlineName() {
        return outlineName;
    }

    public void setOutlineName(String outlineName) {
        this.outlineName = outlineName;
    }

}
