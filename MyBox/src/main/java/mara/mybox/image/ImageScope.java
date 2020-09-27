package mara.mybox.image;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.scene.image.Image;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.data.IntPoint;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-8-1 16:22:41
 * @Version 1.0
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
    protected int colorDistance, colorDistance2;
    protected float hsbDistance;
    protected boolean areaExcluded, colorExcluded, eightNeighbor;
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
        colorDistance2 = colorDistance * colorDistance;
        hsbDistance = 0.5f;
        opacity = 0.3;
        areaExcluded = colorExcluded = false;
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
        return ImageScope.cloneAll(this);
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
                    s = message("WholeImage");
                    break;
                case Matting:
                    String pointsString = "";
                    if (points.isEmpty()) {
                        pointsString += message("None");
                    } else {
                        for (IntPoint p : points) {
                            pointsString += "(" + p.getX() + "," + p.getY() + ") ";
                        }
                    }
                    s = message("Points") + ":" + pointsString;
                    s += " " + message("ColorDistance") + ":" + colorDistance;
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
            colorString += message("None") + " ";
        } else {
            for (Color c : colors) {
                colorString += c.toString() + " ";
            }
        }
        switch (colorScopeType) {
            case AllColor:
                s += " " + message("AllColors");
                break;
            case Color:
            case Red:
            case Green:
            case Blue:
                s += " " + message("SelectedColors") + ":" + colorString;
                s += message("ColorDistance") + ":" + colorDistance;
                if (colorExcluded) {
                    s += " " + message("Excluded");
                }
                break;
            case Brightness:
            case Saturation:
                s += " " + message("SelectedColors") + ":" + colorString;
                s += message("ColorDistance") + ":" + (int) (hsbDistance * 100);
                if (colorExcluded) {
                    s += " " + message("Excluded");
                }
                break;
            case Hue:
                s += " " + message("SelectedColors") + ":" + colorString;
                s += message("HueDistance") + ":" + (int) (hsbDistance * 360);
                if (colorExcluded) {
                    s += " " + message("Excluded");
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
    public static BufferedImage indicateRectangle(BufferedImage source,
            Color color, int lineWidth, DoubleRectangle rect) {
        try {

            int width = source.getWidth();
            int height = source.getHeight();
            if (!rect.isValid(width, height)) {
                return source;
            }
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            g.setComposite(ac);
            g.setColor(color);
            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    1f, new float[]{lineWidth, lineWidth}, 0f);
            g.setStroke(stroke);
            g.drawRect((int) rect.getSmallX(), (int) rect.getSmallY(), (int) rect.getWidth(), (int) rect.getHeight());
            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return source;
        }
    }

    public static BufferedImage indicateCircle(BufferedImage source,
            Color color, int lineWidth, DoubleCircle circle) {
        try {
            if (!circle.isValid()) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            g.setComposite(ac);
            g.setColor(color);
            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    1f, new float[]{lineWidth, lineWidth}, 0f);
            g.setStroke(stroke);
            g.drawOval((int) circle.getCenterX() - (int) circle.getRadius(), (int) circle.getCenterY() - (int) circle.getRadius(),
                    2 * (int) circle.getRadius(), 2 * (int) circle.getRadius());
            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return source;
        }
    }

    public static BufferedImage indicateEllipse(BufferedImage source,
            Color color, int lineWidth, DoubleEllipse ellipse) {
        try {
            if (!ellipse.isValid()) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();
            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            g.setComposite(ac);
            g.setColor(color);
            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    1f, new float[]{lineWidth, lineWidth}, 0f);
            g.setStroke(stroke);
            DoubleRectangle rect = ellipse.getRectangle();
            g.drawOval((int) Math.round(rect.getSmallX()), (int) Math.round(rect.getSmallY()),
                    (int) Math.round(rect.getWidth()), (int) Math.round(rect.getHeight()));
            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return source;
        }
    }

    public static BufferedImage indicateSplit(BufferedImage source,
            List<Integer> rows, List<Integer> cols,
            Color lineColor, int lineWidth, boolean showSize, double scale) {
        try {
            if (rows == null || cols == null) {
                return source;
            }
            int width = source.getWidth();
            int height = source.getHeight();

            int imageType = source.getType();
            if (imageType == BufferedImage.TYPE_CUSTOM) {
                imageType = BufferedImage.TYPE_INT_ARGB;
            }
            BufferedImage target = new BufferedImage(width, height, imageType);
            Graphics2D g = target.createGraphics();
            g.drawImage(source, 0, 0, width, height, null);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            g.setComposite(ac);
            g.setColor(lineColor);
            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    1f, new float[]{lineWidth, lineWidth}, 0f);
//            BasicStroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
            g.setStroke(stroke);

            for (int i = 0; i < rows.size(); ++i) {
                int row = (int) (rows.get(i) / scale);
                if (row <= 0 || row >= height - 1) {
                    continue;
                }
                g.drawLine(0, row, width, row);
            }
            for (int i = 0; i < cols.size(); ++i) {
                int col = (int) (cols.get(i) / scale);
                if (col <= 0 || col >= width - 1) {
                    continue;
                }
                g.drawLine(col, 0, col, height);
            }

            if (showSize) {
                List<String> texts = new ArrayList<>();
                List<Integer> xs = new ArrayList<>();
                List<Integer> ys = new ArrayList<>();
                for (int i = 0; i < rows.size() - 1; ++i) {
                    int h = rows.get(i + 1) - rows.get(i) + 1;
                    for (int j = 0; j < cols.size() - 1; ++j) {
                        int w = cols.get(j + 1) - cols.get(j) + 1;
                        texts.add(w + "x" + h);
                        xs.add(cols.get(j) + w / 3);
                        ys.add(rows.get(i) + h / 3);
//                    logger.debug(w / 2 + ", " + h / 2 + "  " + w + "x" + h);
                    }
                }

                int fontSize = width / (cols.size() * 10);
                Font font = new Font(Font.MONOSPACED, Font.BOLD, fontSize);
                g.setFont(font);
                for (int i = 0; i < texts.size(); ++i) {
                    g.drawString(texts.get(i), xs.get(i), ys.get(i));
                }
            }

            g.dispose();
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return source;
        }
    }

    public static ImageScope cloneAll(ImageScope sourceScope) {
        ImageScope targetScope = new ImageScope();
        ImageScope.cloneAll(targetScope, sourceScope);
        return targetScope;
    }

    public static void cloneAll(ImageScope targetScope, ImageScope sourceScope) {
        try {
            targetScope.setImage(sourceScope.getImage());
            targetScope.setScopeType(sourceScope.getScopeType());
            targetScope.setColorScopeType(sourceScope.getColorScopeType());
            cloneValues(targetScope, sourceScope);
        } catch (Exception e) {
        }
    }

    public static void cloneValues(ImageScope targetScope, ImageScope sourceScope) {
        try {
            List<IntPoint> npoints = new ArrayList<>();
            if (sourceScope.getPoints() != null) {
                npoints.addAll(sourceScope.getPoints());
            }
            targetScope.setPoints(npoints);
            List<Color> ncolors = new ArrayList<>();
            if (sourceScope.getColors() != null) {
                ncolors.addAll(sourceScope.getColors());
            }
            targetScope.setColors(ncolors);
            targetScope.setRectangle(sourceScope.getRectangle().cloneValues());
            targetScope.setCircle(sourceScope.getCircle().cloneValues());
            targetScope.setEllipse(sourceScope.getEllipse().cloneValues());
            targetScope.setPolygon(sourceScope.getPolygon().cloneValues());
            targetScope.setColorDistance(sourceScope.getColorDistance());
            targetScope.setColorDistance2(sourceScope.getColorDistance2());
            targetScope.setHsbDistance(sourceScope.getHsbDistance());
            targetScope.setColorExcluded(sourceScope.isColorExcluded());
            targetScope.setAreaExcluded(sourceScope.isAreaExcluded());
            targetScope.setOpacity(sourceScope.getOpacity());
            targetScope.setCreateTime(sourceScope.getCreateTime());
            targetScope.setOutline(sourceScope.getOutline());
            targetScope.setEightNeighbor(sourceScope.isEightNeighbor());
        } catch (Exception e) {
//            logger.debug(e.toString());
        }
    }

    public static ImageScope fineImageScope(ImageScope sourceScope) {
        try {
            ImageScope newScope = ImageScope.fineImageScope(sourceScope.getImage(),
                    sourceScope.getScopeType(), sourceScope.getColorScopeType());
            ImageScope.cloneValues(newScope, sourceScope);
            return newScope;
        } catch (Exception e) {
//            logger.debug(e.toString());
            return sourceScope;
        }
    }

    public static ImageScope fineImageScope(Image image,
            ScopeType scopeType, ColorScopeType colorScopeType) {
        switch (scopeType) {
            case All:
                return new All(image);
            case Rectangle:
                return new Rectangle(image);
            case Circle:
                return new Circle(image);
            case Ellipse:
                return new Ellipse(image);
            case Polygon:
                return new Polygon(image);
            case Color:
                switch (colorScopeType) {
                    case AllColor:
                        return new All(image);
                    case Color:
                        return new ColorDistance(image);
                    case Red:
                        return new RedDistance(image);
                    case Green:
                        return new GreenDistance(image);
                    case Blue:
                        return new BlueDistance(image);
                    case Brightness:
                        return new BrightnessDistance(image);
                    case Saturation:
                        return new SaturationDistance(image);
                    case Hue:
                        return new HueDistance(image);
                    default:
                        return new All(image);
                }
            case RectangleColor:
                switch (colorScopeType) {
                    case AllColor:
                        return new Rectangle(image);
                    case Color:
                        return new RectangleColor(image);
                    case Red:
                        return new RectangleRed(image);
                    case Green:
                        return new RectangleGreen(image);
                    case Blue:
                        return new RectangleBlue(image);
                    case Brightness:
                        return new RectangleBrightness(image);
                    case Saturation:
                        return new RectangleSaturation(image);
                    case Hue:
                        return new RectangleHue(image);
                    default:
                        return new Rectangle(image);
                }
            case CircleColor:
                switch (colorScopeType) {
                    case AllColor:
                        return new Circle(image);
                    case Color:
                        return new CircleColor(image);
                    case Red:
                        return new CircleRed(image);
                    case Green:
                        return new CircleGreen(image);
                    case Blue:
                        return new CircleBlue(image);
                    case Brightness:
                        return new CircleBrightness(image);
                    case Saturation:
                        return new CircleSaturation(image);
                    case Hue:
                        return new CircleHue(image);
                    default:
                        return new Circle(image);
                }
            case EllipseColor:
                switch (colorScopeType) {
                    case AllColor:
                        return new Ellipse(image);
                    case Color:
                        return new EllipseColor(image);
                    case Red:
                        return new EllipseRed(image);
                    case Green:
                        return new EllipseGreen(image);
                    case Blue:
                        return new EllipseBlue(image);
                    case Brightness:
                        return new EllipseBrightness(image);
                    case Saturation:
                        return new EllipseSaturation(image);
                    case Hue:
                        return new EllipseHue(image);
                    default:
                        return new ImageScope(image, scopeType);
                }
            case PolygonColor:
                switch (colorScopeType) {
                    case AllColor:
                        return new Polygon(image);
                    case Color:
                        return new PolygonColor(image);
                    case Red:
                        return new PolygonRed(image);
                    case Green:
                        return new PolygonGreen(image);
                    case Blue:
                        return new PolygonBlue(image);
                    case Brightness:
                        return new PolygonBrightness(image);
                    case Saturation:
                        return new PolygonSaturation(image);
                    case Hue:
                        return new PolygonHue(image);
                    default:
                        return new ImageScope(image, scopeType);
                }
            case Matting:
                switch (colorScopeType) {
                    case AllColor:
                        return new Matting(image);
                    case Color:
                        return new MattingColor(image);
                    case Red:
                        return new MattingRed(image);
                    case Green:
                        return new MattingGreen(image);
                    case Blue:
                        return new MattingBlue(image);
                    case Brightness:
                        return new MattingBrightness(image);
                    case Saturation:
                        return new MattingSaturation(image);
                    case Hue:
                        return new MattingHue(image);
                    default:
                        return new Matting(image);
                }
            case Outline:
                return new Outline(image);
            default:
                return new ImageScope(image, scopeType);
        }
    }

    public static ScopeType type(String type) {
        return ScopeType.valueOf(type);
    }

    public static boolean inShape(DoubleShape shape, boolean areaExcluded,
            int x, int y) {
        if (areaExcluded) {
            return !shape.include(x, y);
        } else {
            return shape.include(x, y);
        }
    }

    public static boolean isColorMatch2(List<Color> colors, boolean colorExcluded,
            int colorDistance2, Color color) {
        if (colorExcluded) {
            for (Color oColor : colors) {
                if (ImageColor.isColorMatch2(color, oColor, colorDistance2)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (ImageColor.isColorMatch2(color, oColor, colorDistance2)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isRedMatch(List<Color> colors, boolean colorExcluded,
            int colorDistance, Color color) {
        if (colorExcluded) {
            for (Color oColor : colors) {
                if (ImageColor.isRedMatch(color, oColor, colorDistance)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (ImageColor.isRedMatch(color, oColor, colorDistance)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isGreenMatch(List<Color> colors, boolean colorExcluded,
            int colorDistance, Color color) {
        if (colorExcluded) {
            for (Color oColor : colors) {
                if (ImageColor.isGreenMatch(color, oColor, colorDistance)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (ImageColor.isGreenMatch(color, oColor, colorDistance)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isBlueMatch(List<Color> colors, boolean colorExcluded,
            int colorDistance, Color color) {
        if (colorExcluded) {
            for (Color oColor : colors) {
                if (ImageColor.isBlueMatch(color, oColor, colorDistance)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (ImageColor.isBlueMatch(color, oColor, colorDistance)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isBrightnessMatch(List<Color> colors, boolean colorExcluded,
            float hsbDistance, Color color) {
        if (colorExcluded) {
            for (Color oColor : colors) {
                if (ImageColor.isBrightnessMatch(color, oColor, hsbDistance)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (ImageColor.isBrightnessMatch(color, oColor, hsbDistance)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isSaturationMatch(List<Color> colors, boolean colorExcluded,
            float hsbDistance, Color color) {
        if (colorExcluded) {
            for (Color oColor : colors) {
                if (ImageColor.isSaturationMatch(color, oColor, hsbDistance)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (ImageColor.isSaturationMatch(color, oColor, hsbDistance)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isHueMatch(List<Color> colors, boolean colorExcluded,
            float hsbDistance, Color color) {
        if (colorExcluded) {
            for (Color oColor : colors) {
                if (ImageColor.isHueMatch(color, oColor, hsbDistance)) {
                    return false;
                }
            }
            return true;
        } else {
            for (Color oColor : colors) {
                if (ImageColor.isHueMatch(color, oColor, hsbDistance)) {
                    return true;
                }
            }
            return false;
        }
    }

    /*
        SubClass
     */
    static class All extends ImageScope {

        public All(Image image) {
            this.image = image;
            this.scopeType = ScopeType.All;
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return true;
        }
    }

    static class Rectangle extends ImageScope {

        public Rectangle(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Rectangle;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
            } else {
                rectangle = new DoubleRectangle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return inShape(rectangle, areaExcluded, x, y);
        }
    }

    static class Circle extends ImageScope {

        public Circle(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Circle;
            if (image != null) {
                circle = new DoubleCircle(image.getWidth() / 2, image.getHeight() / 2,
                        image.getHeight() / 4);
            } else {
                circle = new DoubleCircle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return inShape(circle, areaExcluded, x, y);
        }
    }

    static class Ellipse extends ImageScope {

        public Ellipse(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Ellipse;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
                ellipse = new DoubleEllipse(rectangle);
            } else {
                rectangle = new DoubleRectangle();
                ellipse = new DoubleEllipse();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return inShape(ellipse, areaExcluded, x, y);
        }
    }

    static class Polygon extends ImageScope {

        public Polygon(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Polygon;
            if (image != null) {
                polygon = new DoublePolygon();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return inShape(polygon, areaExcluded, x, y);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isColorMatch2(color1, color2, colorDistance2);
        }
    }

    static class AllColor extends ImageScope {

        public AllColor(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Color;
            this.colorScopeType = ColorScopeType.AllColor;
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return true;
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return true;
        }
    }

    static class ColorDistance extends ImageScope {

        public ColorDistance(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Color;
            this.colorScopeType = ColorScopeType.Color;
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return isColorMatch2(colors, colorExcluded, colorDistance2, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isColorMatch2(color1, color2, colorDistance2);
        }
    }

    static class RedDistance extends ImageScope {

        public RedDistance(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Color;
            this.colorScopeType = ColorScopeType.Red;
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return isRedMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isRedMatch(color1, color2, colorDistance);
        }
    }

    static class GreenDistance extends ImageScope {

        public GreenDistance(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Color;
            this.colorScopeType = ColorScopeType.Green;
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return isGreenMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isGreenMatch(color1, color2, colorDistance);
        }
    }

    static class BlueDistance extends ImageScope {

        public BlueDistance(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Color;
            this.colorScopeType = ColorScopeType.Blue;
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return isBlueMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isBlueMatch(color1, color2, colorDistance);
        }
    }

    static class BrightnessDistance extends ImageScope {

        public BrightnessDistance(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Color;
            this.colorScopeType = ColorScopeType.Brightness;
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return isBrightnessMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isBrightnessMatch(color1, color2, hsbDistance);
        }
    }

    static class SaturationDistance extends ImageScope {

        public SaturationDistance(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Color;
            this.colorScopeType = ColorScopeType.Saturation;
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return isSaturationMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isSaturationMatch(color1, color2, hsbDistance);
        }
    }

    static class HueDistance extends ImageScope {

        public HueDistance(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Color;
            this.colorScopeType = ColorScopeType.Hue;
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            return isHueMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isHueMatch(color1, color2, hsbDistance);
        }
    }

    static class RectangleColor extends ImageScope {

        public RectangleColor(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Rectangle;
            this.colorScopeType = ColorScopeType.Color;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
            } else {
                rectangle = new DoubleRectangle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(rectangle, areaExcluded, x, y)) {
                return false;
            }
            return isColorMatch2(colors, colorExcluded, colorDistance2, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isColorMatch2(color1, color2, colorDistance2);
        }
    }

    static class RectangleRed extends ImageScope {

        public RectangleRed(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Rectangle;
            this.colorScopeType = ColorScopeType.Red;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
            } else {
                rectangle = new DoubleRectangle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(rectangle, areaExcluded, x, y)) {
                return false;
            }
            return isRedMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isRedMatch(color1, color2, colorDistance);
        }
    }

    static class RectangleGreen extends ImageScope {

        public RectangleGreen(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Rectangle;
            this.colorScopeType = ColorScopeType.Green;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
            } else {
                rectangle = new DoubleRectangle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(rectangle, areaExcluded, x, y)) {
                return false;
            }
            return isGreenMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isGreenMatch(color1, color2, colorDistance);
        }
    }

    static class RectangleBlue extends ImageScope {

        public RectangleBlue(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Rectangle;
            this.colorScopeType = ColorScopeType.Blue;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
            } else {
                rectangle = new DoubleRectangle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(rectangle, areaExcluded, x, y)) {
                return false;
            }
            return isBlueMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isBlueMatch(color1, color2, colorDistance);
        }
    }

    static class RectangleBrightness extends ImageScope {

        public RectangleBrightness(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Rectangle;
            this.colorScopeType = ColorScopeType.Brightness;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
            } else {
                rectangle = new DoubleRectangle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(rectangle, areaExcluded, x, y)) {
                return false;
            }
            return isBrightnessMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isBrightnessMatch(color1, color2, hsbDistance);
        }
    }

    static class RectangleSaturation extends ImageScope {

        public RectangleSaturation(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Rectangle;
            this.colorScopeType = ColorScopeType.Saturation;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
            } else {
                rectangle = new DoubleRectangle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(rectangle, areaExcluded, x, y)) {
                return false;
            }
            return isSaturationMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isSaturationMatch(color1, color2, hsbDistance);
        }
    }

    static class RectangleHue extends ImageScope {

        public RectangleHue(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Rectangle;
            this.colorScopeType = ColorScopeType.Hue;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
            } else {
                rectangle = new DoubleRectangle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(rectangle, areaExcluded, x, y)) {
                return false;
            }
            return isHueMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isHueMatch(color1, color2, hsbDistance);
        }
    }

    static class CircleColor extends ImageScope {

        public CircleColor(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Circle;
            this.colorScopeType = ColorScopeType.Color;
            if (image != null) {
                circle = new DoubleCircle(image.getWidth() / 2, image.getHeight() / 2,
                        image.getHeight() / 4);
            } else {
                circle = new DoubleCircle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(circle, areaExcluded, x, y)) {
                return false;
            }
            return isColorMatch2(colors, colorExcluded, colorDistance2, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isColorMatch2(color1, color2, colorDistance2);
        }
    }

    static class CircleRed extends ImageScope {

        public CircleRed(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Circle;
            this.colorScopeType = ColorScopeType.Red;
            if (image != null) {
                circle = new DoubleCircle(image.getWidth() / 2, image.getHeight() / 2,
                        image.getHeight() / 4);
            } else {
                circle = new DoubleCircle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(circle, areaExcluded, x, y)) {
                return false;
            }
            return isRedMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isRedMatch(color1, color2, colorDistance);
        }
    }

    static class CircleGreen extends ImageScope {

        public CircleGreen(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Circle;
            this.colorScopeType = ColorScopeType.Green;
            if (image != null) {
                circle = new DoubleCircle(image.getWidth() / 2, image.getHeight() / 2,
                        image.getHeight() / 4);
            } else {
                circle = new DoubleCircle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(circle, areaExcluded, x, y)) {
                return false;
            }
            return isGreenMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isGreenMatch(color1, color2, colorDistance);
        }
    }

    static class CircleBlue extends ImageScope {

        public CircleBlue(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Circle;
            this.colorScopeType = ColorScopeType.Blue;
            if (image != null) {
                circle = new DoubleCircle(image.getWidth() / 2, image.getHeight() / 2,
                        image.getHeight() / 4);
            } else {
                circle = new DoubleCircle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(circle, areaExcluded, x, y)) {
                return false;
            }
            return isBlueMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isBlueMatch(color1, color2, colorDistance);
        }
    }

    static class CircleBrightness extends ImageScope {

        public CircleBrightness(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Circle;
            this.colorScopeType = ColorScopeType.Brightness;
            if (image != null) {
                circle = new DoubleCircle(image.getWidth() / 2, image.getHeight() / 2,
                        image.getHeight() / 4);
            } else {
                circle = new DoubleCircle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(circle, areaExcluded, x, y)) {
                return false;
            }
            return isBrightnessMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isBrightnessMatch(color1, color2, hsbDistance);
        }
    }

    static class CircleSaturation extends ImageScope {

        public CircleSaturation(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Circle;
            this.colorScopeType = ColorScopeType.Saturation;
            if (image != null) {
                circle = new DoubleCircle(image.getWidth() / 2, image.getHeight() / 2,
                        image.getHeight() / 4);
            } else {
                circle = new DoubleCircle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(circle, areaExcluded, x, y)) {
                return false;
            }
            return isSaturationMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isSaturationMatch(color1, color2, hsbDistance);
        }
    }

    static class CircleHue extends ImageScope {

        public CircleHue(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Circle;
            this.colorScopeType = ColorScopeType.Hue;
            if (image != null) {
                circle = new DoubleCircle(image.getWidth() / 2, image.getHeight() / 2,
                        image.getHeight() / 4);
            } else {
                circle = new DoubleCircle();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(circle, areaExcluded, x, y)) {
                return false;
            }
            return isHueMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isHueMatch(color1, color2, hsbDistance);
        }
    }

    static class EllipseColor extends ImageScope {

        public EllipseColor(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Ellipse;
            this.colorScopeType = ColorScopeType.Color;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
                ellipse = new DoubleEllipse(rectangle);
            } else {
                rectangle = new DoubleRectangle();
                ellipse = new DoubleEllipse();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(ellipse, areaExcluded, x, y)) {
                return false;
            }
            return isColorMatch2(colors, colorExcluded, colorDistance2, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isColorMatch2(color1, color2, colorDistance2);
        }
    }

    static class EllipseRed extends ImageScope {

        public EllipseRed(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Ellipse;
            this.colorScopeType = ColorScopeType.Red;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
                ellipse = new DoubleEllipse(rectangle);
            } else {
                rectangle = new DoubleRectangle();
                ellipse = new DoubleEllipse();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(ellipse, areaExcluded, x, y)) {
                return false;
            }
            return isRedMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isRedMatch(color1, color2, colorDistance);
        }
    }

    static class EllipseGreen extends ImageScope {

        public EllipseGreen(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Ellipse;
            this.colorScopeType = ColorScopeType.Green;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
                ellipse = new DoubleEllipse(rectangle);
            } else {
                rectangle = new DoubleRectangle();
                ellipse = new DoubleEllipse();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(ellipse, areaExcluded, x, y)) {
                return false;
            }
            return isGreenMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isGreenMatch(color1, color2, colorDistance);
        }
    }

    static class EllipseBlue extends ImageScope {

        public EllipseBlue(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Ellipse;
            this.colorScopeType = ColorScopeType.Blue;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
                ellipse = new DoubleEllipse(rectangle);
            } else {
                rectangle = new DoubleRectangle();
                ellipse = new DoubleEllipse();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(ellipse, areaExcluded, x, y)) {
                return false;
            }
            return isBlueMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isBlueMatch(color1, color2, colorDistance);
        }
    }

    static class EllipseBrightness extends ImageScope {

        public EllipseBrightness(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Ellipse;
            this.colorScopeType = ColorScopeType.Brightness;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
                ellipse = new DoubleEllipse(rectangle);
            } else {
                rectangle = new DoubleRectangle();
                ellipse = new DoubleEllipse();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(ellipse, areaExcluded, x, y)) {
                return false;
            }
            return isBrightnessMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isBrightnessMatch(color1, color2, hsbDistance);
        }
    }

    static class EllipseSaturation extends ImageScope {

        public EllipseSaturation(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Ellipse;
            this.colorScopeType = ColorScopeType.Saturation;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
                ellipse = new DoubleEllipse(rectangle);
            } else {
                rectangle = new DoubleRectangle();
                ellipse = new DoubleEllipse();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(ellipse, areaExcluded, x, y)) {
                return false;
            }
            return isSaturationMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isSaturationMatch(color1, color2, hsbDistance);
        }
    }

    static class EllipseHue extends ImageScope {

        public EllipseHue(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Ellipse;
            this.colorScopeType = ColorScopeType.Hue;
            if (image != null) {
                rectangle = new DoubleRectangle(image.getWidth() / 4, image.getHeight() / 4,
                        image.getWidth() * 3 / 4, image.getHeight() * 3 / 4);
                ellipse = new DoubleEllipse(rectangle);
            } else {
                rectangle = new DoubleRectangle();
                ellipse = new DoubleEllipse();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(ellipse, areaExcluded, x, y)) {
                return false;
            }
            return isHueMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isHueMatch(color1, color2, hsbDistance);
        }
    }

    static class PolygonColor extends ImageScope {

        public PolygonColor(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Polygon;
            this.colorScopeType = ColorScopeType.Color;
            if (image != null) {

                polygon = new DoublePolygon();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(polygon, areaExcluded, x, y)) {
                return false;
            }
            return isColorMatch2(colors, colorExcluded, colorDistance2, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isColorMatch2(color1, color2, colorDistance2);
        }
    }

    static class PolygonRed extends ImageScope {

        public PolygonRed(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Polygon;
            this.colorScopeType = ColorScopeType.Red;
            if (image != null) {

                polygon = new DoublePolygon();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(polygon, areaExcluded, x, y)) {
                return false;
            }
            return isRedMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isRedMatch(color1, color2, colorDistance);
        }
    }

    static class PolygonGreen extends ImageScope {

        public PolygonGreen(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Polygon;
            this.colorScopeType = ColorScopeType.Green;
            if (image != null) {

                polygon = new DoublePolygon();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(polygon, areaExcluded, x, y)) {
                return false;
            }
            return isGreenMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isGreenMatch(color1, color2, colorDistance);
        }
    }

    static class PolygonBlue extends ImageScope {

        public PolygonBlue(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Polygon;
            this.colorScopeType = ColorScopeType.Blue;
            if (image != null) {

                polygon = new DoublePolygon();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(polygon, areaExcluded, x, y)) {
                return false;
            }
            return isBlueMatch(colors, colorExcluded, colorDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isBlueMatch(color1, color2, colorDistance);
        }
    }

    static class PolygonBrightness extends ImageScope {

        public PolygonBrightness(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Polygon;
            this.colorScopeType = ColorScopeType.Brightness;
            if (image != null) {

                polygon = new DoublePolygon();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(polygon, areaExcluded, x, y)) {
                return false;
            }
            return isBrightnessMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isBrightnessMatch(color1, color2, hsbDistance);
        }
    }

    static class PolygonSaturation extends ImageScope {

        public PolygonSaturation(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Polygon;
            this.colorScopeType = ColorScopeType.Saturation;
            if (image != null) {

                polygon = new DoublePolygon();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(polygon, areaExcluded, x, y)) {
                return false;
            }
            return isSaturationMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isSaturationMatch(color1, color2, hsbDistance);
        }
    }

    static class PolygonHue extends ImageScope {

        public PolygonHue(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Polygon;
            this.colorScopeType = ColorScopeType.Hue;
            if (image != null) {

                polygon = new DoublePolygon();
            }
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (!inShape(polygon, areaExcluded, x, y)) {
                return false;
            }
            return isHueMatch(colors, colorExcluded, hsbDistance, color);
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isHueMatch(color1, color2, hsbDistance);
        }
    }

    static class Outline extends ImageScope {

        public Outline(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Outline;
        }

        @Override
        protected boolean inScope(int x, int y, Color color) {
            if (outline == null) {
                return false;
            }
            return outline.getRGB(x, y) == 0;
        }
    }

    static class Matting extends ImageScope {

        public Matting(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Matting;
            this.colorScopeType = ColorScopeType.Color;
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isColorMatch2(color1, color2, colorDistance2);
        }
    }

    static class MattingColor extends ImageScope {

        public MattingColor(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Matting;
            this.colorScopeType = ColorScopeType.Color;
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isColorMatch2(color1, color2, colorDistance2);
        }
    }

    static class MattingRed extends ImageScope {

        public MattingRed(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Matting;
            this.colorScopeType = ColorScopeType.Red;
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isRedMatch(color1, color2, colorDistance);
        }
    }

    static class MattingGreen extends ImageScope {

        public MattingGreen(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Matting;
            this.colorScopeType = ColorScopeType.Green;
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isGreenMatch(color1, color2, colorDistance);
        }
    }

    static class MattingBlue extends ImageScope {

        public MattingBlue(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Matting;
            this.colorScopeType = ColorScopeType.Blue;
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isBlueMatch(color1, color2, colorDistance);
        }
    }

    static class MattingBrightness extends ImageScope {

        public MattingBrightness(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Matting;
            this.colorScopeType = ColorScopeType.Brightness;
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isBrightnessMatch(color1, color2, hsbDistance);
        }
    }

    static class MattingSaturation extends ImageScope {

        public MattingSaturation(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Matting;
            this.colorScopeType = ColorScopeType.Saturation;
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isSaturationMatch(color1, color2, hsbDistance);
        }
    }

    static class MattingHue extends ImageScope {

        public MattingHue(Image image) {
            this.image = image;
            this.scopeType = ScopeType.Matting;
            this.colorScopeType = ColorScopeType.Hue;
        }

        @Override
        public boolean inColorMatch(Color color1, Color color2) {
            return ImageColor.isHueMatch(color1, color2, hsbDistance);
        }
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

    public void setColorDistance(int colorDistance) {
        this.colorDistance = colorDistance;
        this.colorDistance2 = colorDistance * colorDistance;
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

    public int getColorDistance2() {
        return colorDistance2;
    }

    public void setColorDistance2(int colorDistance2) {
        this.colorDistance2 = colorDistance2;
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

}
