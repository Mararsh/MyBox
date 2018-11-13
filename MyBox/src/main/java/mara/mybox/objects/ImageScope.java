package mara.mybox.objects;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import static mara.mybox.fxml.FxmlImageTools.isColorMatch;
import static mara.mybox.fxml.FxmlImageTools.isHueMatch;
import static mara.mybox.objects.AppVaribles.getMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-8-1 16:22:41
 * @Version 1.0
 * @License Apache License Version 2.0
 */
public class ImageScope {

    private static final Logger logger = LogManager.getLogger();

    private OperationType operationType;
    private ScopeType scopeType;
    private AreaScopeType areaScopeType;
    private ColorScopeType colorScopeType;
    private List<Color> colors;
    private List<Point> points;
    private Rectangle rectangle;
    private Circle circle;
    private int colorDistance, hueDistance;
    private boolean mattingExcluded;
    private Image image;
    private double opacity;

    public enum OperationType {
        Invalid, Color, ReplaceColor, Crop, Filters, Effects, Convolution
    }

    public enum ScopeType {
        Invalid, All, Matting, Color, Hue, Rectangle, Circle, Settings
    }

    public enum AreaScopeType {
        Invalid, AllArea, Rectangle, RectangleExlcuded, Circle, CircleExcluded
    }

    public enum ColorScopeType {
        Invalid, AllColor, Color, ColorExcluded, Hue, HueExcluded
    }

    public ImageScope() {
        operationType = OperationType.Invalid;
        scopeType = ScopeType.All;
        areaScopeType = AreaScopeType.AllArea;
        colorScopeType = ColorScopeType.AllColor;
        colors = new ArrayList();
        points = new ArrayList();
        rectangle = new Rectangle();
        circle = new Circle();
        mattingExcluded = false;
        colorDistance = 20;
        hueDistance = 20;
        opacity = 0.3;
    }

    public ImageScope(Image image) {
        operationType = OperationType.Invalid;
        scopeType = ScopeType.All;
        areaScopeType = AreaScopeType.AllArea;
        colorScopeType = ColorScopeType.AllColor;
        colors = new ArrayList();
        points = new ArrayList();
        mattingExcluded = false;
        colorDistance = 20;
        hueDistance = 20;
        opacity = 0.3;
        if (image != null) {
            this.image = image;
            rectangle = new Rectangle((int) (image.getWidth() / 4), (int) (image.getHeight() / 4),
                    (int) (image.getWidth() * 3 / 4), (int) (image.getHeight() * 3 / 4));
            circle = new Circle((int) (image.getWidth() / 2), (int) (image.getHeight() / 2),
                    (int) (image.getHeight() / 4));
        } else {
            rectangle = new Rectangle();
            circle = new Circle();
        }
    }

    public ImageScope cloneValues() {
        ImageScope scope = new ImageScope(image);
        scope.setOperationType(operationType);
        scope.setScopeType(scopeType);
        scope.setAreaScopeType(areaScopeType);
        scope.setColorScopeType(colorScopeType);
        List<Point> npoints = new ArrayList<>();
        if (points != null) {
            npoints.addAll(points);
        }
        scope.setPoints(npoints);
        List<Color> ncolors = new ArrayList<>();
        if (colors != null) {
            ncolors.addAll(colors);
        }
        scope.setColors(ncolors);
        scope.setRectangle(rectangle.cloneValues());
        scope.setCircle(circle.cloneValues());
        scope.setColorDistance(colorDistance);
        scope.setHueDistance(hueDistance);
        scope.setMattingExcluded(mattingExcluded);
        scope.setOpacity(opacity);
        return scope;
    }

    public boolean inScope(int x, int y, Color color) {
        return inAreaScope(x, y) && inColorScope(color);
    }

    public boolean inAreaScope(int x, int y) {
        switch (areaScopeType) {
            case AllArea:
                return true;
            case Rectangle:
                return rectangle.include(x, y);
            case RectangleExlcuded:
                return !rectangle.include(x, y);
            case Circle:
                return circle.include(x, y);
            case CircleExcluded:
                return !circle.include(x, y);
            default:
                return false;
        }
    }

    public boolean inColorScope(Color color) {
        switch (colorScopeType) {
            case AllColor:
                return true;
            case Color:
                for (Color oColor : colors) {
                    if (isColorMatch(color, oColor, colorDistance)) {
                        return true;
                    }
                }
                return false;
            case ColorExcluded:
                for (Color oColor : colors) {
                    if (isColorMatch(color, oColor, colorDistance)) {
                        return false;
                    }
                }
                return true;
            case Hue:
                for (Color oColor : colors) {
                    if (isHueMatch(color, oColor, hueDistance)) {
                        return true;
                    }
                }
                return false;
            case HueExcluded:
                for (Color oColor : colors) {
                    if (isHueMatch(color, oColor, hueDistance)) {
                        return false;
                    }
                }
                return true;
            default:
                return false;
        }
    }

    public void addPoints(Point point) {
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

    public void addPoints(int x, int y) {
        if (x < 0 || y < 0) {
            return;
        }
        Point point = new Point(x, y);
        addPoints(point);
    }

    public void clearPoints() {
        points = new ArrayList<>();
    }

    public void addColor(Color color) {
        if (color == null) {
            return;
        }
        if (colors == null) {
            colors = new ArrayList<>();
        }
        if (!colors.contains(color)) {
            colors.add(color);
        }
    }

    public void clearColors() {
        colors = new ArrayList<>();
    }

    public void setCircleCenter(int x, int y) {
        if (circle == null) {
            circle = new Circle();
        }
        circle.setCenterX(x);
        circle.setCenterY(y);
    }

    public void setCircleRadius(int r) {
        if (circle == null) {
            circle = new Circle();
        }
        circle.setRadius(r);
    }

    public String getScopeText() {
        String s = "";
        if (scopeType == ImageScope.ScopeType.All) {
            s = getMessage("WholeImage");
        } else if (scopeType == ImageScope.ScopeType.Matting) {
            String pointsString = "";
            if (points.isEmpty()) {
                pointsString += getMessage("None");
            } else {
                for (Point p : points) {
                    pointsString += "(" + p.getX() + "," + p.getY() + ") ";
                }
            }
            s = getMessage("Points") + ":" + pointsString;
            s += " " + getMessage("ColorDistance") + ":" + colorDistance;
        } else {
            switch (areaScopeType) {
                case AllArea:
                    s = getMessage("AllArea");
                    break;
                case Rectangle:
                    s = getMessage("SelectedRectangle") + ":("
                            + rectangle.getLeftX() + "," + rectangle.getLeftY()
                            + ")-(" + rectangle.getRightX() + "," + rectangle.getRightY() + ")";
                    break;
                case RectangleExlcuded:
                    s = getMessage("SelectedRectangle") + ":("
                            + rectangle.getLeftX() + "," + rectangle.getLeftY()
                            + ")-(" + rectangle.getRightX() + "," + rectangle.getRightY() + ")";
                    s += " " + getMessage("Excluded");
                    break;
                case Circle:
                    s = getMessage("SelectedCircle") + ":("
                            + circle.getCenterX() + "," + circle.getCenterY()
                            + ")-" + circle.getRadius();
                    break;
                case CircleExcluded:
                    s = getMessage("SelectedCircle") + ":("
                            + circle.getCenterX() + "," + circle.getCenterY()
                            + ")-" + circle.getRadius();
                    s += " " + getMessage("Excluded");
                    break;
                default:
                    break;
            }

            String colorString = "";
            if (colors.isEmpty()) {
                colorString += getMessage("None") + " ";
            } else {
                for (Color c : colors) {
                    colorString += c.toString() + " ";
                }
            }
            switch (colorScopeType) {
                case AllColor:
                    s += " " + getMessage("AllColors");
                    break;
                case Color:
                    s += " " + getMessage("SelectedColors") + ":" + colorString;
                    s += getMessage("ColorDistance") + ":" + colorDistance;
                    break;
                case ColorExcluded:
                    s += " " + getMessage("SelectedColors") + ":" + colorString;
                    s += getMessage("ColorDistance") + ":" + colorDistance;
                    s += " " + getMessage("Excluded");
                    break;
                case Hue:
                    s += " " + getMessage("SelectedColors") + ":" + colorString;
                    s += getMessage("HueDistance") + ":" + hueDistance;
                    break;
                case HueExcluded:
                    s += " " + getMessage("SelectedColors") + ":" + colorString;
                    s += getMessage("HueDistance") + ":" + hueDistance;
                    s += " " + getMessage("Excluded");
                    break;
                default:
                    break;
            }
        }
        return s;
    }

    public List<Color> getColors() {
        return colors;
    }

    public void setColors(List<Color> colors) {
        this.colors = colors;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setCircle(Circle circle) {
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
    }

    public int getHueDistance() {
        return hueDistance;
    }

    public void setHueDistance(int hueDistance) {
        this.hueDistance = hueDistance;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public ScopeType getScopeType() {
        return scopeType;
    }

    public void setScopeType(ScopeType scopeType) {
        this.scopeType = scopeType;
    }

    public AreaScopeType getAreaScopeType() {
        return areaScopeType;
    }

    public void setAreaScopeType(AreaScopeType areaScopeType) {
        this.areaScopeType = areaScopeType;
    }

    public ColorScopeType getColorScopeType() {
        return colorScopeType;
    }

    public void setColorScopeType(ColorScopeType colorScopeType) {
        this.colorScopeType = colorScopeType;
    }

    public boolean isMattingExcluded() {
        return mattingExcluded;
    }

    public void setMattingExcluded(boolean mattingExcluded) {
        this.mattingExcluded = mattingExcluded;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

}
