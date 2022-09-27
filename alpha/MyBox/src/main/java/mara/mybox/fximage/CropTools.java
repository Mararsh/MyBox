package mara.mybox.fximage;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2018-11-13 12:38:14
 * @License Apache License Version 2.0
 */
public class CropTools {

    public static Image cropInsideFx(Image image, DoubleShape shape, Color bgColor) {
        if (image == null || shape == null || !shape.isValid() || bgColor == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.CropTools.cropInside(source, shape, ColorConvertTools.converColor(bgColor));
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image cropOutsideFx(Image image, DoubleShape shape, Color bgColor) {
        try {
            if (image == null || shape == null || !shape.isValid() || bgColor == null) {
                return image;
            }
            BufferedImage source = SwingFXUtils.fromFXImage(image, null);
            BufferedImage target = mara.mybox.bufferedimage.CropTools.cropOutside(source, shape, ColorConvertTools.converColor(bgColor));
            Image newImage = SwingFXUtils.toFXImage(target, null);
            return newImage;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return image;
        }
    }

    public static Image cropOutsideFx(Image image, DoubleRectangle rect) {
        return cropOutsideFx(image, rect, Color.TRANSPARENT);
    }

    public static Image cropOutsideFx(Image image, double x1, double y1, double x2, double y2) {
        return cropOutsideFx(image, new DoubleRectangle(x1, y1, x2, y2), Color.TRANSPARENT);
    }

}
