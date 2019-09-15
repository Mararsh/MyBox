package mara.mybox.data;

import java.awt.image.BufferedImage;
import mara.mybox.image.ImageScope;

/**
 * @Author Mara
 * @CreateDate 2018-11-11 12:29:29
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class DoubleOutline extends DoubleRectangle {

    private BufferedImage image;
    private DoubleRectangle rectangle;
    private int insideColor;
    private double offsetX, offsetY;

    public DoubleOutline() {
    }

    public DoubleOutline(BufferedImage image, int color) {
        this.image = image;
        rectangle = new DoubleRectangle(0, 0, image.getWidth(), image.getHeight());
        this.insideColor = color;
        offsetX = rectangle.getSmallX() >= 0 ? 0 : rectangle.getSmallX();
        offsetY = rectangle.getSmallY() >= 0 ? 0 : rectangle.getSmallY();
    }

    public DoubleOutline(BufferedImage image, DoubleRectangle rectangle, int color) {
        this.image = image;
        this.rectangle = rectangle;
        this.insideColor = color;
        offsetX = rectangle.getSmallX() >= 0 ? 0 : rectangle.getSmallX();
        offsetY = rectangle.getSmallY() >= 0 ? 0 : rectangle.getSmallY();
    }

    public DoubleOutline(ImageScope scope) {
        image = scope.getOutline();
        rectangle = scope.getRectangle();
        insideColor = scope.isAreaExcluded() ? -1 : 0;
        offsetX = rectangle.getSmallX() >= 0 ? 0 : rectangle.getSmallX();
        offsetY = rectangle.getSmallY() >= 0 ? 0 : rectangle.getSmallY();
    }

    @Override
    public boolean isValid() {
        return image != null && rectangle != null;
    }

    @Override
    public DoubleOutline cloneValues() {
        return new DoubleOutline(image, rectangle, insideColor);
    }

    @Override
    public boolean include(double x, double y) {
        return isValid() && rectangle.include(x, y)
                && (image.getRGB((int) (x - offsetX), (int) (y - offsetY)) == insideColor);
    }

    @Override
    public DoubleRectangle getBound() {
        return rectangle;
    }

    @Override
    public DoubleOutline move(double offset) {
        DoubleRectangle nRectangle = new DoubleRectangle(
                smallX + offset, smallY + offset,
                bigX + offset, bigY + offset);
        return new DoubleOutline(image, nRectangle, insideColor);
    }

    @Override
    public DoubleOutline move(double offsetX, double offsetY) {
        DoubleRectangle nRectangle = new DoubleRectangle(
                smallX + offsetX, smallY + offsetY,
                bigX + offsetX, bigY + offsetY);
        return new DoubleOutline(image, nRectangle, insideColor);
    }

}
