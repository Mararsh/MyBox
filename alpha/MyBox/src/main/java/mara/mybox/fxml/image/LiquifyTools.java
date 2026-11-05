package mara.mybox.fxml.image;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.data.IntPoint;
import mara.mybox.fxml.FxTask;
import mara.mybox.image.data.PixelSampler;

/**
 * @Author Mara
 * @CreateDate 2026-11-1
 * @License Apache License Version 2.0
 */
public class LiquifyTools {

    public static Image forwardWarp(FxTask task, Image image,
            IntPoint startPoint, IntPoint endPoint, PixelSampler.SampleMode mode,
            int radius, int step, float strengthMax, float strengthThreshold, boolean isGauss) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.image.tools.LiquifyTools.forwardWarp(task, source,
                startPoint, endPoint, mode, radius, step, strengthMax, strengthThreshold, isGauss);
        if (target == null || (task != null && !task.isWorking())) {
            return null;
        }
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

}
