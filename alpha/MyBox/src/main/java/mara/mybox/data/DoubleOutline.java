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
    private int insideColor;
    private double offsetX, offsetY;

    public DoubleOutline() {
    }

    public DoubleOutline(BufferedImage image, int color) {
        this.image = image;
        x = 0;
        y = 0;
        width = image.getWidth();
        height = image.getHeight();
        this.insideColor = color;
    }

    public DoubleOutline(BufferedImage image, DoubleRectangle rect, int color) {
        this.image = image;
        this.insideColor = color;
        x = rect.getX();
        y = rect.getY();
        width = rect.getWidth();
        height = rect.getHeight();
        offsetX = x >= 0 ? 0 : x;
        offsetY = y >= 0 ? 0 : y;
    }

    public DoubleOutline(ImageScope scope) {
        image = scope.getOutline();
        insideColor = scope.isAreaExcluded() ? -1 : 0;
        DoubleRectangle rect = scope.getRectangle();
        x = rect.getX();
        y = rect.getY();
        width = rect.getWidth();
        height = rect.getHeight();
    }

    @Override
    public boolean isValid() {
        return image != null && super.isValid();
    }

    @Override
    public DoubleOutline cloneValues() {
        return new DoubleOutline(image, this, insideColor);
    }

    @Override
    public DoubleOutline translateRel(double offsetX, double offsetY) {
        DoubleRectangle nRectangle = DoubleRectangle.xywh(
                x + offsetX, y + offsetY, width, height);
        return new DoubleOutline(image, nRectangle, insideColor);
    }

}
