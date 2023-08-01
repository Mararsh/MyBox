package mara.mybox.data;

import java.awt.geom.QuadCurve2D;

/**
 * @Author Mara
 * @CreateDate 2023-7-31
 * @License Apache License Version 2.0
 */
public class DoubleArc implements DoubleShape {

    private double startX, startY, controlX, controlY, endX, endY;

    public DoubleArc() {

    }

    public DoubleArc(double startX, double startY,
            double controlX, double controlY, double endX, double endY) {
        this.startX = startX;
        this.startY = startY;
        this.controlX = controlX;
        this.controlY = controlY;
        this.endX = endX;
        this.endY = endY;
    }

    @Override
    public QuadCurve2D.Double getShape() {
        return new QuadCurve2D.Double(startX, startY, controlX, controlY, endX, endY);
    }

    @Override
    public DoubleArc cloneValues() {
        return new DoubleArc(startX, startY, controlX, controlY, endX, endY);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public DoubleArc translateRel(double offsetX, double offsetY) {
        return new DoubleArc(startX + offsetX, startY + offsetY,
                controlX + offsetX, controlY + offsetY,
                endX + offsetX, endY + offsetY);
    }

    @Override
    public DoubleArc translateAbs(double x, double y) {
        DoubleShape moved = DoubleShape.translateAbs(this, x, y);
        return moved != null ? (DoubleArc) moved : null;
    }

    public double getStartX() {
        return startX;
    }

    public void setStartX(double startX) {
        this.startX = startX;
    }

    public double getStartY() {
        return startY;
    }

    public void setStartY(double startY) {
        this.startY = startY;
    }

    public double getEndX() {
        return endX;
    }

    public void setEndX(double endX) {
        this.endX = endX;
    }

    public double getEndY() {
        return endY;
    }

    public void setEndY(double endY) {
        this.endY = endY;
    }

}
