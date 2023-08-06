package mara.mybox.data;

import java.awt.geom.Ellipse2D;

/**
 * @Author Mara
 * @CreateDate 2018-11-11 12:29:29
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
// https://en.wikipedia.org/wiki/Ellipse
public class DoubleEllipse implements DoubleShape {

    private double centerX, centerY, radiusX, radiusY;

    public DoubleEllipse() {
    }

    public static DoubleEllipse rect(double x1, double y1, double x2, double y2) {
        DoubleEllipse e = new DoubleEllipse();
        e.setCenterX((x1 + x2) / 2);
        e.setCenterY((y1 + y2) / 2);
        e.setRadiusX(Math.abs(x2 - x1) / 2);
        e.setRadiusY(Math.abs(y2 - y1) / 2);
        return e;
    }

    public static DoubleEllipse rect(DoubleRectangle rect) {
        DoubleEllipse e = new DoubleEllipse();
        DoublePoint c = DoubleShape.getCenter(rect);
        e.setCenterX(c.getX());
        e.setCenterY(c.getY());
        e.setRadiusX(rect.getWidth() / 2);
        e.setRadiusY(rect.getHeight() / 2);
        return e;
    }

    public static DoubleEllipse ellipse(double centerX, double centerY, double radiusX, double radiusY) {
        DoubleEllipse e = new DoubleEllipse();
        e.setCenterX(centerX);
        e.setCenterY(centerY);
        e.setRadiusX(radiusX);
        e.setRadiusY(radiusY);
        return e;
    }

    public double getX1() {
        return centerX - radiusX;
    }

    public double getY1() {
        return centerY - radiusY;
    }

    public double getX2() {
        return centerX + radiusX;
    }

    public double getY2() {
        return centerY + radiusY;
    }

    @Override
    public Ellipse2D.Double getShape() {
        return new Ellipse2D.Double(centerX - radiusX, centerY - radiusY, 2 * radiusX, 2 * radiusY);
    }

    @Override
    public boolean isValid() {
        return radiusX > 0 && radiusY > 0;
    }

    @Override
    public boolean isEmpty() {
        return !isValid();
    }

    @Override
    public DoubleEllipse cloneValues() {
        return ellipse(centerX, centerY, radiusX, radiusY);
    }

    @Override
    public DoubleEllipse translateRel(double offsetX, double offsetY) {
        DoubleEllipse nEllipse = ellipse(
                centerX + offsetX, centerY + offsetY,
                radiusX, radiusY);
        return nEllipse;
    }

    @Override
    public DoubleEllipse translateAbs(double x, double y) {
        DoubleShape moved = DoubleShape.translateAbs(this, x, y);
        return moved != null ? (DoubleEllipse) moved : null;
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

    /*
        set
     */
    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }

    public void setRadiusX(double radiusX) {
        this.radiusX = radiusX;
    }

    public void setRadiusY(double radiusY) {
        this.radiusY = radiusY;
    }

}
