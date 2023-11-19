package mara.mybox.controller;

import java.io.File;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @License Apache License Version 2.0
 */
public class ImageViewerController extends BaseImageController {

    public ImageViewerController() {
        baseTitle = message("ImageViewer");
    }

    /*
        static methods
     */
    public static ImageViewerController open() {
        try {
            ImageViewerController controller = (ImageViewerController) WindowTools.openStage(Fxmls.ImageViewerFxml);
            if (controller != null) {
                controller.requestMouse();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static ImageViewerController openFile(File file) {
        try {
            ImageViewerController controller = open();
            if (controller != null && file != null) {
                controller.loadImageFile(file);
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static ImageViewerController openImage(Image image) {
        ImageViewerController controller = open();
        if (controller != null) {
            controller.loadImage(image);
        }
        return controller;
    }

    public static ImageViewerController openImageInfo(ImageInformation info) {
        ImageViewerController controller = open();
        if (controller != null) {
            controller.loadImageInfo(info);
        }
        return controller;
    }

}
