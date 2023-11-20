package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.data.DoublePolylines;
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
public class ImageEraserController extends BaseImageEditController {

    @FXML
    protected ControlStroke strokeController;

    public ImageEraserController() {
        baseTitle = message("Eraser");
    }

    @Override
    protected void initMore() {
        try {
            operation = "Eraser";
            strokeController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded() || image == null) {
                return false;
            }
            showAnchors = false;
            popItemMenu = popAnchorMenuCheck.isSelected();
            addPointWhenClick = false;
            popShapeMenu = false;
            supportPath = false;
            maskPolylinesData = new DoublePolylines();
            shapeStyle = strokeController.style;

            strokeController.setWidthList((int) (image.getWidth() / 20),
                    (int) shapeStyle.getStrokeWidth());

            maskPolylinesData = null;
            showMaskPolylines();

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    @FXML
    @Override
    public void goAction() {
        shapeStyle = strokeController.pickValues();
        drawMaskPolylines();
    }

    @FXML
    @Override
    public boolean withdrawAction() {
        if (imageView == null || imageView.getImage() == null) {
            return false;
        }
        maskPolylinesData.removeLastLine();
        drawMaskPolylines();
        return true;
    }

    @FXML
    @Override
    public void clearAction() {
        loadImage(srcImage());

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
