package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.image.ImageClipboard;
import mara.mybox.image.ImageScope;
import mara.mybox.value.AppVariables;

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

            copyToSystemClipboardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue("CopyToSystemClipboard", copyToSystemClipboardCheck.isSelected());
                }
            });
            copyToSystemClipboardCheck.setSelected(AppVariables.getUserConfigBoolean("CopyToSystemClipboard", true));

            clipboardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "CopyOpenClipboard", clipboardCheck.isSelected());
                }
            });
            clipboardCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "CopyOpenClipboard", true));

            marginsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "CopyCutMargins", marginsCheck.isSelected());
                }
            });
            marginsCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "CopyCutMargins", true));

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
            imageController.hideImagePane();
            imageController.showScopePane();
        } else {
            imageController.hideScopePane();
            imageController.showImagePane();
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
                                newImage = FxmlImageManufacture.scopeImage(imageView.getImage(),
                                        scopeController.scope, bgColor, marginsCheck.isSelected());
                            }

                        } else if (excludeRadio.isSelected()) {
                            if (scopeController.scope == null
                                    || scopeController.scope.getScopeType() == ImageScope.ScopeType.All
                                    || scopeController.scope.getScopeType() == ImageScope.ScopeType.Operate) {
                                return false;
                            } else {
                                newImage = FxmlImageManufacture.scopeExcludeImage(imageView.getImage(),
                                        scopeController.scope, bgColor, marginsCheck.isSelected());
                            }
                        }

                        if (task == null || isCancelled()) {
                            return false;
                        }
                        return ImageClipboard.add(newImage, copyToSystemClipboardCheck.isSelected()) != null;
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    imageController.popSuccessful();
                    imageController.updateBottom(ImageOperation.Copy);
                    if (clipboardCheck.isSelected()) {
                        if (operationsController.clipboardController != null) {
                            operationsController.clipboardController.loadClipboard();
                        }
                        operationsController.clipboardPane.setExpanded(true);
                    }
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public void resetOperationPane() {

    }

}
