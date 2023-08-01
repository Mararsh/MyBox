package mara.mybox.data;

import java.awt.geom.Line2D;

/**
 * @Author Mara
 * @CreateDate 2023-7-31
 * @License Apache License Version 2.0
 */
public class DoubleQuadratic implements DoubleShape {

    private double startX, startY, endX, endY;

    public DoubleQuadratic() {

    }

    public DoubleQuadratic(double startX, double startY, double endX, double endY) {
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
    public DoubleQuadratic cloneValues() {
        return new DoubleQuadratic(startX, startY, endX, endY);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public DoubleQuadratic translateRel(double offsetX, double offsetY) {
        DoubleQuadratic nline = new DoubleQuadratic(
                startX + offsetX, startY + offsetY,
                endX + offsetX, endY + offsetY);
        return nline;
    }

    @Override
    public DoubleQuadratic translateAbs(double x, double y) {
        DoubleShape moved = DoubleShape.translateAbs(this, x, y);
        return moved != null ? (DoubleQuadratic) moved : null;
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
