package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2018-11-11 12:29:29
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
// https://en.wikipedia.org/wiki/Ellipse
public class IntEllipse {

    private IntRectangle rectangle;
    private IntPoint focalSmall, focalBig;
    private int longAxis, shortAxis, focalDistance, centerX, centerY,
            radiusX, radiusY, sumLength;
    private boolean focalsOnX;

    public IntEllipse() {

    }

    public IntEllipse(int x1, int y1, int x2, int y2) {
        makeEllipse(new IntRectangle(x1, y1, x2, y2));
    }

    public IntEllipse(IntRectangle rect) {
        makeEllipse(rect);
    }

    public IntEllipse(IntPoint focal1, IntPoint focal2, int length) {
        if (IntPoint.distance(focal2, focal2) >= length) {
            return;
        }
        focalsOnX = (focal1.getY() == focal2.getY());
        longAxis = length / 2;
        if (focalsOnX) {
            centerX = (focal1.getX() + focal2.getX()) / 2;
            centerY = focal1.getY();
            focalDistance = Math.abs(focal1.getX() - focal2.getX()) / 2;
            shortAxis = (int) Math.sqrt(longAxis * longAxis - focalDistance * focalDistance);
            radiusX = longAxis;
            radiusY = shortAxis;
            if (focal1.getX() >= focal2.getX()) {
                focalSmall = new IntPoint(focal2.getX(), focal2.getY());
                focalBig = new IntPoint(focal1.getX(), focal1.getY());
            } else {
                focalSmall = new IntPoint(focal1.getX(), focal1.getY());
                focalBig = new IntPoint(focal2.getX(), focal2.getY());
            }
            rectangle = new IntRectangle(centerX - longAxis, centerY + shortAxis,
                    centerX + longAxis, centerY + shortAxis);
        } else {
            centerX = focal1.getX();
            centerY = (focal1.getY() + focal2.getY()) / 2;
            focalDistance = Math.abs(focal1.getY() - focal2.getY()) / 2;
            shortAxis = (int) Math.sqrt(longAxis * longAxis - focalDistance * focalDistance);
            radiusX = shortAxis;
            radiusY = longAxis;
            if (focal1.getY() >= focal2.getY()) {
                focalSmall = new IntPoint(focal2.getX(), focal2.getY());
                focalBig = new IntPoint(focal1.getX(), focal1.getY());
            } else {
                focalSmall = new IntPoint(focal1.getX(), focal1.getY());
                focalBig = new IntPoint(focal2.getX(), focal2.getY());
            }
            rectangle = new IntRectangle(centerX - shortAxis, centerY + longAxis,
                    centerX + shortAxis, centerY + longAxis);
        }

        sumLength = length;
    }

    private void makeEllipse(IntRectangle rect) {
        if (!rect.isValid()) {
            return;
        }
        rectangle = rect;
        focalsOnX = rect.getWidth() >= rect.getHeight();
        centerX = (rect.getSmallX() + rect.getBigX()) / 2;
        centerY = (rect.getSmallY() + rect.getBigY()) / 2;
        if (focalsOnX) {
            longAxis = rect.getWidth() / 2;
            shortAxis = rect.getHeight() / 2;
            radiusX = longAxis;
            radiusY = shortAxis;
            focalDistance = (int) Math.sqrt(longAxis * longAxis - shortAxis * shortAxis);
            focalSmall = new IntPoint(centerX - focalDistance, centerY);
            focalBig = new IntPoint(centerX + focalDistance, centerY);
            sumLength = rect.getWidth();
        } else {
            longAxis = rect.getHeight() / 2;
            shortAxis = rect.getWidth() / 2;
            radiusX = shortAxis;
            radiusY = longAxis;
            focalDistance = (int) Math.sqrt(longAxis * longAxis - shortAxis * shortAxis);
            focalSmall = new IntPoint(centerX, centerY - focalDistance);
            focalBig = new IntPoint(centerX, centerY + focalDistance);
            sumLength = rect.getHeight();
        }

    }

    public boolean isValid() {
        return longAxis > 0 && shortAxis > 0
                && focalBig != null && focalSmall != null
                && rectangle != null && rectangle.isValid();
    }

    public IntEllipse cloneValues() {
        return new IntEllipse(focalSmall, focalBig, longAxis);
    }

    public boolean include(int x, int y) {
        int distanceA = IntPoint.distance(focalBig.getX(), focalBig.getY(), x, y);
        int distanceB = IntPoint.distance(focalSmall.getX(), focalSmall.getY(), x, y);
        return distanceA + distanceB <= sumLength;
    }

    public boolean on(int x, int y) {
        int distanceA = IntPoint.distance(focalBig.getX(), focalBig.getY(), x, y);
        int distanceB = IntPoint.distance(focalSmall.getX(), focalSmall.getY(), x, y);
        return distanceA + distanceB == sumLength;
    }

    public boolean include(IntPoint p) {
        int distanceA = IntPoint.distance(focalBig, p);
        int distanceB = IntPoint.distance(focalSmall, p);
        return distanceA + distanceB <= sumLength;
    }

    public boolean on(IntPoint p) {
        int distanceA = IntPoint.distance(focalBig, p);
        int distanceB = IntPoint.distance(focalSmall, p);
        return distanceA + distanceB == sumLength;
    }

    public IntRectangle getRectangle() {
        return rectangle;
    }

    public void setRectangle(IntRectangle rectangle) {
        this.rectangle = rectangle;
    }

    public IntPoint getFocalSmall() {
        return focalSmall;
    }

    public void setFocalSmall(IntPoint focalSmall) {
        this.focalSmall = focalSmall;
    }

    public IntPoint getFocalBig() {
        return focalBig;
    }

    public void setFocalBig(IntPoint focalBig) {
        this.focalBig = focalBig;
    }

    public int getLongAxis() {
        return longAxis;
    }

    public void setLongAxis(int longAxis) {
        this.longAxis = longAxis;
    }

    public int getShortAxis() {
        return shortAxis;
    }

    public void setShortAxis(int shortAxis) {
        this.shortAxis = shortAxis;
    }

    public int getFocalDistance() {
        return focalDistance;
    }

    public void setFocalDistance(int focalDistance) {
        this.focalDistance = focalDistance;
    }

    public int getCenterX() {
        return centerX;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public void setCenterY(int centerY) {
        this.centerY = centerY;
    }

    public int getSumLength() {
        return sumLength;
    }

    public void setSumLength(int sumLength) {
        this.sumLength = sumLength;
    }

    public boolean isFocalsOnX() {
        return focalsOnX;
    }

    public void setFocalsOnX(boolean focalsOnX) {
        this.focalsOnX = focalsOnX;
    }

    public int getRadiusX() {
        return radiusX;
    }

    public void setRadiusX(int radiusX) {
        this.radiusX = radiusX;
    }

    public int getRadiusY() {
        return radiusY;
    }

    public void setRadiusY(int radiusY) {
        this.radiusY = radiusY;
    }

}
