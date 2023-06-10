package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.controller.ImageManufactureController_Image.ImageOperation;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ScopeTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageManufactureCropController extends ImageManufactureOperationController {

    protected int centerX, centerY, radius;

    @FXML
    protected ToggleGroup copyGroup;
    @FXML
    protected RadioButton includeRadio, excludeRadio;
    @FXML
    protected ColorSetController colorSetController;
    @FXML
    protected CheckBox clipboardCheck, clipMarginsCheck, imageMarginsCheck;

    @Override
    public void initPane() {
        try {
            super.initPane();

            colorSetController.init(this, baseName + "CropColor");

            clipboardCheck.setSelected(UserConfig.getBoolean(baseName + "CropPutClipboard", false));
            clipboardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "CropPutClipboard", clipboardCheck.isSelected());
                }
            });

            imageMarginsCheck.setSelected(UserConfig.getBoolean(baseName + "CropCutImageMargins", true));
            imageMarginsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "CropCutImageMargins", imageMarginsCheck.isSelected());
                }
            });

            clipMarginsCheck.setSelected(UserConfig.getBoolean(baseName + "CropCutClipMargins", true));
            clipMarginsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "CropCutClipMargins", clipMarginsCheck.isSelected());
                }
            });

            clipMarginsCheck.disableProperty().bind(clipboardCheck.selectedProperty().not());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void paneExpanded() {
        imageController.showRightPane();
        imageController.resetImagePane();
        imageController.scopeTab();
        if (scopeController.scopeWhole()
                || scopeController.scope.getScopeType() == ImageScope.ScopeType.Operate) {
            scopeController.scopeRectangleRadio.setSelected(true);
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (scopeController.scopeWhole()
                || scopeController.scope.getScopeType() == ImageScope.ScopeType.Operate) {
            popError(Languages.message("InvalidScope"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image newImage, cuttedClip;

            @Override
            protected boolean handle() {
                Color bgColor = colorSetController.color();

                if (includeRadio.isSelected()) {
                    newImage = ScopeTools.scopeExcludeImage(imageView.getImage(),
                            scopeController.scope, bgColor, imageMarginsCheck.isSelected());

                } else if (excludeRadio.isSelected()) {
                    newImage = ScopeTools.scopeImage(imageView.getImage(),
                            scopeController.scope, bgColor, imageMarginsCheck.isSelected());
                } else {
                    return false;
                }
                if (task == null || isCancelled()) {
                    return false;
                }
                if (UserConfig.getBoolean(baseName + "CropPutClipboard", false)) {
                    if (includeRadio.isSelected()) {
                        cuttedClip = ScopeTools.scopeImage(imageView.getImage(),
                                scopeController.scope, bgColor, clipMarginsCheck.isSelected());

                    } else if (excludeRadio.isSelected()) {
                        cuttedClip = ScopeTools.scopeExcludeImage(imageView.getImage(),
                                scopeController.scope, bgColor, clipMarginsCheck.isSelected());
                    }
                    ImageClipboard.add(cuttedClip, ImageClipboard.ImageSource.Crop);
                }
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                imageController.popSuccessful();
                if (excludeRadio.isSelected() && imageMarginsCheck.isSelected()) {
                    scopeController.scopeAllRadio.setSelected(true);
                }
                imageController.updateImage(ImageOperation.Crop, newImage, cost);
                if (cuttedClip != null) {
                    if (operationsController.clipboardController != null) {
                        operationsController.clipboardController.clipsController.refreshAction();
                    }
                    operationsController.clipboardPane.setExpanded(true);
                }
            }
        };
        start(task);
    }

}
