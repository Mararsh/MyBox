package mara.mybox.data;

import java.awt.geom.Ellipse2D;

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
    public Ellipse2D.Double getShape() {
        return new Ellipse2D.Double(centerX - radius, centerY - radius, 2 * radius, 2 * radius);
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
    public DoubleCircle translateRel(double offsetX, double offsetY) {
        return new DoubleCircle(centerX + offsetX, centerY + offsetY, radius);
    }

    @Override
    public DoubleCircle translateAbs(double x, double y) {
        DoubleShape moved = DoubleShape.translateAbs(this, x, y);
        return moved != null ? (DoubleCircle) moved : null;
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
