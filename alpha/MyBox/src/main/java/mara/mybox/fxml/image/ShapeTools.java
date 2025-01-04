package mara.mybox.fxml.image;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.image.data.ImageMosaic;
import mara.mybox.image.data.PixelsBlend;
import mara.mybox.data.DoublePolylines;
import mara.mybox.data.DoubleShape;
import mara.mybox.data.ShapeStyle;
import mara.mybox.fxml.FxTask;

/**
 * @Author Mara
 * @CreateDate 2018-11-13 12:38:14
 * @License Apache License Version 2.0
 */
public class ShapeTools {

    public static Image drawShape(FxTask task, Image image, DoubleShape shape, ShapeStyle style, PixelsBlend blender) {
        if (shape == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.image.tools.ShapeTools.drawShape(task, source, shape, style, blender);
        if (target == null || (task != null && !task.isWorking())) {
            return null;
        }
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawErase(FxTask task, Image image, DoublePolylines penData, ShapeStyle style) {
        if (penData == null || style == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.image.tools.ShapeTools.drawErase(task, source, penData, style);
        if (target == null || (task != null && !task.isWorking())) {
            return null;
        }
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image drawMosaic(FxTask task, Image image, DoublePolylines penData,
            ImageMosaic.MosaicType mosaicType, int strokeWidth, int intensity) {
        if (penData == null || mosaicType == null || strokeWidth < 1) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.image.tools.ShapeTools.drawMosaic(task, source, penData, mosaicType, strokeWidth, intensity);
        if (target == null || (task != null && !task.isWorking())) {
            return null;
        }
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

}
