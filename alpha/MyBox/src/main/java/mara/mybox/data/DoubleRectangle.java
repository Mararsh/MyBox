package mara.mybox.data;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * @Author Mara
 * @CreateDate 2018-11-11 12:23:40
 * @License Apache License Version 2.0
 */
public class DoubleRectangle implements DoubleShape {

    protected double smallX, smallY, bigX, bigY, width, height;
    protected double round;

    public DoubleRectangle() {
    }

    public DoubleRectangle(Rectangle2D.Double rectangle) {
        smallX = rectangle.getX();
        smallY = rectangle.getY();
        width = rectangle.getWidth();
        height = rectangle.getHeight();
        bigX = rectangle.getX() + width - 1;
        bigY = rectangle.getY() + height - 1;
    }

    public DoubleRectangle(double x1, double y1, double x2, double y2) {
        smallX = x1;
        smallY = y1;
        bigX = x2;
        bigY = y2;
        width = getWidth();
        height = getHeight();
    }

    @Override
    public Shape getShape() {
        if (round > 0) {
            return new RoundRectangle2D.Double(smallX, smallY, width, height, round, round);
        } else {
            return new Rectangle2D.Double(smallX, smallY, width, height);
        }
    }

    @Override
    public DoubleRectangle cloneValues() {
        return new DoubleRectangle(smallX, smallY, bigX, bigY);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return !isValid() || width == 0 || height == 0;
    }

    @Override
    public DoubleRectangle translateRel(double offsetX, double offsetY) {
        DoubleRectangle nRectangle = new DoubleRectangle(
                smallX + offsetX, smallY + offsetY,
                bigX + offsetX, bigY + offsetY);
        return nRectangle;
    }

    @Override
    public DoubleRectangle translateAbs(double x, double y) {
        DoubleShape moved = DoubleShape.translateAbs(this, x, y);
        return moved != null ? (DoubleRectangle) moved : null;
    }

    public Rectangle rectangle() {
        return new Rectangle((int) smallX, (int) smallY, (int) getWidth(), (int) getHeight());
    }

    public boolean same(DoubleRectangle rect) {
        return rect != null
                && smallX == rect.getSmallX() && smallY == rect.getSmallY()
                && bigX == rect.getBigX() && bigY == rect.getBigY();
    }

    /*
        get
     */
    public final double getWidth() {
        width = Math.abs(bigX - smallX) + 1;
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public final double getHeight() {
        height = Math.abs(bigY - smallY) + 1;
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

    public double getRound() {
        return round;
    }

    public void setRound(double round) {
        this.round = round;
    }

}
