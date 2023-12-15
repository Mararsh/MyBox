package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.MarginTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-4
 * @License Apache License Version 2.0
 */
public class ImageMarginsController extends BaseImageEditController {

    @FXML
    protected ControlImageMargins marginsController;
    @FXML
    protected Label commentsLabel;

    public ImageMarginsController() {
        baseTitle = message("Margins");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Margins");

            marginsController.setParameters(this);

            okButton.disableProperty().bind(marginsController.widthSelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle())
                    .or(marginsController.distanceInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded()) {
                return false;
            }
            image = currentImage();
            if (image == null) {
                return false;
            }
            int width = (int) image.getWidth();
            int height = (int) image.getWidth();
            String info = message("CurrentSize") + ": " + width + "x" + height;
            commentsLabel.setText(info);

            marginsController.imageWidth(width);

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    @Override
    public void maskShapeDataChanged() {
        if (!marginsController.dragRadio.isSelected()
                || maskRectangleData == null || image == null) {
            return;
        }
        super.maskShapeDataChanged();
        String info = message("CurrentSize") + ": " + (int) (image.getWidth())
                + "x" + (int) (image.getHeight()) + "  "
                + message("AfterChange") + ": " + (int) (maskRectangleData.getWidth())
                + "x" + (int) (maskRectangleData.getHeight());
        commentsLabel.setText(info);
    }

    public void selectAllRect() {
        if (imageView.getImage() == null) {
            return;
        }
        maskRectangleData = DoubleRectangle.xywh(0, 0,
                imageView.getImage().getWidth(), imageView.getImage().getHeight());
        maskShapeDataChanged();
    }

    @Override
    protected boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        return marginsController.pickValues();
    }

    @Override
    protected void handleImage() {
        if (marginsController.dragRadio.isSelected()) {
            handledImage = MarginTools.dragMarginsFx(task, currentImage(),
                    marginsController.colorController.color(), maskRectangleData);

        } else if (marginsController.addRadio.isSelected()) {
            handledImage = MarginTools.addMarginsFx(task, currentImage(),
                    marginsController.colorController.color(),
                    marginsController.margin,
                    marginsController.marginsTopCheck.isSelected(),
                    marginsController.marginsBottomCheck.isSelected(),
                    marginsController.marginsLeftCheck.isSelected(),
                    marginsController.marginsRightCheck.isSelected());
            opInfo = marginsController.margin + "";

        } else if (marginsController.blurRadio.isSelected()) {
            handledImage = MarginTools.blurMarginsAlpha(task, currentImage(),
                    marginsController.margin,
                    marginsController.marginsTopCheck.isSelected(),
                    marginsController.marginsBottomCheck.isSelected(),
                    marginsController.marginsLeftCheck.isSelected(),
                    marginsController.marginsRightCheck.isSelected());
            opInfo = marginsController.margin + "";

        } else if (marginsController.cutColorRadio.isSelected()) {
            handledImage = MarginTools.cutMarginsByColor(task, currentImage(),
                    marginsController.colorController.color(),
                    marginsController.distance,
                    marginsController.marginsTopCheck.isSelected(),
                    marginsController.marginsBottomCheck.isSelected(),
                    marginsController.marginsLeftCheck.isSelected(),
                    marginsController.marginsRightCheck.isSelected());
            opInfo = marginsController.distance + "";

        } else if (marginsController.cutWidthRadio.isSelected()) {
            handledImage = MarginTools.cutMarginsByWidth(task, currentImage(),
                    marginsController.margin,
                    marginsController.marginsTopCheck.isSelected(),
                    marginsController.marginsBottomCheck.isSelected(),
                    marginsController.marginsLeftCheck.isSelected(),
                    marginsController.marginsRightCheck.isSelected());
            opInfo = marginsController.margin + "";
        }
    }

    /*
        static methods
     */
    public static ImageMarginsController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageMarginsController controller = (ImageMarginsController) WindowTools.branchStage(
                    parent, Fxmls.ImageMarginsFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
