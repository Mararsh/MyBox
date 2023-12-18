package mara.mybox.bufferedimage;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * @Author Mara
 * @CreateDate 2019-2-15
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageMosaic extends PixelsOperation {

    protected MosaicType type;
    protected int intensity;

    public enum MosaicType {
        Mosaic, FrostedGlass
    };

    public ImageMosaic() {
        this.operationType = OperationType.Mosaic;
        this.type = MosaicType.Mosaic;
        intensity = 20;
    }

    public static ImageMosaic create() {
        return new ImageMosaic();
    }

    @Override
    protected Color operatePixel(BufferedImage target, Color color, int x, int y) {
        int newColor = ShapeTools.mosaic(image, imageWidth, imageHeight, x, y, type, intensity);
        target.setRGB(x, y, newColor);
        return new Color(newColor, true);
    }

    /*
        set
     */
    public ImageMosaic setType(MosaicType type) {
        this.type = type;
        return this;
    }

    public ImageMosaic setIntensity(int intensity) {
        this.intensity = intensity;
        return this;
    }

}
