package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2018-11-11 12:29:29
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class DoubleCircle implements DoubleShape {

    private double centerX, centerY, radius, radius2;

    public DoubleCircle() {

    }

    public DoubleCircle(double x, double y, double r) {
        centerX = x;
        centerY = y;
        radius = r;
        radius2 = r * r;
    }

    @Override
    public DoubleCircle cloneValues() {
        return new DoubleCircle(centerX, centerY, radius);
    }

    @Override
    public boolean isValid() {
        return radius > 0;
    }

    public boolean same(DoubleCircle circle) {
        return centerX == circle.getCenterX() && centerY == circle.getCenterY()
                && radius == circle.getRadius();
    }

    @Override
    public boolean contains(double x, double y) {
        double distanceX = centerX - x;
        double distaneY = centerY - y;
        return distanceX * distanceX + distaneY * distaneY <= radius2;
    }

    @Override
    public DoublePoint getCenter() {
        return new DoublePoint(centerX, centerY);
    }

    @Override
    public DoubleCircle move(double offset) {
        DoubleCircle nCircle = new DoubleCircle(
                centerX + offset, centerY + offset, radius);
        return nCircle;
    }

    @Override
    public DoubleCircle move(double offsetX, double offsetY) {
        DoubleCircle nCircle = new DoubleCircle(
                centerX + offsetX, centerY + offsetY, radius);
        return nCircle;
    }

    @Override
    public DoubleCircle moveTo(double x, double y) {
        DoubleShape moved = DoubleShape.moveTo(this, x, y);
        return moved != null ? (DoubleCircle) moved : null;
    }

    @Override
    public DoubleRectangle getBound() {
        return new DoubleRectangle(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
    }

    public double getCenterX() {
        return centerX;
    }

    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        radius2 = radius * radius;
        this.radius = radius;
    }

}
