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
    private DoublePoint focalSmall, focalBig;
    private double longAxis, shortAxis, focalDistance, centerX, centerY,
            radiusX, radiusY, sumLength;
    private boolean focalsOnX;

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
        focalsOnX = rect.getWidth() >= rect.getHeight();
        DoublePoint center = rect.getCenter();
        centerX = center.getX();
        centerY = center.getY();
        if (focalsOnX) {
            longAxis = rect.getWidth() / 2;
            shortAxis = rect.getHeight() / 2;
            radiusX = longAxis;
            radiusY = shortAxis;
            focalDistance = Math.sqrt(longAxis * longAxis - shortAxis * shortAxis);
            focalSmall = new DoublePoint(centerX - focalDistance, centerY);
            focalBig = new DoublePoint(centerX + focalDistance, centerY);
            sumLength = rect.getWidth();
        } else {
            longAxis = rect.getHeight() / 2;
            shortAxis = rect.getWidth() / 2;
            radiusX = shortAxis;
            radiusY = longAxis;
            focalDistance = Math.sqrt(longAxis * longAxis - shortAxis * shortAxis);
            focalSmall = new DoublePoint(centerX, centerY - focalDistance);
            focalBig = new DoublePoint(centerX, centerY + focalDistance);
            sumLength = rect.getHeight();
        }

    }

    @Override
    public Ellipse2D.Double getShape() {
        return new Ellipse2D.Double(centerX - radiusX, centerY - radiusY, 2 * radiusX, 2 * radiusY);
    }

    @Override
    public boolean isValid() {
        return longAxis > 0 && shortAxis > 0
                && focalBig != null && focalSmall != null
                && rectangle != null && rectangle.isValid();
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
    public boolean contains(double x, double y) {
        double distanceA = DoublePoint.distance(focalBig.getX(), focalBig.getY(), x, y);
        double distanceB = DoublePoint.distance(focalSmall.getX(), focalSmall.getY(), x, y);
        return distanceA + distanceB <= sumLength;
    }

    @Override
    public DoubleRectangle getBound() {
        return new DoubleRectangle(centerX - radiusX, centerY - radiusY, centerX + radiusX, centerY + radiusY);
    }

    public boolean on(double x, double y) {
        double distanceA = DoublePoint.distance(focalBig.getX(), focalBig.getY(), x, y);
        double distanceB = DoublePoint.distance(focalSmall.getX(), focalSmall.getY(), x, y);
        return distanceA + distanceB == sumLength;
    }

    public boolean include(DoublePoint p) {
        double distanceA = DoublePoint.distance(focalBig, p);
        double distanceB = DoublePoint.distance(focalSmall, p);
        return distanceA + distanceB <= sumLength;
    }

    public boolean on(DoublePoint p) {
        double distanceA = DoublePoint.distance(focalBig, p);
        double distanceB = DoublePoint.distance(focalSmall, p);
        return distanceA + distanceB == sumLength;
    }

    @Override
    public DoublePoint getCenter() {
        return new DoublePoint(centerX, centerY);
    }

    @Override
    public DoubleEllipse translateRel(double offset) {
        return translateRel(offset, offset);
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

    public DoubleRectangle getRectangle() {
        return rectangle;
    }

    public void setRectangle(DoubleRectangle rectangle) {
        this.rectangle = rectangle;
    }

    public DoublePoint getFocalSmall() {
        return focalSmall;
    }

    public void setFocalSmall(DoublePoint focalSmall) {
        this.focalSmall = focalSmall;
    }

    public DoublePoint getFocalBig() {
        return focalBig;
    }

    public void setFocalBig(DoublePoint focalBig) {
        this.focalBig = focalBig;
    }

    public double getLongAxis() {
        return longAxis;
    }

    public void setLongAxis(double longAxis) {
        this.longAxis = longAxis;
    }

    public double getShortAxis() {
        return shortAxis;
    }

    public void setShortAxis(double shortAxis) {
        this.shortAxis = shortAxis;
    }

    public double getFocalDistance() {
        return focalDistance;
    }

    public void setFocalDistance(double focalDistance) {
        this.focalDistance = focalDistance;
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

    public double getSumLength() {
        return sumLength;
    }

    public void setSumLength(double sumLength) {
        this.sumLength = sumLength;
    }

    public boolean isFocalsOnX() {
        return focalsOnX;
    }

    public void setFocalsOnX(boolean focalsOnX) {
        this.focalsOnX = focalsOnX;
    }

    public double getRadiusX() {
        return radiusX;
    }

    public void setRadiusX(double radiusX) {
        this.radiusX = radiusX;
    }

    public double getRadiusY() {
        return radiusY;
    }

    public void setRadiusY(double radiusY) {
        this.radiusY = radiusY;
    }

}
