package mara.mybox.data;

import java.awt.geom.Line2D;
import static mara.mybox.tools.DoubleTools.imageScale;
import static mara.mybox.value.Languages.message;

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
    public String name() {
        return message("StraightLine");
    }

    @Override
    public Line2D.Double getShape() {
        return new Line2D.Double(startX, startY, endX, endY);
    }

    @Override
    public DoubleLine copy() {
        return new DoubleLine(startX, startY, endX, endY);
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
        endX += offsetX;
        endY += offsetY;
        return true;
    }

    @Override
    public boolean scale(double scaleX, double scaleY) {
        endX *= scaleX;
        endY *= scaleY;
        return true;
    }

    @Override
    public String pathAbs() {
        return "M " + imageScale(startX) + "," + imageScale(startY) + " \n"
                + "L " + imageScale(endX) + "," + imageScale(endY);
    }

    @Override
    public String pathRel() {
        return "M " + imageScale(startX) + "," + imageScale(startY) + " \n"
                + "l " + imageScale(endX - startX) + "," + imageScale(endY - startY);
    }

    @Override
    public String elementAbs() {
        return "<line x1=\"" + imageScale(startX) + "\""
                + " y1=\"" + imageScale(startY) + "\""
                + " x2=\"" + imageScale(endX) + "\""
                + " y2=\"" + imageScale(endY) + "\"> ";
    }

    @Override
    public String elementRel() {
        return elementAbs();
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
