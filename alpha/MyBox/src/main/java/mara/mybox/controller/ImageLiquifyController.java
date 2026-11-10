package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.image.ImageViewTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2026-11-1
 * @License Apache License Version 2.0
 */
public class ImageLiquifyController extends BaseImageEditController {

    @FXML
    protected ControlImageLiquify liquifyController;

    public ImageLiquifyController() {
        baseTitle = message("Liquify");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Liquify");

            liquifyController.setImage(this);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void mousePressed(MouseEvent event) {
        if (imageView == null || imageView.getImage() == null
                || isPickingColor) {
            return;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        if (p == null) {
            return;
        }
        scrollPane.setPannable(false);
        lastPoint = p;
        liquifyController.setStart(p);
    }

    @FXML
    @Override
    public void mouseReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        if (imageView == null || imageView.getImage() == null
                || isPickingColor || lastPoint == null) {
            return;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        if (p == null) {
            return;
        }
        liquifyController.setEnd(p);
        lastPoint = null;
        okAction();
    }

    @Override
    public boolean checkOptions() {
        return super.checkOptions() && liquifyController.pickValues();
    }

    @Override
    protected void handleImage(FxTask currentTask) {
        if (liquifyController.liquify == null) {
            return;
        }
        opInfo = message("StartPoint") + ":" + liquifyController.startPoint.text() + " "
                + message("EndPoint") + ":" + liquifyController.endPoint.text();
        handledImage = liquifyController.liquify.render(srcImage(), currentTask);
        liquifyController.liquify = null;
    }

    /*
        static methods
     */
    public static ImageLiquifyController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageLiquifyController controller = (ImageLiquifyController) WindowTools.referredStage(
                    parent, Fxmls.ImageLiquifyFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
