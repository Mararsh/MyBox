package mara.mybox.controller;

import java.awt.Color;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ScopeTools;
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
    public void reset() {
        super.reset();
        cuttedClip = null;
    }

    @Override
    protected Image handleImage(Image inImage, ImageScope inScope) {
        try {
            operation = message("Crop");
            opInfo = null;
            Color bgColor = bgColorController.awtColor();
            handledImage = ScopeTools.selectedScope(
                    inImage, inScope, bgColor,
                    imageMarginsCheck.isSelected(),
                    excludeScope(), skipTransparent());
            if (handledImage == null || task == null || task.isCancelled()) {
                return null;
            }
            if (copyClipboardCheck.isSelected()) {
                cuttedClip = ScopeTools.selectedScope(
                        inImage, inScope, bgColor,
                        clipMarginsCheck.isSelected(),
                        excludeScope(), skipTransparent());
                ImageClipboard.add(cuttedClip, ImageClipboard.ImageSource.Crop);
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
            ImageCropController controller = (ImageCropController) WindowTools.branch(
                    parent.getMyWindow(), Fxmls.ImageCropFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
