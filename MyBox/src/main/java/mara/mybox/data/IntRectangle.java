package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2018-11-11 12:23:40
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class IntRectangle {

    private int smallX, smallY, bigX, bigY, width, height;
    private int maxX, maxY;

    public IntRectangle() {
        maxX = Integer.MAX_VALUE;
        maxY = Integer.MAX_VALUE;
    }

    public IntRectangle(int x1, int y1, int x2, int y2) {
        maxX = Integer.MAX_VALUE;
        maxY = Integer.MAX_VALUE;
        smallX = x1;
        smallY = y1;
        bigX = x2;
        bigY = y2;
    }

    public IntRectangle(int maxX, int maxY, int x1, int y1, int x2, int y2) {
        this.maxX = maxX;
        this.maxY = maxY;
        smallX = x1;
        smallY = y1;
        bigX = x2;
        bigY = y2;
    }

    public IntRectangle cloneValues() {
        return new IntRectangle(maxX, maxY, smallX, smallY, bigX, bigY);
    }

    public boolean isValid() {
        return bigX > smallX && bigY > smallY
                && bigX < maxX && bigY < maxY;
    }

    public boolean isValid(int maxX, int maxY) {
        return bigX > smallX && bigY > smallY
                && bigX < maxX && bigY < maxY;
    }

    public boolean include(int x, int y) {
        return x >= smallX && y >= smallY && x <= bigX && y <= bigY;
    }

    public int getWidth() {
        width = Math.abs(bigX - smallX + 1);
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        height = Math.abs(bigY - smallY + 1);
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getSmallX() {
        return smallX;
    }

    public void setSmallX(int smallX) {
        this.smallX = smallX;
    }

    public int getSmallY() {
        return smallY;
    }

    public void setSmallY(int smallY) {
        this.smallY = smallY;
    }

    public int getBigX() {
        return bigX;
    }

    public void setBigX(int bigX) {
        this.bigX = bigX;
    }

    public int getBigY() {
        return bigY;
    }

    public void setBigY(int bigY) {
        this.bigY = bigY;
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
