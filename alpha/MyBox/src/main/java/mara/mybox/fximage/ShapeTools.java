package mara.mybox.fximage;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.PixelsBlend;
import mara.mybox.data.DoublePolylines;
import mara.mybox.data.DoubleShape;
import mara.mybox.data.ShapeStyle;

/**
 * @Author Mara
 * @CreateDate 2018-11-13 12:38:14
 * @License Apache License Version 2.0
 */
public class ShapeTools {

    public static Image drawShape(Image image, DoubleShape shape, ShapeStyle style, PixelsBlend blender) {
        if (shape == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.ShapeTools.drawShape(source, shape, style, blender);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawErase(Image image, DoublePolylines penData, ShapeStyle style) {
        if (penData == null || style == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.ShapeTools.drawErase(source, penData, style);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

}
