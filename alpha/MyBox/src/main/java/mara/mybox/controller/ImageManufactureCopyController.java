package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.controller.ImageManufactureController_Image.ImageOperation;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ScopeTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-9-22
 * @License Apache License Version 2.0
 */
public class ImageManufactureCopyController extends ImageManufactureOperationController {

    @FXML
    protected ToggleGroup copyGroup;
    @FXML
    protected RadioButton includeRadio, excludeRadio, wholeRadio;
    @FXML
    protected ControlColorSet colorSetController;
    @FXML
    protected CheckBox clipboardCheck, marginsCheck;

    @Override
    public void initPane() {
        try {
            super.initPane();

            colorSetController.init(this, baseName + "CopyColor");

            clipboardCheck.setSelected(UserConfig.getBoolean(baseName + "CopyOpenClipboard", true));
            clipboardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "CopyOpenClipboard", clipboardCheck.isSelected());
                }
            });

            marginsCheck.setSelected(UserConfig.getBoolean(baseName + "CopyCutMargins", true));
            marginsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "CopyCutMargins", marginsCheck.isSelected());
                }
            });

            copyGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkCopyType();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void checkCopyType() {
        if (wholeRadio.isSelected()) {
            editor.imageTab();
        } else {
            showScope(true);
        }
    }

    @Override
    protected void paneExpanded() {
        editor.showRightPane();
        editor.resetImagePane();
        checkCopyType();
    }

    @FXML
    @Override
    public void okAction() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image newImage;

            @Override
            protected boolean handle() {
                try {
                    Color bgColor = colorSetController.color();
                    if (wholeRadio.isSelected()) {
                        newImage = imageView.getImage();

                    } else {
                        if (scopeController.scopeWhole()) {
                            newImage = imageView.getImage();
                        } else {
                            newImage = ScopeTools.scopeImage(imageView.getImage(),
                                    scopeController.scope, bgColor,
                                    marginsCheck.isSelected(),
                                    excludeRadio.isSelected(),
                                    true);
                        }
                    }

                    if (task == null || isCancelled()) {
                        return false;
                    }
                    return ImageClipboard.add(newImage, ImageClipboard.ImageSource.Copy) != null;
                } catch (Exception e) {
                    MyBoxLog.debug(e);
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                editor.popSuccessful();
                editor.updateLabel(ImageOperation.Copy);
                if (clipboardCheck.isSelected()) {
                    if (operationsController.clipboardController != null) {
                        operationsController.clipboardController.clipsController.refreshAction();
                    }
                    operationsController.clipboardPane.setExpanded(true);
                }
            }
        };
        start(task);
    }

    @Override
    public void resetOperationPane() {

    }

}
