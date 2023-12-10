package mara.mybox.fximage;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;

/**
 * @Author Mara
 * @CreateDate 2018-11-13 12:38:14
 * @License Apache License Version 2.0
 */
public class CropTools {

    public static Image cropInsideFx(FxTask task, Image image, DoubleRectangle rect, Color bgColor) {
        if (image == null || rect == null || rect.isEmpty() || bgColor == null) {
            return image;
        }
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.bufferedimage.CropTools.cropInside(task,
                source, rect, ColorConvertTools.converColor(bgColor));
        if (target == null || (task != null && !task.isWorking())) {
            return null;
        }
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image cropOutsideFx(FxTask task, Image image, DoubleRectangle rect, Color bgColor) {
        try {
            if (image == null || rect == null || rect.isEmpty() || bgColor == null) {
                return image;
            }
            BufferedImage source = SwingFXUtils.fromFXImage(image, null);
            if (source == null) {
                return image;
            }
            BufferedImage target = mara.mybox.bufferedimage.CropTools.cropOutside(task,
                    source, rect, ColorConvertTools.converColor(bgColor));
            if (target == null || (task != null && !task.isWorking())) {
                return null;
            }
            Image newImage = SwingFXUtils.toFXImage(target, null);
            return newImage;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return image;
        }
    }

    public static Image cropOutsideFx(FxTask task, Image image, DoubleRectangle rect) {
        return cropOutsideFx(task, image, rect, Color.TRANSPARENT);
    }

    public static Image cropOutsideFx(FxTask task, Image image, double x1, double y1, double x2, double y2) {
        return cropOutsideFx(task, image, DoubleRectangle.xy12(x1, y1, x2, y2), Color.TRANSPARENT);
    }

}
