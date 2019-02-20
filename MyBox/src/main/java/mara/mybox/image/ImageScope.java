package mara.mybox.image;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;
import mara.mybox.data.IntCircle;
import mara.mybox.data.IntPoint;
import mara.mybox.data.IntRectangle;
import static mara.mybox.value.AppVaribles.getMessage;

/**
 * @Author Mara
 * @CreateDate 2018-8-1 16:22:41
 * @Version 1.0
 * @License Apache License Version 2.0
 */
public class ImageScope {

    private ScopeType scopeType;
    private ColorScopeType colorScopeType;
    private List<Color> colors;
    private List<IntPoint> points;
    private IntRectangle rectangle;
    private IntCircle circle;
    private int colorDistance, colorDistance2;
    private float hsbDistance;
    private boolean areaExcluded, colorExcluded;
    private Image image;
    private double opacity;

    public enum ScopeType {
        Invalid, All, Matting, Rectangle, Circle, Color, RectangleColor, CircleColor
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

    private void init() {
        scopeType = ScopeType.All;
        colorScopeType = ColorScopeType.AllColor;
        colors = new ArrayList();
        points = new ArrayList();
        colorDistance = 50;
        colorDistance2 = colorDistance * colorDistance;
        hsbDistance = 0.5f;
        opacity = 0.3;
        circle = new IntCircle();
        if (image != null) {
            rectangle = new IntRectangle((int) (image.getWidth() / 4), (int) (image.getHeight() / 4),
                    (int) (image.getWidth() * 3 / 4), (int) (image.getHeight() * 3 / 4));
            circle = new IntCircle((int) (image.getWidth() / 2), (int) (image.getHeight() / 2),
                    (int) (image.getHeight() / 4));
        } else {
            rectangle = new IntRectangle();
            circle = new IntCircle();
        }

    }

    public ImageScope cloneValues() {
        ImageScope scope = new ImageScope(image);
        scope.setScopeType(scopeType);
        scope.setColorScopeType(colorScopeType);
        List<IntPoint> npoints = new ArrayList<>();
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
        scope.setColorDistance2(colorDistance2);
        scope.setHsbDistance(hsbDistance);
        scope.setColorExcluded(colorExcluded);
        scope.setAreaExcluded(areaExcluded);
        scope.setOpacity(opacity);
        return scope;
    }

    public boolean inScope(int x, int y, Color color) {
        if (null == scopeType) {
            return true;
        }
        boolean inArea;
        switch (scopeType) {
            case All:
                return true;
            case Rectangle:
                if (areaExcluded) {
                    return !rectangle.include(x, y);
                } else {
                    return rectangle.include(x, y);
                }
            case Circle:
                if (areaExcluded) {
                    return !circle.include(x, y);
                } else {
                    return circle.include(x, y);
                }
            case Color:
                return inColorScope(color);
            case RectangleColor:
                if (areaExcluded) {
                    inArea = !rectangle.include(x, y);
                } else {
                    inArea = rectangle.include(x, y);
                }
                if (!inArea) {
                    return false;
                }
                return inColorScope(color);
            case CircleColor:
                if (areaExcluded) {
                    inArea = !circle.include(x, y);
                } else {
                    inArea = circle.include(x, y);
                }
                if (!inArea) {
                    return false;
                }
                return inColorScope(color);
            default:
                return true;
        }
    }

    public boolean inColorScope(Color color) {
        switch (colorScopeType) {
            case AllColor:
                return true;
            case Color:
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
            case Red:
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
            case Green:
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
            case Blue:
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
            case Brightness:
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
            case Saturation:
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
            case Hue:
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
            default:
                return false;
        }
    }

    public boolean inColorMatch2(Color color1, Color color2) {
        switch (colorScopeType) {
            case AllColor:
                return true;
            case Color:
                if (colorExcluded) {
                    return !ImageColor.isColorMatch2(color1, color2, colorDistance2);
                } else {
                    return ImageColor.isColorMatch2(color1, color2, colorDistance2);
                }
            case Red:
                if (colorExcluded) {
                    return !ImageColor.isRedMatch(color1, color2, colorDistance);
                } else {
                    return ImageColor.isRedMatch(color1, color2, colorDistance);
                }
            case Green:
                if (colorExcluded) {
                    return !ImageColor.isGreenMatch(color1, color2, colorDistance);
                } else {
                    return ImageColor.isGreenMatch(color1, color2, colorDistance);
                }
            case Blue:
                if (colorExcluded) {
                    return !ImageColor.isBlueMatch(color1, color2, colorDistance);
                } else {
                    return ImageColor.isBlueMatch(color1, color2, colorDistance);
                }
            case Brightness:
                if (colorExcluded) {
                    return !ImageColor.isBrightnessMatch(color1, color2, hsbDistance);
                } else {
                    return ImageColor.isBrightnessMatch(color1, color2, hsbDistance);
                }
            case Saturation:
                if (colorExcluded) {
                    return !ImageColor.isSaturationMatch(color1, color2, hsbDistance);
                } else {
                    return ImageColor.isSaturationMatch(color1, color2, hsbDistance);
                }
            case Hue:
                if (colorExcluded) {
                    return !ImageColor.isHueMatch(color1, color2, hsbDistance);
                } else {
                    return ImageColor.isHueMatch(color1, color2, hsbDistance);
                }
            default:
                return false;
        }
    }

    public boolean inColorMatch(Color color1, Color color2) {
        boolean isMatch;
        switch (colorScopeType) {
            case AllColor:
                isMatch = true;
                break;
            case Color:
                isMatch = ImageColor.isColorMatch2(color1, color2, colorDistance2);
                break;
            case Red:
                isMatch = ImageColor.isRedMatch(color1, color2, colorDistance);
                break;
            case Green:
                isMatch = ImageColor.isGreenMatch(color1, color2, colorDistance);
                break;
            case Blue:
                isMatch = ImageColor.isBlueMatch(color1, color2, colorDistance);
                break;
            case Brightness:
                isMatch = ImageColor.isBrightnessMatch(color1, color2, hsbDistance);
                break;
            case Saturation:
                isMatch = ImageColor.isSaturationMatch(color1, color2, hsbDistance);
                break;
            case Hue:
                isMatch = ImageColor.isHueMatch(color1, color2, hsbDistance);
                break;
            default:
                isMatch = false;
        }
        return isMatch;
    }

    public void addPoints(IntPoint point) {
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
        IntPoint point = new IntPoint(x, y);
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
            circle = new IntCircle();
        }
        circle.setCenterX(x);
        circle.setCenterY(y);
    }

    public void setCircleRadius(int r) {
        if (circle == null) {
            circle = new IntCircle();
        }
        circle.setRadius(r);
    }

    public String getScopeText() {
        String s = "";
        if (null != scopeType) {
            switch (scopeType) {
                case All:
                    s = getMessage("WholeImage");
                    break;
                case Matting:
                    String pointsString = "";
                    if (points.isEmpty()) {
                        pointsString += getMessage("None");
                    } else {
                        for (IntPoint p : points) {
                            pointsString += "(" + p.getX() + "," + p.getY() + ") ";
                        }
                    }
                    s = getMessage("Points") + ":" + pointsString;
                    s += " " + getMessage("ColorDistance") + ":" + colorDistance;
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
            case Red:
            case Green:
            case Blue:
                s += " " + getMessage("SelectedColors") + ":" + colorString;
                s += getMessage("ColorDistance") + ":" + colorDistance;
                if (colorExcluded) {
                    s += " " + getMessage("Excluded");
                }
                break;
            case Brightness:
            case Saturation:
                s += " " + getMessage("SelectedColors") + ":" + colorString;
                s += getMessage("ColorDistance") + ":" + (int) (hsbDistance * 100);
                if (colorExcluded) {
                    s += " " + getMessage("Excluded");
                }
                break;
            case Hue:
                s += " " + getMessage("SelectedColors") + ":" + colorString;
                s += getMessage("HueDistance") + ":" + (int) (hsbDistance * 360);
                if (colorExcluded) {
                    s += " " + getMessage("Excluded");
                }
                break;
            default:
                break;
        }
        return s;
    }

    public List<Color> getColors() {
        return colors;
    }

    public void setColors(List<Color> colors) {
        this.colors = colors;
    }

    public IntRectangle getRectangle() {
        return rectangle;
    }

    public void setRectangle(IntRectangle rectangle) {
        this.rectangle = rectangle;
    }

    public IntCircle getCircle() {
        return circle;
    }

    public void setCircle(IntCircle circle) {
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

}
