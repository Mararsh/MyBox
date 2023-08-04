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

    private DoubleRectangle rectangle;
    private double centerX, centerY, radiusX, radiusY;

    public DoubleEllipse() {

    }

    public DoubleEllipse(double x1, double y1, double x2, double y2) {
        makeEllipse(new DoubleRectangle(x1, y1, x2, y2));
    }

    public DoubleEllipse(DoubleRectangle rect) {
        makeEllipse(rect);
    }

    private void makeEllipse(DoubleRectangle rect) {
        if (rect == null || !rect.isValid()) {
            return;
        }
        rectangle = rect;
        DoublePoint center = DoubleShape.getCenter(rect);
        centerX = center.getX();
        centerY = center.getY();
        radiusX = rect.getWidth() / 2;
        radiusY = rect.getHeight() / 2;
    }

    @Override
    public Ellipse2D.Double getShape() {
        return new Ellipse2D.Double(centerX - radiusX, centerY - radiusY, 2 * radiusX, 2 * radiusY);
    }

    @Override
    public boolean isValid() {
        return rectangle != null && rectangle.isValid();
    }

    @Override
    public boolean isEmpty() {
        return !isValid();
    }

    public boolean same(DoubleEllipse ellipse) {
        return centerX == ellipse.getCenterX()
                && centerY == ellipse.getCenterY()
                && radiusX == ellipse.getRadiusX()
                && radiusY == ellipse.getRadiusY();
    }

    @Override
    public DoubleEllipse cloneValues() {
        return new DoubleEllipse(rectangle);
    }

    @Override
    public DoubleEllipse translateRel(double offsetX, double offsetY) {
        DoubleEllipse nEllipse = new DoubleEllipse(
                centerX - radiusX + offsetX,
                centerY - radiusY + offsetY,
                centerX + radiusX + offsetX,
                centerY + radiusY + offsetY);
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
    public DoubleRectangle getRectangle() {
        return rectangle;
    }

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

}
