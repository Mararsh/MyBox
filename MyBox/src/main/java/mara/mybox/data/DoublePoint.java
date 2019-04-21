package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2018-11-11 12:23:02
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class DoublePoint {

    private double x, y;

    public DoublePoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static double distance2(double x1, double y1, double x2, double y2) {
        double distanceX = x1 - x2;
        double distanceY = y1 - y2;
        return distanceX * distanceX + distanceY * distanceY;
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        return (double) Math.sqrt(distance2(x1, y1, x2, y2));
    }

    public static double distance2(DoublePoint A, DoublePoint B) {
        double distanceX = A.getX() - B.getX();
        double distanceY = A.getY() - B.getY();
        return distanceX * distanceX + distanceY * distanceY;
    }

    public static double distance(DoublePoint A, DoublePoint B) {
        return (double) Math.sqrt(distance2(A, B));
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

}
