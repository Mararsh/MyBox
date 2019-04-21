package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2018-11-11 12:23:40
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class DoubleRectangle implements DoubleShape {

    private double smallX, smallY, bigX, bigY, width, height;
    private double maxX, maxY;

    public DoubleRectangle() {
        maxX = Integer.MAX_VALUE;
        maxY = Integer.MAX_VALUE;
    }

    public DoubleRectangle(double x1, double y1, double x2, double y2) {
        maxX = Integer.MAX_VALUE;
        maxY = Integer.MAX_VALUE;
        smallX = x1;
        smallY = y1;
        bigX = x2;
        bigY = y2;
    }

    public DoubleRectangle(double maxX, double maxY, double x1, double y1, double x2, double y2) {
        this.maxX = maxX;
        this.maxY = maxY;
        smallX = x1;
        smallY = y1;
        bigX = x2;
        bigY = y2;
    }

    @Override
    public DoubleRectangle cloneValues() {
        return new DoubleRectangle(maxX, maxY, smallX, smallY, bigX, bigY);
    }

    @Override
    public boolean isValid() {
        return bigX > smallX && bigY > smallY
                && bigX < maxX && bigY < maxY;
    }

    public boolean isValid(double maxX, double maxY) {
        return bigX > smallX && bigY > smallY
                && bigX < maxX && bigY < maxY;
    }

    @Override
    public boolean include(double x, double y) {
        return x >= smallX && y >= smallY && x <= bigX && y <= bigY;
    }

    @Override
    public DoubleRectangle move(double offset) {
        DoubleRectangle nRectangle = new DoubleRectangle(
                smallX + offset, smallY + offset,
                bigX + offset, bigY + offset);
        return nRectangle;
    }

    @Override
    public DoubleRectangle move(double offsetX, double offsetY) {
        DoubleRectangle nRectangle = new DoubleRectangle(
                smallX + offsetX, smallY + offsetY,
                bigX + offsetX, bigY + offsetY);
        return nRectangle;
    }

    @Override
    public DoubleRectangle getBound() {
        return this;
    }

    public double getWidth() {
        width = Math.abs(bigX - smallX + 1);
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        height = Math.abs(bigY - smallY + 1);
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getSmallX() {
        return smallX;
    }

    public void setSmallX(double smallX) {
        this.smallX = smallX;
    }

    public double getSmallY() {
        return smallY;
    }

    public void setSmallY(double smallY) {
        this.smallY = smallY;
    }

    public double getBigX() {
        return bigX;
    }

    public void setBigX(double bigX) {
        this.bigX = bigX;
    }

    public double getBigY() {
        return bigY;
    }

    public void setBigY(double bigY) {
        this.bigY = bigY;
    }

    public double getMaxX() {
        return maxX;
    }

    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }

}
