package mara.mybox.data;

import java.awt.Rectangle;
import java.awt.Shape;

/**
 * @Author Mara
 * @CreateDate 2019-04-02
 * @License Apache License Version 2.0
 */
public interface DoubleShape {

    DoubleShape cloneValues();

    boolean isValid();

    boolean isEmpty();

    Shape getShape();

    DoubleShape translateRel(double offsetX, double offsetY);

    DoubleShape translateAbs(double x, double y);

    /*
        static
     */
    public static enum ShapeType {
        Line, Rectangle, Circle, Ellipse, Polygon, Polyline, Lines,
        Cubic, Quadratic, Arc, Path, Text;
    }

    public static final double ChangeThreshold = 0.01;

    public static boolean changed(double offsetX, double offsetY) {
        return Math.abs(offsetX) > ChangeThreshold || Math.abs(offsetY) > ChangeThreshold;
    }

    public static boolean changed(DoublePoint p1, DoublePoint p2) {
        if (p1 == null || p2 == null) {
            return false;
        }
        return changed(p1.getX() - p2.getX(), p1.getY() - p2.getY());
    }

    public static DoubleShape translateAbs(DoubleShape shape, double x, double y) {
        DoublePoint center = getCenter(shape);
        double offsetX = x - center.getX();
        double offsetY = y - center.getY();
        if (DoubleShape.changed(offsetX, offsetY)) {
            return shape.translateRel(offsetX, offsetY);
        } else {
            return null;
        }
    }

    public static Rectangle getBound(DoubleShape shape) {
        return shape.getShape().getBounds();
    }

    public static boolean contains(DoubleShape shape, double x, double y) {
        return shape.isValid() && shape.getShape().contains(x, y);
    }

    public static DoublePoint getCenter(DoubleShape shape) {
        Rectangle bound = getBound(shape);
        return new DoublePoint((bound.getMinX() + bound.getMaxX()) / 2, (bound.getMinY() + bound.getMaxY()) / 2);
    }

}
