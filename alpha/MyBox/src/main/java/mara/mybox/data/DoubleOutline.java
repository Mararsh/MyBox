package mara.mybox.data;

import java.awt.image.BufferedImage;
import mara.mybox.bufferedimage.ImageScope;

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
        rectangle = new DoubleRectangle(0, 0, image.getWidth() - 1, image.getHeight() - 1);
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

    public boolean contains(double x, double y) {
        try {
            return isValid() && DoubleShape.contains(rectangle, x, y)
                    && (image.getRGB((int) (x - offsetX), (int) (y - offsetY)) == insideColor);
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return false;
        }
    }

    public DoubleRectangle getBound() {
        return rectangle;
    }

    @Override
    public DoubleOutline translateRel(double offsetX, double offsetY) {
        DoubleRectangle nRectangle = new DoubleRectangle(
                smallX + offsetX, smallY + offsetY,
                bigX + offsetX, bigY + offsetY);
        return new DoubleOutline(image, nRectangle, insideColor);
    }

}
