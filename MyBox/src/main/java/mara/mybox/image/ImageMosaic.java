package mara.mybox.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

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
    }

    public static ImageMosaic create() {
        return new ImageMosaic();
    }

    public static ImageMosaic create(BufferedImage image, ImageScope scope, MosaicType type, int intensity) {
        return ImageMosaic.create().image(image)
                .scope(scope).type(type).intensity(intensity).init();
    }

    public static ImageMosaic create(Image image, ImageScope scope, MosaicType type, int intensity) {
        return ImageMosaic.create().image(SwingFXUtils.fromFXImage(image, null))
                .scope(scope).type(type).intensity(intensity).init();
    }

    public ImageMosaic type(MosaicType type) {
        this.type = type;
        return this;
    }

    public ImageMosaic intensity(int intensity) {
        this.intensity = intensity;
        return this;
    }

    public ImageMosaic image(BufferedImage image) {
        this.image = image;
        return this;
    }

    public ImageMosaic scope(ImageScope scope) {
        this.scope = scope;
        return this;
    }

    public ImageMosaic init() {
        if (type == null) {
            type = MosaicType.Mosaic;
        }
        imageWidth = image.getWidth();
        imageHeight = image.getHeight();
        return this;
    }

    @Override
    protected Color operatePixel(BufferedImage target, Color color, int x, int y) {
        int newColor = ImageManufacture.mosaic(image, imageWidth, imageHeight, x, y, type, intensity);
        target.setRGB(x, y, newColor);
        return new Color(newColor, true);
    }

}
