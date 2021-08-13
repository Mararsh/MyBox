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
    protected ColorSet colorSetController;
    @FXML
    protected CheckBox clipboardCheck, marginsCheck;

    @Override
    public void initPane() {
        try {
            colorSetController.init(this, baseName + "CopyColor");

            clipboardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "CopyOpenClipboard", clipboardCheck.isSelected());
                }
            });
            clipboardCheck.setSelected(UserConfig.getBoolean(baseName + "CopyOpenClipboard", true));

            marginsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "CopyCutMargins", marginsCheck.isSelected());
                }
            });
            marginsCheck.setSelected(UserConfig.getBoolean(baseName + "CopyCutMargins", true));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void paneExpanded() {
        imageController.showRightPane();
        imageController.resetImagePane();
        if (scopeController != null && scopeController.scope != null
                && scopeController.scope.getScopeType() != ImageScope.ScopeType.All) {
            imageController.scopeTab();
        } else {
            imageController.imageTab();
        }
    }

    @FXML
    @Override
    public void okAction() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image newImage;

                @Override
                protected boolean handle() {
                    try {
                        Color bgColor = colorSetController.color();
                        if (wholeRadio.isSelected()) {
                            newImage = imageView.getImage();

                        } else if (includeRadio.isSelected()) {
                            if (scopeController.scope == null
                                    || scopeController.scope.getScopeType() == ImageScope.ScopeType.All
                                    || scopeController.scope.getScopeType() == ImageScope.ScopeType.Operate) {
                                newImage = imageView.getImage();
                            } else {
                                newImage = ScopeTools.scopeImage(imageView.getImage(),
                                        scopeController.scope, bgColor, marginsCheck.isSelected());
                            }

                        } else if (excludeRadio.isSelected()) {
                            if (scopeController.scope == null
                                    || scopeController.scope.getScopeType() == ImageScope.ScopeType.All
                                    || scopeController.scope.getScopeType() == ImageScope.ScopeType.Operate) {
                                return false;
                            } else {
                                newImage = ScopeTools.scopeExcludeImage(imageView.getImage(),
                                        scopeController.scope, bgColor, marginsCheck.isSelected());
                            }
                        }

                        if (task == null || isCancelled()) {
                            return false;
                        }
                        return ImageClipboard.add(newImage, ImageClipboard.ImageSource.Copy) != null;
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    imageController.popSuccessful();
                    imageController.updateLabel(ImageOperation.Copy);
                    if (clipboardCheck.isSelected()) {
                        if (operationsController.clipboardController != null) {
                            operationsController.clipboardController.clipsController.refreshAction();
                        }
                        operationsController.clipboardPane.setExpanded(true);
                    }
                }
            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @Override
    public void resetOperationPane() {

    }

}
