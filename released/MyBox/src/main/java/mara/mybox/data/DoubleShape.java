package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2019-04-02
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public interface DoubleShape {

    DoubleShape cloneValues();

    boolean isValid();

    boolean contains(double x, double y);

    DoubleRectangle getBound();

    DoublePoint getCenter();

    DoubleShape move(double offset);

    DoubleShape move(double offsetX, double offsetY);

    DoubleShape moveTo(double x, double y);

    /*
        static
     */
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

    public static DoubleShape moveTo(DoubleShape shape, double x, double y) {
        DoublePoint center = shape.getCenter();
        double offsetX = x - center.getX();
        double offsetY = y - center.getY();
        if (DoubleShape.changed(offsetX, offsetY)) {
            return shape.move(offsetX, offsetY);
        } else {
            return null;
        }
    }

}
