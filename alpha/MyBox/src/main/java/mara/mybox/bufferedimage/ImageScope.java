package mara.mybox.bufferedimage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.scene.image.Image;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.IntPoint;
import mara.mybox.db.data.BaseData;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppValues;
import mara.mybox.value.Colors;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

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
    protected List<IntPoint> points;
    protected DoubleRectangle rectangle;
    protected DoubleCircle circle;
    protected DoubleEllipse ellipse;
    protected DoublePolygon polygon;
    protected int colorDistance, colorDistanceSquare;
    protected float hsbDistance;
    protected boolean areaExcluded, colorExcluded, eightNeighbor, distanceSquareRoot;
    protected Image image, clip;
    protected Color maskColor;
    protected float maskOpacity;
    protected Date createTime, modifyTime;
    protected BufferedImage outlineSource, outline;

    public static enum ScopeType {
        Matting, Rectangle, Circle, Ellipse, Polygon, Color, Outline
    }

    public static enum ColorScopeType {
        AllColor, Color, Red, Green, Blue, Brightness, Saturation, Hue
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
        scopeType = ScopeType.Rectangle;
        colorScopeType = ColorScopeType.AllColor;
        colors = new ArrayList<>();
        points = new ArrayList<>();
        colorDistance = 50;
        colorDistanceSquare = colorDistance * colorDistance;
        hsbDistance = 0.5f;
        maskColor = Colors.TRANSPARENT;
        maskOpacity = 0.5f;
        areaExcluded = colorExcluded = distanceSquareRoot = false;
        eightNeighbor = true;
        if (image != null) {
            rectangle = DoubleRectangle.xywh(image.getWidth() / 4, image.getHeight() / 4,
                    image.getWidth() / 2, image.getHeight() / 2);
            circle = new DoubleCircle(image.getWidth() / 2, image.getHeight() / 2,
                    Math.min(image.getWidth(), image.getHeight()) / 4);
            ellipse = DoubleEllipse.rect(rectangle);
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

    public void decode() {
        if (colorData != null) {
            ImageScopeTools.decodeColorData(this);
        }
        if (areaData != null) {
            ImageScopeTools.decodeAreaData(this);
        }
        if (outlineName != null) {
            ImageScopeTools.decodeOutline(this);
        }
    }

    public void encode() {
        ImageScopeTools.encodeColorData(this);
        ImageScopeTools.encodeAreaData(this);
        ImageScopeTools.encodeOutline(this);
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

    public void setPoint(int index, int x, int y) {
        if (x < 0 || y < 0 || points == null || index < 0 || index >= points.size()) {
            return;
        }
        IntPoint point = new IntPoint(x, y);
        points.set(index, point);
    }

    public void deletePoint(int index) {
        if (points == null || index < 0 || index >= points.size()) {
            return;
        }
        points.remove(index);
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

    public String getScopeText() {
        String s = "";
        if (null != scopeType) {
            switch (scopeType) {
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
                    data.setScopeType(value == null ? null : ImageScopeTools.scopeType((String) value));
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
                    data.setHsbDistance(value == null ? AppValues.InvalidInteger : Float.parseFloat(value + ""));
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
            MyBoxLog.debug(e);
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

    public String getColorTypeName() {
        if (colorScopeType == null) {
            return null;
        }
        return message(colorScopeType.name());
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

    public ImageScope setRectangle(DoubleRectangle rectangle) {
        this.rectangle = rectangle;
        return this;
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

    public ImageScope setImage(Image image) {
        this.image = image;
        return this;
    }

    public int getColorDistance() {
        return colorDistance;
    }

    public float getMaskOpacity() {
        return maskOpacity;
    }

    public void setMaskOpacity(float maskOpacity) {
        this.maskOpacity = maskOpacity;
    }

    public Color getMaskColor() {
        return maskColor;
    }

    public void setMaskColor(Color maskColor) {
        this.maskColor = maskColor;
    }

    public ScopeType getScopeType() {
        return scopeType;
    }

    public ImageScope setScopeType(ScopeType scopeType) {
        this.scopeType = scopeType;
        return this;
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

    public ImageScope setAreaExcluded(boolean areaExcluded) {
        this.areaExcluded = areaExcluded;
        return this;
    }

    public boolean isColorExcluded() {
        return colorExcluded;
    }

    public ImageScope setColorExcluded(boolean colorExcluded) {
        this.colorExcluded = colorExcluded;
        return this;
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

    public ImageScope setOutline(BufferedImage outline) {
        this.outline = outline;
        return this;
    }

    public BufferedImage getOutlineSource() {
        return outlineSource;
    }

    public ImageScope setOutlineSource(BufferedImage outlineSource) {
        this.outlineSource = outlineSource;
        return this;
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

    public ImageScope setEightNeighbor(boolean eightNeighbor) {
        this.eightNeighbor = eightNeighbor;
        return this;
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
