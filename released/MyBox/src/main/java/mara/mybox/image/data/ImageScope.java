package mara.mybox.image.data;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;
import mara.mybox.color.ColorMatch;
import mara.mybox.color.ColorMatch.MatchAlgorithm;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.IntPoint;
import mara.mybox.fxml.FxTask;
import mara.mybox.image.tools.ImageScopeTools;
import mara.mybox.value.Colors;

/**
 * @Author Mara
 * @CreateDate 2018-8-1 16:22:41
 * @License Apache License Version 2.0
 */
public class ImageScope {

    public static String ValueSeparator = ",";

    protected String background, name, shapeData, colorData, outlineName;
    protected ShapeType shapeType;
    protected List<Color> colors;
    protected List<IntPoint> points;
    protected DoubleRectangle rectangle;
    protected DoubleCircle circle;
    protected DoubleEllipse ellipse;
    protected DoublePolygon polygon;
    protected ColorMatch colorMatch;
    protected boolean shapeExcluded, colorExcluded;
    protected Image image, clip;
    protected Color maskColor;
    protected float maskOpacity;
    protected BufferedImage outlineSource, outline;

    public static enum ShapeType {
        Whole, Matting4, Matting8, Rectangle, Circle, Ellipse, Polygon, Outline
    }

    public ImageScope() {
        init();
    }

    public final void init() {
        shapeType = ShapeType.Whole;
        colorMatch = new ColorMatch();
        maskColor = Colors.TRANSPARENT;
        maskOpacity = 0.5f;
        shapeExcluded = false;
        resetParameters();
    }

    public final void resetParameters() {
        rectangle = null;
        circle = null;
        ellipse = null;
        polygon = null;
        shapeData = null;
        colorData = null;
        outlineName = null;
        outlineSource = null;
        outline = null;
        points = new ArrayList<>();
        clearColors();
    }

    public ImageScope cloneValues() {
        return ImageScopeTools.cloneAll(this);
    }

    public boolean isWhole() {
        return shapeType == null || shapeType == ShapeType.Whole;
    }

    public void decode(FxTask task) {
        if (colorData != null) {
            ImageScopeTools.decodeColorData(this);
        }
        if (shapeData != null) {
            ImageScopeTools.decodeShapeData(this);
        }
        if (outlineName != null) {
            ImageScopeTools.decodeOutline(task, this);
        }
    }

    public void encode(FxTask task) {
        ImageScopeTools.encodeColorData(this);
        ImageScopeTools.encodeShapeData(this);
        ImageScopeTools.encodeOutline(task, this);
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

    /*
        color match
     */
    protected boolean isMatchColor(Color color1, Color color2) {
        boolean match = colorMatch.isMatch(color1, color2);
        return colorExcluded ? !match : match;
    }

    public boolean isMatchColors(Color color) {
        return colorMatch.isMatchColors(colors, color, colorExcluded);
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
        colors = null;
    }

    /**
     *
     * @param x the x of the pixel
     * @param y the y of the pixel
     * @param color the color of the pixel
     * @return whether in image scope
     */
    public boolean inScope(int x, int y, Color color) {
        return inShape(x, y) && isMatchColors(color);
    }

    public boolean inShape(int x, int y) {
        return true;
    }

    /*
        Static methods
     */
    public static ImageScope create() {
        return new ImageScope();
    }

    public static boolean valid(ImageScope data) {
        return data != null
                && data.getColorMatch() != null
                && data.getShapeType() != null;
    }

    public static ShapeType shapeType(String type) {
        try {
            return ShapeType.valueOf(type);
        } catch (Exception e) {
            return ShapeType.Whole;
        }
    }

    public static MatchAlgorithm matchAlgorithm(String a) {
        try {
            return MatchAlgorithm.valueOf(a);
        } catch (Exception e) {
            return ColorMatch.DefaultAlgorithm;
        }
    }

    /*
        customized get/set
     */
    public ShapeType getShapeType() {
        if (shapeType == null) {
            shapeType = ShapeType.Whole;
        }
        return shapeType;
    }

    public ImageScope setShapeType(ShapeType shapeType) {
        this.shapeType = shapeType != null ? shapeType : ShapeType.Whole;
        return this;
    }

    public ImageScope setShapeType(String shapeType) {
        this.shapeType = shapeType(shapeType);
        return this;
    }

    public double getColorThreshold() {
        return colorMatch.getThreshold();
    }

    public void setColorThreshold(double threshold) {
        colorMatch.setThreshold(threshold);
    }

    public MatchAlgorithm getColorAlgorithm() {
        return colorMatch.getAlgorithm();
    }

    public void setColorAlgorithm(MatchAlgorithm algorithm) {
        colorMatch.setAlgorithm(algorithm);
    }

    public void setColorAlgorithm(String algorithm) {
        colorMatch.setAlgorithm(matchAlgorithm(algorithm));
    }

    public boolean setColorWeights(String weights) {
        return colorMatch.setColorWeights(weights);
    }

    public String getColorWeights() {
        return colorMatch.getColorWeights();
    }

    /*
        get/set
     */
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

    public ColorMatch getColorMatch() {
        return colorMatch;
    }

    public List<Color> getColors() {
        return colors;
    }

    public ImageScope setColors(List<Color> colors) {
        this.colors = colors;
        return this;
    }

    public boolean isColorExcluded() {
        return colorExcluded;
    }

    public ImageScope setColorExcluded(boolean colorExcluded) {
        this.colorExcluded = colorExcluded;
        return this;
    }

    public float getMaskOpacity() {
        return maskOpacity;
    }

    public ImageScope setMaskOpacity(float maskOpacity) {
        this.maskOpacity = maskOpacity;
        return this;
    }

    public Color getMaskColor() {
        return maskColor;
    }

    public ImageScope setMaskColor(Color maskColor) {
        this.maskColor = maskColor;
        return this;
    }

    public String getBackground() {
        return background;
    }

    public boolean isShapeExcluded() {
        return shapeExcluded;
    }

    public ImageScope setShapeExcluded(boolean shapeExcluded) {
        this.shapeExcluded = shapeExcluded;
        return this;
    }

    public List<IntPoint> getPoints() {
        return points;
    }

    public void setPoints(List<IntPoint> points) {
        this.points = points;
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

    public void setBackground(String background) {
        this.background = background;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getShapeData() {
        return shapeData;
    }

    public void setShapeData(String shapeData) {
        this.shapeData = shapeData;
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
