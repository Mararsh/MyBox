package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2020-10-31
 * @License Apache License Version 2.0
 */
public class ImageTooLargeController extends BaseController {

    protected BaseImageController parent;
    protected ImageInformation imageInfo;

    @FXML
    protected Label infoLabel;

    public ImageTooLargeController() {
    }

    @Override
    public void initControls() {
        super.initControls();

    }

    public void setParameters(BaseImageController parent, ImageInformation imageInfo) {
        if (parent == null || imageInfo == null) {
            return;
        }
        this.parent = parent;
        this.imageInfo = imageInfo;
        infoLabel.setText(imageInfo.sampleInformation(null));
    }

    @FXML
    public void sample() {
        thisPane.setDisable(true);
        if (parent.baseName.equals("ImageSample")) {
            parent.loadImageInfo(imageInfo);
            closeStage();
        } else {
            ImageSampleController controller = (ImageSampleController) loadScene(Fxmls.ImageSampleFxml);
            controller.loadImageInfo(imageInfo);
            parent.closeStage();
            closeStage();
        }
    }

    @FXML
    public void split() {
        thisPane.setDisable(true);
        if (parent.baseName.equals("ImageSplit")) {
            parent.loadImageInfo(imageInfo);
            closeStage();
        } else {
            ImageSplitController controller = (ImageSplitController) loadScene(Fxmls.ImageSplitFxml);
            controller.loadImageInfo(imageInfo);
            parent.closeStage();
            closeStage();
        }
    }

    @FXML
    public void mem() {
        thisPane.setDisable(true);
        SettingsController controller = SettingsController.oneOpen(this);
        controller.tabPane.getSelectionModel().select(controller.baseTab);
        closeStage();
    }

    @FXML
    public void view() {
        thisPane.setDisable(true);
        if (parent.baseName.equals("ImageViewer")) {
            parent.loadImageInfo(imageInfo);
            closeStage();
        } else {
            ImageViewerController controller = (ImageViewerController) loadScene(Fxmls.ImageViewerFxml);
            controller.loadImageInfo(imageInfo);
            parent.closeStage();
            closeStage();
        }
    }

    @FXML
    public void cancel() {
        closeStage();
    }

}
