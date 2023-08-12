package mara.mybox.data;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

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

    boolean translateRel(double offsetX, double offsetY);

    String name();

    /*
        static
     */
    public static enum ShapeType {
        Line, Rectangle, Circle, Ellipse, Polygon, Polyline, Polylines,
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

    public static boolean translateCenterAbs(DoubleShape shape, double x, double y) {
        DoublePoint center = getCenter(shape);
        double offsetX = x - center.getX();
        double offsetY = y - center.getY();
        if (DoubleShape.changed(offsetX, offsetY)) {
            return shape.translateRel(offsetX, offsetY);
        }
        return false;
    }

    public static boolean translateRel(DoubleShape shape, double offsetX, double offsetY) {
        if (DoubleShape.changed(offsetX, offsetY)) {
            return shape.translateRel(offsetX, offsetY);
        }
        return false;
    }

    // notice bound may truncate values
    public static Rectangle2D getBound(DoubleShape shape) {
        return shape.getShape().getBounds2D();
    }

    public static boolean contains(DoubleShape shape, double x, double y) {
        return shape.isValid() && shape.getShape().contains(x, y);
    }

    public static DoublePoint getCenter(DoubleShape shape) {
        Rectangle2D bound = getBound(shape);
        return new DoublePoint(bound.getCenterX(), bound.getCenterY());
    }

}
