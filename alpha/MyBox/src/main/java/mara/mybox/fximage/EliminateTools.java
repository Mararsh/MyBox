package mara.mybox.fximage;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageMosaic;
import mara.mybox.data.DoublePolylines;

/**
 * @Author Mara
 * @CreateDate 2018-11-13 12:38:14
 * @License Apache License Version 2.0
 */
public class EliminateTools {

    public static Image drawErase(Image image, DoublePolylines penData, int strokeWidth) {
        if (penData == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.EliminateTools.drawErase2(source, penData, strokeWidth);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawMosaic(Image image, DoublePolylines penData,
            ImageMosaic.MosaicType mosaicType, int strokeWidth, int intensity) {
        if (penData == null || mosaicType == null || strokeWidth < 1) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.EliminateTools.drawMosaic(
                source, penData, mosaicType, strokeWidth, intensity);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

}
