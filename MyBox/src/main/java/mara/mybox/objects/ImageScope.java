package mara.mybox.objects;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import static mara.mybox.tools.FxmlTools.isColorMatch;
import static mara.mybox.tools.FxmlTools.isHueMatch;
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

    private List<Color> colors;
    private int leftX, leftY, rightX, rightY, centerX, centerY, radius, colorDistance, hueDistance;
    private boolean allColors, matchColor, colorExcluded, rectangleExcluded, circleExcluded;
    private Image image;
    private int operationType, areaScopeType;
    private double opacity;
    private boolean indicateScope;

    public static final class OperationType {

        public static final int None = 0;
        public static final int Color = 1;
        public static final int ReplaceColor = 2;
        public static final int Crop = 3;
        public static final int Filters = 4;
        public static final int Transform = 5;

    }

    public static final class AreaScopeType {

        public static final int Invalid = -1;
        public static final int AllArea = 0;
        public static final int Rectangle = 1;
        public static final int Circle = 2;
    }

    public ImageScope() {
        allColors = matchColor = true;
        colors = new ArrayList();
        leftX = leftY = rightX = rightY = centerX = centerY = radius = -1;
        colorExcluded = rectangleExcluded = circleExcluded = false;
        colorDistance = 0;
        hueDistance = 5;
        opacity = 0.1;
    }

    public boolean isAll() {
        return (areaScopeType == AreaScopeType.AllArea) && allColors;
    }

    public boolean inScope(int x, int y, Color color) {
        return inAreaScope(x, y) && inColorScope(color);
    }

    public boolean indicateOpacity(int x, int y, Color color) {
        if (indicateScope) {
            if (!inAreaScope(x, y)) {
                return true;
            }
            if (!inColorScope(color)) {
                return true;
            }
        }
        return false;
    }

    public boolean inAreaScope(int x, int y) {
        switch (areaScopeType) {
            case AreaScopeType.AllArea:
                return true;
            case AreaScopeType.Rectangle:
                if (x >= leftX && x <= rightX && y >= leftY && y <= rightY) {
                    return !rectangleExcluded;
                } else {
                    return rectangleExcluded;
                }
            case AreaScopeType.Circle:
                int distanceX = centerX - x;
                int distaneY = centerY - y;
                if (distanceX * distanceX + distaneY * distaneY <= radius * radius) {
                    return !circleExcluded;
                } else {
                    return circleExcluded;
                }
            default:
                return false;
        }
    }

    public boolean inColorScope(Color color) {
        if (allColors) {
            return true;
        }
        boolean matched = false;
        for (Color oColor : colors) {
            if (matchColor) {
                if (isColorMatch(color, oColor, colorDistance)) {
                    matched = true;
                    break;
                }
            } else {
                if (isHueMatch(color, oColor, hueDistance)) {
                    matched = true;
                    break;
                }
            }
        }
        if (matched) {
            return !colorExcluded;
        } else {
            return colorExcluded;
        }
    }

    public List<Color> getColors() {
        return colors;
    }

    public void setColors(List<Color> colors) {
        this.colors = colors;
    }

    public int getLeftX() {
        return leftX;
    }

    public void setLeftX(int leftX) {
        this.leftX = leftX;
    }

    public int getLeftY() {
        return leftY;
    }

    public void setLeftY(int leftY) {
        this.leftY = leftY;
    }

    public int getRightX() {
        return rightX;
    }

    public void setRightX(int rightX) {
        this.rightX = rightX;
    }

    public int getRightY() {
        return rightY;
    }

    public void setRightY(int rightY) {
        this.rightY = rightY;
    }

    public boolean isAllColors() {
        return allColors;
    }

    public void setAllColors(boolean allColors) {
        this.allColors = allColors;
    }

    public boolean isMatchColor() {
        return matchColor;
    }

    public void setMatchColor(boolean matchColor) {
        this.matchColor = matchColor;
    }

    public boolean isColorExcluded() {
        return colorExcluded;
    }

    public void setColorExcluded(boolean colorExcluded) {
        this.colorExcluded = colorExcluded;
    }

    public int getCenterX() {
        return centerX;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public void setCenterY(int centerY) {
        this.centerY = centerY;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public boolean isRectangleExcluded() {
        return rectangleExcluded;
    }

    public void setRectangleExcluded(boolean rectangleExcluded) {
        this.rectangleExcluded = rectangleExcluded;
    }

    public boolean isCircleExcluded() {
        return circleExcluded;
    }

    public void setCircleExcluded(boolean circleExcluded) {
        this.circleExcluded = circleExcluded;
    }

    public int getAreaScopeType() {
        return areaScopeType;
    }

    public void setAreaScopeType(int areaScopeType) {
        this.areaScopeType = areaScopeType;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public int getOperationType() {
        return operationType;
    }

    public void setOperationType(int operationType) {
        this.operationType = operationType;
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

    public boolean isIndicateScope() {
        return indicateScope;
    }

    public void setIndicateScope(boolean indicateScope) {
        this.indicateScope = indicateScope;
    }

}
