package mara.mybox.bufferedimage;

import java.awt.image.BufferedImage;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.bufferedimage.PixelsBlend.ImagesBlendMode;

/**
 * @Author Mara
 * @CreateDate 2018-10-31 20:03:32
 * @License Apache License Version 2.0
 */
public class ImageBlend {

    protected ImagesBlendMode blendMode;
    protected float opacity;
    protected boolean intersectOnly, onTop;
    protected int x, y;
    protected BufferedImage foreImage, backImage;

    public ImageBlend() {

    }

    public ImageBlend(BufferedImage foreImage, BufferedImage backImage,
            boolean onTop, ImagesBlendMode blendMode, float opacity, boolean intersectOnly, int x, int y) {
        this.foreImage = foreImage;
        this.backImage = backImage;
        this.onTop = onTop;
        this.blendMode = blendMode;
        this.opacity = opacity;
        this.intersectOnly = intersectOnly;
        this.x = x;
        this.y = y;
    }

    public static BufferedImage blend(BufferedImage foreImage, BufferedImage backImage,
            int x, int y, ImagesBlendMode blendMode, float opacity, boolean orderReversed, boolean ignoreTransparent) {
        try {
            if (foreImage == null || backImage == null || blendMode == null) {
                return null;
            }
            int imageType = BufferedImage.TYPE_INT_ARGB;
            DoubleRectangle rect = new DoubleRectangle(x, y,
                    x + foreImage.getWidth() - 1, y + foreImage.getHeight() - 1);
            BufferedImage target = new BufferedImage(backImage.getWidth(), backImage.getHeight(), imageType);
            PixelsBlend colorBlend = PixelsBlendFactory.create(blendMode)
                    .setBlendMode(blendMode).setOpacity(opacity)
                    .setOrderReversed(orderReversed).setIgnoreTransparency(ignoreTransparent);
            for (int j = 0; j < backImage.getHeight(); ++j) {
                for (int i = 0; i < backImage.getWidth(); ++i) {
                    int backPixel = backImage.getRGB(i, j);
                    if (rect.contains(i, j)) {
                        int forePixel = foreImage.getRGB(i - x, j - y);
                        target.setRGB(i, j, colorBlend.blend(forePixel, backPixel));
                    } else {
                        target.setRGB(i, j, backPixel);
                    }
                }
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return foreImage;
        }
    }

    /*
        get/set
     */
    public ImagesBlendMode getBlendMode() {
        return blendMode;
    }

    public void setBlendMode(ImagesBlendMode blendMode) {
        this.blendMode = blendMode;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public boolean isIntersectOnly() {
        return intersectOnly;
    }

    public void setIntersectOnly(boolean intersectOnly) {
        this.intersectOnly = intersectOnly;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public BufferedImage getForeImage() {
        return foreImage;
    }

    public void setForeImage(BufferedImage foreImage) {
        this.foreImage = foreImage;
    }

    public BufferedImage getBackImage() {
        return backImage;
    }

    public void setBackImage(BufferedImage backImage) {
        this.backImage = backImage;
    }

}
