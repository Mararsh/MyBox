package mara.mybox.data;

import java.awt.geom.Arc2D;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-7-31
 * @License Apache License Version 2.0
 */
public class DoubleArc implements DoubleShape {

    private double centerX, centerY, radiusX, radiusY, startAngle, extentAngle;
    private int type;

    public DoubleArc() {
    }

    public static DoubleArc rect(double x1, double y1, double x2, double y2,
            double startAngle, double extentAngle, int type) {
        DoubleArc a = new DoubleArc();
        a.setCenterX((x1 + x2) / 2);
        a.setCenterY((y1 + y2) / 2);
        a.setRadiusX(Math.abs(x2 - x1) / 2);
        a.setRadiusY(Math.abs(y2 - y1) / 2);
        a.setStartAngle(startAngle);
        a.setExtentAngle(extentAngle);
        a.setType(type);
        return a;
    }

    public static DoubleArc arc(double centerX, double centerY, double radiusX, double radiusY,
            double startAngle, double extentAngle, int type) {
        DoubleArc a = new DoubleArc();
        a.setCenterX(centerX);
        a.setCenterY(centerY);
        a.setRadiusX(radiusX);
        a.setRadiusY(radiusY);
        a.setStartAngle(startAngle);
        a.setExtentAngle(extentAngle);
        a.setType(type);
        return a;
    }

    @Override
    public String name() {
        return message("ArcCurve");
    }

    public double getX1() {
        return centerX - radiusX;
    }

    public double getY1() {
        return centerY - radiusY;
    }

    public double getX2() {
        return centerX + radiusX;
    }

    public double getY2() {
        return centerY + radiusY;
    }

    @Override
    public Arc2D.Double getShape() {
        return new Arc2D.Double(getX1(), getY1(), radiusX * 2, radiusY * 2, startAngle, extentAngle, type);
    }

    @Override
    public DoubleArc cloneValues() {
        return DoubleArc.arc(centerX, centerY, radiusX, radiusY, startAngle, extentAngle, type);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return !isValid();
    }

    @Override
    public boolean translateRel(double offsetX, double offsetY) {
        centerX += offsetX;
        centerY += offsetY;
        return true;
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

    public double getRadiusX() {
        return radiusX;
    }

    public double getRadiusY() {
        return radiusY;
    }

    public double getStartAngle() {
        return startAngle;
    }

    public double getExtentAngle() {
        return extentAngle;
    }

    public int getType() {
        return type;
    }

    /*
        set
     */
    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }

    public void setRadiusX(double radiusX) {
        this.radiusX = radiusX;
    }

    public void setRadiusY(double radiusY) {
        this.radiusY = radiusY;
    }

    public void setStartAngle(double startAngle) {
        this.startAngle = startAngle;
    }

    public void setExtentAngle(double extentAngle) {
        this.extentAngle = extentAngle;
    }

    public void setType(int type) {
        this.type = type;
    }

}
