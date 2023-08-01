package mara.mybox.data;

import java.awt.geom.Line2D;

/**
 * @Author Mara
 * @CreateDate 2023-7-6
 * @License Apache License Version 2.0
 */
public class DoubleLine implements DoubleShape {

    private double startX, startY, endX, endY;

    public DoubleLine() {

    }

    public DoubleLine(double startX, double startY, double endX, double endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    @Override
    public Line2D.Double getShape() {
        return new Line2D.Double(startX, startY, endX, endY);
    }

    @Override
    public DoubleLine cloneValues() {
        return new DoubleLine(startX, startY, endX, endY);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public DoubleLine translateRel(double offsetX, double offsetY) {
        DoubleLine nline = new DoubleLine(
                startX + offsetX, startY + offsetY,
                endX + offsetX, endY + offsetY);
        return nline;
    }

    @Override
    public DoubleLine translateAbs(double x, double y) {
        DoubleShape moved = DoubleShape.translateAbs(this, x, y);
        return moved != null ? (DoubleLine) moved : null;
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
