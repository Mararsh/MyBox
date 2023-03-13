package mara.mybox.controller;

import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2019-8-12
 * @License Apache License Version 2.0
 *
 * ImageManufactureController < ImageManufactureController_Actions <
 * ImageManufactureController_Image < ImageViewerController
 */
public class ImageManufactureController extends ImageManufactureController_Actions {

    public ImageManufactureController() {
        baseTitle = Languages.message("EditImage");
        TipsLabelKey = "ImageManufactureTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            imageLoaded = new SimpleBooleanProperty(false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initCreatePane();
            initHisTab();
            initBackupsTab();
            initEditBar();

            mainBox.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            rightPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));

            operationsController.setParameters(this);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(popButton, Languages.message("PopTabImage"));
            NodeStyleTools.setTooltip(viewImageButton, Languages.message("PopManufacturedImage"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void initBackupsTab() {
        try {
            backupController.setParameters(this, baseName);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initHisTab() {
        try {
            hisController.setParameters(this);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded() || image == null) {
                return false;
            }
            imageLoaded.set(true);
            imageChanged = false;
            resetImagePane();

            scopeController.setParameters(this);
            scopeSavedController.setParameters(this);
            operationsController.resetOperationPanes();

//            autoSize();
            hisTab.setDisable(sourceFile == null);
            backupTab.setDisable(sourceFile == null);
            hisController.recordImageHistory(ImageOperation.Load, image);
            backupController.loadBackups(sourceFile);
            updateLabelString(Languages.message("Loaded"));

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    /*
        static methods
     */
    public static ImageManufactureController open() {
        try {
            ImageManufactureController controller = (ImageManufactureController) WindowTools.openStage(Fxmls.ImageManufactureFxml);
            if (controller != null) {
                controller.requestMouse();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImageManufactureController openImage(Image image) {
        ImageManufactureController controller = open();
        if (controller != null) {
            controller.loadImage(image);
        }
        return controller;
    }

    public static ImageManufactureController openFile(File file) {
        ImageManufactureController controller = open();
        if (controller != null) {
            controller.loadImageFile(file);
        }
        return controller;
    }

    public static ImageManufactureController openImageInfo(ImageInformation imageInfo) {
        ImageManufactureController controller = open();
        if (controller != null) {
            controller.loadImageInfo(imageInfo);
        }
        return controller;
    }

    public static ImageManufactureController open(File file, ImageInformation imageInfo) {
        ImageManufactureController controller = open();
        if (controller != null) {
            controller.loadImage(file, imageInfo);
        }
        return controller;
    }

}
