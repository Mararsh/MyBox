package mara.mybox.data;

import java.awt.geom.Arc2D;

/**
 * @Author Mara
 * @CreateDate 2023-7-31
 * @License Apache License Version 2.0
 */
public class DoubleArc implements DoubleShape {

    private double centerX, centerY, radiusX, radiusY,
            width, height, x, y, startAngle, extentAngle;
    private int type;

    public DoubleArc(DoubleRectangle rect, double startAngle, double extentAngle, int type) {
        x = rect.getSmallX();
        y = rect.getSmallY();
        width = rect.getWidth();
        height = rect.getHeight();
        DoublePoint center = DoubleShape.getCenter(rect);
        centerX = center.getX();
        centerY = center.getY();
        radiusX = rect.getWidth() / 2;
        radiusY = rect.getHeight() / 2;
        this.startAngle = startAngle;
        this.extentAngle = extentAngle;
        this.type = type;
    }

    public DoubleArc(double x, double y, double width, double height,
            double startAngle, double extentAngle, int type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.startAngle = startAngle;
        this.extentAngle = extentAngle;
        this.type = type;
        radiusX = width / 2;
        radiusY = height / 2;
        centerX = x + radiusX;
        centerY = y + radiusY;
    }

    @Override
    public Arc2D.Double getShape() {
        return new Arc2D.Double(x, y, width, height, startAngle, extentAngle, type);
    }

    @Override
    public DoubleArc cloneValues() {
        return new DoubleArc(x, y, width, height, startAngle, extentAngle, type);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return !isValid();
    }

    @Override
    public DoubleArc translateRel(double offsetX, double offsetY) {
        return new DoubleArc(x + offsetX, y + offsetY,
                width, height, startAngle, extentAngle, type);
    }

    @Override
    public DoubleArc translateAbs(double x, double y) {
        DoubleShape moved = DoubleShape.translateAbs(this, x, y);
        return moved != null ? (DoubleArc) moved : null;
    }

    /*
        get
     */
    public double getCenterX() {
        return centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public double getRadiusX() {
        return radiusX;
    }

    public double getRadiusY() {
        return radiusY;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getStartAngle() {
        return startAngle;
    }

    public double getExtentAngle() {
        return extentAngle;
    }

    public int getType() {
        return type;
    }

}
