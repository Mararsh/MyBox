package mara.mybox.fxml.image;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.data.IntPoint;
import mara.mybox.fxml.FxTask;

/**
 * @Author Mara
 * @CreateDate 2026-11-1
 * @License Apache License Version 2.0
 */
public class LiquifyTools {

    public static Image forwardWarp(FxTask task, Image image,
            IntPoint startPoint, IntPoint endPoint,
            int radius, int step, float strength, boolean isGauss) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.image.tools.LiquifyTools.forwardWarp(task, source,
                startPoint, endPoint, radius, step, strength, isGauss);
        if (target == null || (task != null && !task.isWorking())) {
            return null;
        }
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

}
