package mara.mybox.data;

import java.awt.geom.Ellipse2D;

/**
 * @Author Mara
 * @CreateDate 2018-11-11 12:29:29
 * @License Apache License Version 2.0
 */
// https://en.wikipedia.org/wiki/Ellipse
public class DoubleEllipse implements DoubleShape {

    protected double x, y, width, height;

    public DoubleEllipse() {
    }

    public static DoubleEllipse xywh(double x, double y, double width, double height) {
        DoubleEllipse e = new DoubleEllipse();
        e.setX(x);
        e.setY(y);
        e.setWidth(width);
        e.setHeight(height);
        return e;
    }

    public static DoubleEllipse xy12(double x1, double y1, double x2, double y2) {
        DoubleEllipse e = new DoubleEllipse();
        e.setX(Math.min(x1, x2));
        e.setY(Math.min(y1, y2));
        e.setWidth(Math.abs(x2 - x1));
        e.setHeight(Math.abs(y2 - y1));
        return e;
    }

    public static DoubleEllipse rect(DoubleRectangle rect) {
        DoubleEllipse e = new DoubleEllipse();
        e.setX(rect.getX());
        e.setY(rect.getY());
        e.setWidth(rect.getWidth());
        e.setHeight(rect.getHeight());
        return e;
    }

    public static DoubleEllipse ellipse(double centerX, double centerY, double radiusX, double radiusY) {
        DoubleEllipse e = new DoubleEllipse();
        e.setX(centerX - radiusX);
        e.setY(centerY - radiusY);
        e.setWidth(radiusX * 2);
        e.setHeight(radiusY * 2);
        return e;
    }

    public double getCenterX() {
        return x + width * 0.5;
    }

    public double getCenterY() {
        return y + height * 0.5;
    }

    public double getRadiusX() {
        return width * 0.5;
    }

    public double getRadiusY() {
        return height * 0.5;
    }

    // exclude maxX and maxY       
    public double getMaxX() {
        return x + width;
    }

    public double getMaxY() {
        return y + height;
    }

    public void setMaxX(double maxX) {
        width = Math.abs(maxX - x);
    }

    public void setMaxY(double maxY) {
        height = Math.abs(maxY - y);
    }

    public void changeX(double nx) {
        width = width + x - nx;
        x = nx;
    }

    public void changeY(double ny) {
        height = height + y - ny;
        y = ny;
    }

    @Override
    public Ellipse2D.Double getShape() {
        return new Ellipse2D.Double(x, y, width, height);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return !isValid() || width <= 0 || height <= 0;
    }

    @Override
    public DoubleEllipse cloneValues() {
        return xywh(x, y, width, height);
    }

    @Override
    public boolean translateRel(double offsetX, double offsetY) {
        x += offsetX;
        y += offsetY;
        return true;
    }

    /*
        get
     */
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }


    /*
        set
     */
    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setHeight(double height) {
        this.height = height;
    }

}
