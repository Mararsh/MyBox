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
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2018-8-1 16:22:41
 * @License Apache License Version 2.0
 */
public class ImageScope {

    protected String file, name;
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

}
