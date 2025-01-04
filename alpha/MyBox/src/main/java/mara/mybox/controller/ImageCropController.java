package mara.mybox.controller;

import java.awt.Color;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import mara.mybox.image.data.ImageScope;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.image.ScopeTools;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-10-28
 * @License Apache License Version 2.0
 */
public class ImageCropController extends BasePixelsController {

    private Image cuttedClip;

    @FXML
    protected ToggleGroup targetGroup;
    @FXML
    protected CheckBox clipMarginsCheck, imageMarginsCheck,
            copyClipboardCheck, openClipboardCheck;
    @FXML
    protected ControlColorSet bgColorController;

    public ImageCropController() {
        baseTitle = message("Crop");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Crop");

            bgColorController.init(this, baseName + "BackgroundColor", javafx.scene.paint.Color.DARKGREEN);

            imageMarginsCheck.setSelected(UserConfig.getBoolean(baseName + "ImageCutMargins", true));
            imageMarginsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "ImageCutMargins", imageMarginsCheck.isSelected());
                }
            });

            copyClipboardCheck.setSelected(UserConfig.getBoolean(baseName + "CopyClipboard", false));
            copyClipboardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "CopyClipboard", copyClipboardCheck.isSelected());
                }
            });

            clipMarginsCheck.setSelected(UserConfig.getBoolean(baseName + "ClipCutMargins", true));
            clipMarginsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "ClipCutMargins", clipMarginsCheck.isSelected());
                }
            });
            clipMarginsCheck.visibleProperty().bind(copyClipboardCheck.selectedProperty());

            openClipboardCheck.setSelected(UserConfig.getBoolean(baseName + "OpenClipboard", true));
            openClipboardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "OpenClipboard", openClipboardCheck.isSelected());
                }
            });
            openClipboardCheck.visibleProperty().bind(copyClipboardCheck.selectedProperty());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected Image handleImage(FxTask currentTask, Image inImage, ImageScope inScope) {
        try {
            operation = message("Crop");
            opInfo = null;
            cuttedClip = null;
            Color color = bgColorController.awtColor();
            handledImage = ScopeTools.selectedScope(currentTask,
                    inImage, inScope, color,
                    imageMarginsCheck.isSelected(),
                    !excludeScope(), skipTransparent());
            if (handledImage == null || currentTask == null || !currentTask.isWorking()) {
                return null;
            }
            if (copyClipboardCheck.isSelected()) {
                cuttedClip = ScopeTools.selectedScope(currentTask,
                        inImage, inScope, color,
                        clipMarginsCheck.isSelected(),
                        excludeScope(), skipTransparent());
                if (cuttedClip == null || currentTask == null || !currentTask.isWorking()) {
                    return null;
                }
                ImageClipboard.add(currentTask, cuttedClip, ImageClipboard.ImageSource.Crop);
            }
            return handledImage;
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    @Override
    protected void afterHandle() {
        if (copyClipboardCheck.isSelected() && openClipboardCheck.isSelected()
                && cuttedClip != null) {
            ImageInMyBoxClipboardController.oneOpen();
        }
    }

    /*
        static methods
     */
    public static ImageCropController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageCropController controller = (ImageCropController) WindowTools.branchStage(
                    parent, Fxmls.ImageCropFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
