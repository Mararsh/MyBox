package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

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
        baseTitle = message("EditImage");
        TipsLabelKey = "ImageManufactureTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            imageLoaded = new SimpleBooleanProperty(false);

        } catch (Exception e) {
            MyBoxLog.error(e);
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

            operationsController.setParameters(this);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(popButton, message("PopTabImage"));
            NodeStyleTools.setTooltip(viewImageButton, message("PopManufacturedImage"));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void initBackupsTab() {
        try {
            backupController.setParameters(this, baseName);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initHisTab() {
        try {
            hisController.setParameters(this);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            scopeController.reset();

            if (!super.afterImageLoaded() || image == null) {
                return false;
            }
            if (sourceFile == null) {
                saveAsTmp();
                return true;
            }
            imageLoaded.set(true);
            imageChanged = false;
            resetImagePane();

            hisTab.setDisable(sourceFile == null);
            hisController.loadHistories();
            backupController.loadBackups(sourceFile);

            finalRefineView();

            scopeController.setParameters(this);

            operationsController.resetOperationPanes();

            updateLabelString(message("Loaded"));

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean saveAsTmp() {
        if (image == null) {
            return false;
        }
        if (task != null) {
            task.cancel();
        }
        File tmpFile = FileTmpTools.generateFile("png");
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage((Image) image, null);
                if (bufferedImage == null || task == null || isCancelled()) {
                    return false;
                }
                return ImageFileWriters.writeImageFile(bufferedImage, "png", tmpFile.getAbsolutePath());
            }

            @Override
            protected void whenSucceeded() {
                sourceFileChanged(tmpFile);
            }
        };
        start(task);
        return true;
    }

    @Override
    public void setImageChanged(boolean imageChanged) {
        super.setImageChanged(imageChanged);
        recoverButton.setDisable(!imageChanged);
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
            MyBoxLog.error(e);
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
