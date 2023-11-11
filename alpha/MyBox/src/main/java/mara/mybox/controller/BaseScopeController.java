package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ScaleTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public abstract class BaseScopeController extends BaseChildController {

    protected BaseImageController imageController;
    protected ImageEditorController editor;
    protected String operation, opInfo;
    protected Image handledImage;

    @FXML
    protected ControlImageScopeInput scopeController;
    @FXML
    protected ControlColorSet bgColorController;
    @FXML
    protected ToggleGroup selectGroup;
    @FXML
    protected RadioButton includeRadio, excludeRadio, wholeRadio;
    @FXML
    protected CheckBox ignoreTransparentCheck;

    protected void setParameters(BaseImageController parent) {
        try {
            if (parent == null) {
                close();
            }
            this.imageController = parent;
            if (imageController instanceof ImageEditorController) {
                editor = (ImageEditorController) imageController;
            }
            scopeController.setParameters(this);

            if (bgColorController != null) {
                bgColorController.init(this, baseName + "BackgroundColor", Color.DARKGREEN);
            }

            if (wholeRadio != null) {
                String select = UserConfig.getString(baseName + "SelectType", "Whole");
                if ("Include".equals(select)) {
                    includeRadio.setSelected(true);
                } else if ("Exclude".equals(select)) {
                    excludeRadio.setSelected(true);
                } else {
                    wholeRadio.setSelected(true);
                }
                selectGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                        if (includeRadio.isSelected()) {
                            UserConfig.setString(baseName + "SelectType", "Include");
                            showLeftPane();
                        } else if (excludeRadio.isSelected()) {
                            UserConfig.setString(baseName + "SelectType", "Exclude");
                            showLeftPane();
                        } else {
                            UserConfig.setString(baseName + "SelectType", "Whole");
                            hideLeftPane();
                        }
                    }
                });
                if (wholeRadio.isSelected()) {
                    hideLeftPane();
                } else if (excludeRadio.isSelected()) {
                    showLeftPane();
                }

            } else {
                String select = UserConfig.getString(baseName + "SelectType", "Include");
                if ("Exclude".equals(select)) {
                    excludeRadio.setSelected(true);
                } else {
                    includeRadio.setSelected(true);
                }
                selectGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                        if (includeRadio.isSelected()) {
                            UserConfig.setString(baseName + "SelectType", "Include");
                        } else if (excludeRadio.isSelected()) {
                            UserConfig.setString(baseName + "SelectType", "Exclude");
                        }
                    }
                });
            }

            ignoreTransparentCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (!isSettingValues) {
                        scopeController.indicateScope();
                    }
                }
            });

            reset();

            initMore();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initMore() {
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            return scopeController.keyEventsFilter(event);
        } else {
            return true;
        }
    }

    public void reset() {
        operation = null;
        opInfo = null;
        handledImage = null;
    }

    protected Image srcImage() {
        return scopeController.srcImage();
    }

    public ImageScope scope() {
        if (wholeRadio != null && wholeRadio.isSelected()) {
            return null;
        } else {
            return scopeController.finalScope();
        }
    }

    protected boolean checkOptions() {
        if (imageController == null || !imageController.isShowing()) {
            close();
            return false;
        }
        return srcImage() != null;
    }

    @FXML
    @Override
    public void okAction() {
        if (!checkOptions()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        reset();
        task = new SingletonCurrentTask<Void>(this) {
            private ImageScope scope;

            @Override
            protected boolean handle() {
                try {
                    scope = scope();
                    handledImage = handleImage(srcImage(), scope);
                    return handledImage != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                editor.updateImage(operation, opInfo, scope, handledImage, cost);
                if (closeAfterCheck.isSelected()) {
                    close();
                }
                afterHandle();
            }
        };
        start(task);
    }

    protected Image handleImage(Image inImage, ImageScope inScope) {
        return null;
    }

    protected void afterHandle() {

    }

    @FXML
    protected void demo() {
        if (!checkOptions()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        reset();
        task = new SingletonCurrentTask<Void>(this) {
            private Image demoImage;

            @Override
            protected boolean handle() {
                try {
                    demoImage = ScaleTools.demoImage(srcImage());
                    demoImage = handleImage(demoImage, scope());
                    return demoImage != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                ImagePopController.openImage(myController, demoImage);
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void undoAction() {
        if (editor != null) {
            editor.undoAction();
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        if (editor != null) {
            editor.recoverAction();
        }
    }

    @FXML
    @Override
    public void saveAction() {
        imageController.saveAction();
    }

    /*
        static methods
     */
    public static BaseScopeController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            BaseScopeController controller = (BaseScopeController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageBlackWhiteFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
