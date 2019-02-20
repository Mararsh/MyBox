package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2018-11-11 12:23:40
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class IntRectangle {

    private int leftX, leftY, rightX, rightY, width, height;
    private int maxX, maxY;

    public IntRectangle() {
        maxX = Integer.MAX_VALUE;
        maxY = Integer.MAX_VALUE;
    }

    public IntRectangle(int x1, int y1, int x2, int y2) {
        maxX = Integer.MAX_VALUE;
        maxY = Integer.MAX_VALUE;
        leftX = x1;
        leftY = y1;
        rightX = x2;
        rightY = y2;
    }

    public IntRectangle(int maxX, int maxY, int x1, int y1, int x2, int y2) {
        this.maxX = maxX;
        this.maxY = maxY;
        leftX = x1;
        leftY = y1;
        rightX = x2;
        rightY = y2;
    }

    public IntRectangle cloneValues() {
        return new IntRectangle(maxX, maxY, leftX, leftY, rightX, rightY);
    }

    public boolean isValid() {
        return leftX >= 0 && leftY >= 0 && rightX > leftX && rightY > leftY
                && rightX < maxX && rightY < maxY;
    }

    public boolean isValid(int maxX, int maxY) {
        return leftX >= 0 && leftY >= 0 && rightX > leftX && rightY > leftY
                && rightX < maxX && rightY < maxY;
    }

    public boolean include(int x, int y) {
        return x >= leftX && y >= leftY && x <= rightX && y <= rightY;
    }

    public int getWidth() {
        width = rightX - leftX + 1;
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        height = rightY - leftY + 1;
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getLeftX() {
        return leftX;
    }

    public void setLeftX(int leftX) {
        this.leftX = leftX;
    }

    public int getLeftY() {
        return leftY;
    }

    public void setLeftY(int leftY) {
        this.leftY = leftY;
    }

    public int getRightX() {
        return rightX;
    }

    public void setRightX(int rightX) {
        this.rightX = rightX;
    }

    public int getRightY() {
        return rightY;
    }

    public void setRightY(int rightY) {
        this.rightY = rightY;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

}
