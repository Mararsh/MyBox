package mara.mybox.data;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.tools.DoubleTools.imageScale;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-04-02
 * @License Apache License Version 2.0
 */
public interface DoubleShape {

    DoubleShape copy();

    boolean isValid();

    boolean isEmpty();

    Shape getShape();

    boolean translateRel(double offsetX, double offsetY);

    boolean scale(double scaleX, double scaleY);

    String name();

    String svgRel();

    String svgAbs();

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

    public static boolean translateCenterAbs(DoubleShape shapeData, double x, double y) {
        DoublePoint center = getCenter(shapeData);
        double offsetX = x - center.getX();
        double offsetY = y - center.getY();
        if (DoubleShape.changed(offsetX, offsetY)) {
            return shapeData.translateRel(offsetX, offsetY);
        }
        return false;
    }

    public static boolean translateRel(DoubleShape shapeData, double offsetX, double offsetY) {
        if (DoubleShape.changed(offsetX, offsetY)) {
            return shapeData.translateRel(offsetX, offsetY);
        }
        return false;
    }

    public static boolean scale(DoubleShape shapeData, double scaleX, double scaleY) {
        try {
            if (shapeData == null) {
                return true;
            }
            return shapeData.scale(scaleX, scaleY);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static DoublePath rorate(DoubleShape shapeData, double angle, double x, double y) {
        try {
            if (shapeData == null) {
                return null;
            }
            AffineTransform t = AffineTransform.getRotateInstance(Math.toRadians(angle), x, y);
            Shape shape = t.createTransformedShape(shapeData.getShape());
            return DoublePath.shapeToPathData(shape);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DoublePath shear(DoubleShape shapeData, double x, double y) {
        try {
            if (shapeData == null) {
                return null;
            }
            AffineTransform t = AffineTransform.getShearInstance(x, y);
            Shape shape = t.createTransformedShape(shapeData.getShape());
            return DoublePath.shapeToPathData(shape);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DoublePath pathData(DoubleShape shapeData) {
        try {
            if (shapeData == null) {
                return null;
            }
            return DoublePath.shapeToPathData(shapeData.getShape());
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    // notice bound may truncate values
    public static Rectangle2D getBound(DoubleShape shapeData) {
        return shapeData.getShape().getBounds2D();
    }

    public static boolean contains(DoubleShape shapeData, double x, double y) {
        return shapeData.isValid() && shapeData.getShape().contains(x, y);
    }

    public static DoublePoint getCenter(DoubleShape shapeData) {
        Rectangle2D bound = getBound(shapeData);
        return new DoublePoint(bound.getCenterX(), bound.getCenterY());
    }

    public static String values(DoubleShape shapeData) {
        Rectangle2D bounds = getBound(shapeData);
        return shapeData.name() + "\n"
                + message("LeftTop") + ": " + imageScale(bounds.getMinX()) + ", " + imageScale(bounds.getMinY()) + "\n"
                + message("RightBottom") + ": " + imageScale(bounds.getMaxX()) + ", " + imageScale(bounds.getMaxY()) + "\n"
                + message("Center") + ": " + imageScale(bounds.getCenterX()) + ", " + imageScale(bounds.getCenterY()) + "\n"
                + message("Width") + ": " + imageScale(bounds.getWidth()) + "  " + message("Height") + ": " + imageScale(bounds.getHeight());
    }

}
