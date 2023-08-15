package mara.mybox.data;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javafx.scene.image.Image;
import static mara.mybox.tools.DoubleTools.imageScale;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-11-11 12:23:40
 * @License Apache License Version 2.0
 */
public class DoubleRectangle implements DoubleShape {

    protected double x, y, width, height, round;

    public DoubleRectangle() {
    }

    public static DoubleRectangle xywh(double x, double y, double width, double height) {
        DoubleRectangle rect = new DoubleRectangle();
        rect.setX(x);
        rect.setY(y);
        rect.setWidth(width);
        rect.setHeight(height);
        return rect;
    }

    public static DoubleRectangle xy12(double x1, double y1, double x2, double y2) {
        DoubleRectangle rect = new DoubleRectangle();
        rect.setX(Math.min(x1, x2));
        rect.setY(Math.min(y1, y2));
        rect.setWidth(Math.abs(x2 - x1));
        rect.setHeight(Math.abs(y2 - y1));
        return rect;
    }

    public static DoubleRectangle rect(Rectangle2D.Double rect2D) {
        DoubleRectangle rect = new DoubleRectangle();
        rect.setX(rect2D.getX());
        rect.setY(rect2D.getY());
        rect.setWidth(rect2D.getWidth());
        rect.setHeight(rect2D.getHeight());
        return rect;
    }

    public static DoubleRectangle image(Image image) {
        DoubleRectangle rect = new DoubleRectangle();
        rect.setX(0);
        rect.setY(0);
        rect.setWidth(image.getWidth());
        rect.setHeight(image.getHeight());
        return rect;
    }

    public static DoubleRectangle image(BufferedImage image) {
        DoubleRectangle rect = new DoubleRectangle();
        rect.setX(0);
        rect.setY(0);
        rect.setWidth(image.getWidth());
        rect.setHeight(image.getHeight());
        return rect;
    }

    @Override
    public String name() {
        return message("Rectangle");
    }

    @Override
    public Shape getShape() {
        if (round > 0) {
            return new RoundRectangle2D.Double(x, y, width, height, round, round);
        } else {
            return new Rectangle2D.Double(x, y, width, height);
        }
    }

    @Override
    public DoubleRectangle copy() {
        return DoubleRectangle.xywh(x, y, width, height);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return !isValid() || width <= 0 || height <= 0;
    }

    public boolean contains(double px, double py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }

    @Override
    public boolean translateRel(double offsetX, double offsetY) {
        x += offsetX;
        y += offsetY;
        return true;
    }

    @Override
    public boolean scale(double scaleX, double scaleY) {
        width *= scaleX;
        height *= scaleY;
        return true;
    }

    @Override
    public String svgAbs() {
        double sx = imageScale(x);
        double sy = imageScale(y);
        double sw = imageScale(x + width);
        double sh = imageScale(y + height);
        return "M " + sx + "," + sy + " \n"
                + "L " + sw + "," + sy + " \n"
                + "L " + sw + "," + sh + " \n"
                + "L " + sx + "," + sh + " \n"
                + "L " + sx + "," + sy;
    }

    @Override
    public String svgRel() {
        double sx = imageScale(x);
        double sy = imageScale(y);
        double sw = imageScale(width);
        double sh = imageScale(height);
        return "m " + sx + "," + sy + " \n"
                + "l " + sw + "," + 0 + " \n"
                + "l " + 0 + "," + sh + " \n"
                + "l " + (-sw) + "," + 0 + " \n"
                + "l " + 0 + "," + (-sh);
    }

    public boolean same(DoubleRectangle rect) {
        return rect != null
                && x == rect.getX() && y == rect.getY()
                && width == rect.getWidth() && height == rect.getHeight();
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

    public String values() {
        return "[x=" + x + ",y=" + y
                + ",width=" + width + ",height=" + height
                + ",maxX=" + getMaxX() + ",maxY=" + getMaxY() + "]";
    }


    /*
        get
     */
    public final double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public final double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getRound() {
        return round;
    }

    public void setRound(double round) {
        this.round = round;
    }

}
