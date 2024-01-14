package mara.mybox.controller;

import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-10-31
 * @License Apache License Version 2.0
 */
public class ImageTooLargeController extends BaseController {

    protected BaseImageController imageController;
    protected ImageInformation imageInfo;
    protected int scale;

    @FXML
    protected Label infoLabel;
    @FXML
    protected ComboBox<String> scaleSelector;

    public ImageTooLargeController() {
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            List<String> values = Arrays.asList("2", "3", "4", "5", "6", "8", "9", "10", "15", "20",
                    "25", "30", "50", "80", "100", "200", "500", "800", "1000");
            scaleSelector.getItems().addAll(values);
            scaleSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkScale();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseImageController parent, ImageInformation info) {
        if (parent == null || info == null) {
            return;
        }
        imageController = parent;
        imageInfo = info;
        infoLabel.setText(imageInfo.sampleInformation(null, null));
        scale = imageInfo.getSampleScale();
        scaleSelector.setValue(scale + "");
    }

    public boolean checkScale() {
        if (isSettingValues) {
            return true;
        }
        int v;
        try {
            v = Integer.parseInt(scaleSelector.getValue());
        } catch (Exception e) {
            v = -1;
        }
        if (v > 1) {
            scale = v;
            ValidationTools.setEditorNormal(scaleSelector);
        } else {
            ValidationTools.setEditorBadStyle(scaleSelector);
            popError(message("InvalidParameter") + ": " + message("SampleScale"));
            return false;
        }
        imageInfo.setXscale(scale);
        imageInfo.setYscale(scale);
        imageInfo.setSampleScale(0);
        imageController.loadWidth = -1;
        return true;
    }

    @FXML
    public void sample() {
        if (!checkScale()) {
            return;
        }
        thisPane.setDisable(true);
        if (imageController.baseName.equals("ImageSample")) {
            imageController.loadImageInfo(imageInfo);
        } else {
            ImageSampleController controller = (ImageSampleController) loadScene(Fxmls.ImageSampleFxml);
            controller.loadImageInfo(imageInfo);
            imageController.closeStage();
        }
        closeStage();
    }

    @FXML
    public void split() {
        if (!checkScale()) {
            return;
        }
        thisPane.setDisable(true);
        if (imageController.baseName.equals("ImageSplit")) {
            imageController.loadImageInfo(imageInfo);
        } else {
            ImageSplitController controller = (ImageSplitController) loadScene(Fxmls.ImageSplitFxml);
            controller.loadImageInfo(imageInfo);
            imageController.closeStage();
        }
        closeStage();
    }

    @FXML
    public void edit() {
        if (!checkScale()) {
            return;
        }
        thisPane.setDisable(true);
        ImageEditorController.openImageInfo(imageInfo);
        imageController.closeStage();
        closeStage();
    }

    @FXML
    public void mem() {
        thisPane.setDisable(true);
        SettingsController controller = SettingsController.oneOpen(this);
        controller.tabPane.getSelectionModel().select(controller.baseTab);
        closeStage();
    }

    @FXML
    public void cancel() {
        closeStage();
    }

}
