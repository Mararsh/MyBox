package mara.mybox.fxml.image;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.image.data.LiquifyConfig;

/**
 * @Author Mara
 * @CreateDate 2026-11-1
 * @License Apache License Version 2.0
 */
public class LiquifyTools {

    public static Image liquify(Image image, LiquifyConfig config) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = mara.mybox.image.tools.LiquifyTools.liquify(config.setSource(source));
        if (target == null || (config.getTask() != null && !config.getTask().isWorking())) {
            return null;
        }
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

}
