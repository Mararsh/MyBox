package mara.mybox.data;

import java.awt.geom.Ellipse2D;
import static mara.mybox.tools.DoubleTools.imageScale;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-11-11 12:29:29
 * @License Apache License Version 2.0
 */
public class DoubleCircle implements DoubleShape {

    private double centerX, centerY, radius;

    public DoubleCircle() {

    }

    public DoubleCircle(double x, double y, double r) {
        centerX = x;
        centerY = y;
        radius = r;
    }

    @Override
    public String name() {
        return message("Circle");
    }

    @Override
    public Ellipse2D.Double getShape() {
        return new Ellipse2D.Double(centerX - radius, centerY - radius, 2 * radius, 2 * radius);
    }

    @Override
    public DoubleCircle copy() {
        return new DoubleCircle(centerX, centerY, radius);
    }

    @Override
    public boolean isValid() {
        return radius > 0;
    }

    @Override
    public boolean isEmpty() {
        return !isValid();
    }

    public boolean same(DoubleCircle circle) {
        return centerX == circle.getCenterX() && centerY == circle.getCenterY()
                && radius == circle.getRadius();
    }

    @Override
    public boolean translateRel(double offsetX, double offsetY) {
        centerX += offsetX;
        centerY += offsetY;
        return true;
    }

    @Override
    public boolean scale(double scaleX, double scaleY) {
        radius *= Math.min(scaleX, scaleY);
        return true;
    }

    @Override
    public String svgAbs() {
        double cx = imageScale(centerX);
        double cy = imageScale(centerY);
        double r = imageScale(radius);
        return "M " + (cx - r) + "," + cy + " \n"
                + "A " + r + "," + r + " 0,0,1 " + (cx + r) + "," + cy + " \n"
                + "A " + r + "," + r + " 0,0,1 " + (cx - r) + "," + cy + " \n"
                + "Z";
    }

    @Override
    public String svgRel() {
        double cx = imageScale(centerX);
        double cy = imageScale(centerY);
        double r = imageScale(radius);
        double r2 = imageScale(2 * radius);
        return "m " + (cx - r) + "," + cy + " \n"
                + "a " + r + "," + r + " 0,0,1 " + r2 + "," + 0 + " \n"
                + "a " + r + "," + r + " 0,0,1 " + (-r2) + "," + 0 + " \n"
                + "z";
    }


    /*
        set
     */
    public void setRadius(double radius) {
        this.radius = radius;
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

    public double getRadius() {
        return radius;
    }

}
