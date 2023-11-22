package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ShapeTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageEraserController extends ImagePolylinesController {

    public ImageEraserController() {
        baseTitle = message("Eraser");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = "Eraser";

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void handleImage() {
        handledImage = ShapeTools.drawErase(srcImage(), maskPolylinesData, shapeStyle);
    }

    /*
        static methods
     */
    public static ImageEraserController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageEraserController controller = (ImageEraserController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageEraserFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
