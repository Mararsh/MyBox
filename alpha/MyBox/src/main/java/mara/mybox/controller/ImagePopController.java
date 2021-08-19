package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-9-18
 * @License Apache License Version 2.0
 */
public class ImagePopController extends BaseImageController {

    protected ImageView sourceImageView;
    protected ChangeListener listener;
    protected ImageManufactureScopeController scopeController;

    @FXML
    protected CheckBox sychronizedCheck;
    @FXML
    protected Button refreshButton;

    @Override
    public void setStageStatus(String prefix, int minSize) {
        setAsPopup(baseName);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            saveAsType = SaveAsType.Open;

            listener = new ChangeListener<Image>() {
                @Override
                public void changed(ObservableValue ov, Image oldv, Image newv) {
                    if (sychronizedCheck.isVisible() && sychronizedCheck.isSelected()) {
                        refreshAction();
                    }
                }
            };

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setSourceImageView(ImageView sourceImageView) {
        try {
            this.sourceImageView = sourceImageView;
            refreshAction();

            sychronizedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldState, Boolean newState) {
                    if (sourceImageView == null) {
                        sychronizedCheck.setVisible(false);
                        refreshButton.setVisible(false);
                        return;
                    }
                    if (sychronizedCheck.isVisible() && sychronizedCheck.isSelected()) {
                        sourceImageView.imageProperty().addListener(listener);
                    } else {
                        sourceImageView.imageProperty().removeListener(listener);
                    }
                }
            });
            sychronizedCheck.setSelected(UserConfig.getBoolean(baseName + "Sychronized", true));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setScopeController(ImageManufactureScopeController scopeController) {
        try {
            this.scopeController = scopeController;
            this.sourceImageView = scopeController.scopeView;

            refreshAction();

            sychronizedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldState, Boolean newState) {
                    if (scopeController == null) {
                        sychronizedCheck.setVisible(false);
                        refreshButton.setVisible(false);
                        return;
                    }
                    if (sychronizedCheck.isVisible() && sychronizedCheck.isSelected()) {
                        sourceImageView.imageProperty().addListener(listener);
                    } else {
                        sourceImageView.imageProperty().removeListener(listener);
                    }
                }
            });
            sychronizedCheck.setSelected(UserConfig.getBoolean(baseName + "Sychronized", true));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setImage(Image image) {
        try {
            sychronizedCheck.setVisible(false);
            refreshButton.setVisible(false);

            loadImage(image);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void refreshAction() {
        if (scopeController != null) {
            makeScope();

        } else if (sourceImageView != null) {
            loadImage(sourceImageView.getImage());

        } else {
            sychronizedCheck.setVisible(false);
            refreshButton.setVisible(false);

        }
    }

    public void makeScope() {
        if (scopeController == null) {
            return;
        }
        synchronized (this) {
            SingletonTask popTask = new SingletonTask<Void>() {

                private Image scopeImage;

                @Override
                protected boolean handle() {
                    try {
                        PixelsOperation pixelsOperation = PixelsOperationFactory.create(scopeController.imageView.getImage(),
                                scopeController.scope, PixelsOperation.OperationType.PreOpacity, PixelsOperation.ColorActionType.Set);
                        pixelsOperation.setSkipTransparent(scopeController.ignoreTransparentCheck.isSelected());
                        pixelsOperation.setIntPara1(255 - (int) (scopeController.opacity * 255));
                        pixelsOperation.setExcludeScope(true);
                        scopeImage = pixelsOperation.operateFxImage();
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    loadImage(scopeImage);
                }
            };
            popTask.setSelf(popTask);
            Thread thread = new Thread(popTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @Override
    public void cleanPane() {
        try {
            if (sourceImageView != null) {
                sourceImageView.imageProperty().removeListener(listener);
                sourceImageView = null;
            }
            scopeController = null;
            listener = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }


    /*
        static methods
     */
    public static ImagePopController openImage(BaseController parent, Image image) {
        try {
            if (parent == null || image == null) {
                return null;
            }
            ImagePopController controller = (ImagePopController) WindowTools.openChildStage(parent.getMyWindow(), Fxmls.ImagePopFxml, false);
            controller.setImage(image);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImagePopController openView(BaseController parent, ImageView imageView) {
        try {
            if (parent == null || imageView == null) {
                return null;
            }
            ImagePopController controller = (ImagePopController) WindowTools.openChildStage(parent.getMyWindow(), Fxmls.ImagePopFxml, false);
            controller.setSourceImageView(imageView);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImagePopController openScope(ImageManufactureScopeController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImagePopController controller = (ImagePopController) WindowTools.openChildStage(parent.getMyWindow(), Fxmls.ImagePopFxml, false);
            controller.setScopeController(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
