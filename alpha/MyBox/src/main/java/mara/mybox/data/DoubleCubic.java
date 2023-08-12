package mara.mybox.data;

import java.awt.geom.CubicCurve2D;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-7-31
 * @License Apache License Version 2.0
 */
public class DoubleCubic implements DoubleShape {

    private double startX, startY, controlX1, controlY1, controlX2, controlY2, endX, endY;

    public DoubleCubic() {

    }

    public DoubleCubic(double startX, double startY,
            double control1X, double control1Y,
            double control2X, double control2Y,
            double endX, double endY) {
        this.startX = startX;
        this.startY = startY;
        this.controlX1 = control1X;
        this.controlY1 = control1Y;
        this.controlX2 = control2X;
        this.controlY2 = control2Y;
        this.endX = endX;
        this.endY = endY;
    }

    @Override
    public String name() {
        return message("CubicCurve");
    }

    @Override
    public CubicCurve2D.Double getShape() {
        return new CubicCurve2D.Double(startX, startY, controlX1, controlY1, controlX2, controlY2, endX, endY);
    }

    @Override
    public DoubleCubic cloneValues() {
        return new DoubleCubic(startX, startY, controlX1, controlY1, controlX2, controlY2, endX, endY);
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
        startX += offsetX;
        startY += offsetY;
        controlX1 += offsetX;
        controlY1 += offsetY;
        controlX2 += offsetX;
        controlY2 += offsetY;
        endX += offsetX;
        endY += offsetY;
        return true;
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

    public double getControlX1() {
        return controlX1;
    }

    public void setControlX1(double controlX1) {
        this.controlX1 = controlX1;
    }

    public double getControlY1() {
        return controlY1;
    }

    public void setControlY1(double controlY1) {
        this.controlY1 = controlY1;
    }

    public double getControlX2() {
        return controlX2;
    }

    public void setControlX2(double controlX2) {
        this.controlX2 = controlX2;
    }

    public double getControlY2() {
        return controlY2;
    }

    public void setControlY2(double controlY2) {
        this.controlY2 = controlY2;
    }

}
