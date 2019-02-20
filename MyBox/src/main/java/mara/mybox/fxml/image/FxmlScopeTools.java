package mara.mybox.fxml.image;

import mara.mybox.fxml.image.ImageTools;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.image.ImageScopeTools;
import mara.mybox.data.IntCircle;
import mara.mybox.data.IntRectangle;

/**
 * @Author Mara
 * @CreateDate 2018-11-10 19:44:49
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlScopeTools {

    public static Image indicateRectangle(Image image,
            Color color, int lineWidth, IntRectangle rect) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageScopeTools.indicateRectangle(source,
                ImageTools.colorConvert(color), lineWidth, rect);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public static Image indicateCircle(Image image,
            Color color, int lineWidth, IntCircle circle) {
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage target = ImageScopeTools.indicateCircle(source,
                ImageTools.colorConvert(color), lineWidth, circle);
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

}
