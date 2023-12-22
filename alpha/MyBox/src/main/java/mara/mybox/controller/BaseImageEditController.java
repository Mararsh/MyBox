package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ScaleTools;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-11-3
 * @License Apache License Version 2.0
 */
public class BaseImageEditController extends BaseShapeController {

    protected BaseImageController imageController;
    protected String operation, opInfo;
    protected Image handledImage;
    protected ImageScope scope;
    protected boolean needFixSize;
    protected FxTask demoTask;

    @FXML
    protected CheckBox closeAfterCheck;

    protected void setParameters(BaseImageController parent) {
        try {
            if (parent == null) {
                close();
                return;
            }
            imageController = parent;
            needFixSize = true;

            imageController.loadNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    loadImage();
                }
            });

            closeAfterCheck.setSelected(UserConfig.getBoolean(baseName + "CloseAfterHandle", false));
            closeAfterCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "CloseAfterHandle", closeAfterCheck.isSelected());
                }
            });

            if (undoButton != null) {
                undoButton.disableProperty().bind(imageController.undoButton.disableProperty());
            }
            if (recoverButton != null) {
                recoverButton.disableProperty().bind(imageController.recoverButton.disableProperty());
            }
            if (saveButton != null) {
                saveButton.disableProperty().bind(imageController.saveButton.disableProperty());
            }

            getMyWindow().requestFocus();
            myStage.toFront();

            initMore();

            loadImage();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initMore() {
    }

    protected void loadImage() {
        if (imageController == null || !imageController.isShowing()) {
            close();
            return;
        }
        loadImage(srcImage());
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded() || image == null) {
                return false;
            }
            toFront();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void updateStageTitle() {
        try {
            if (getMyStage() == null) {
                return;
            }
            myStage.setTitle(getBaseTitle() + imageController.fileTitle());
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected Image srcImage() {
        return imageController.imageView.getImage();
    }

    protected Image currentImage() {
        return imageView.getImage();
    }

    protected File srcFile() {
        return imageController.sourceFile;
    }

    @Override
    public void fitView() {
        if (needFixSize) {
            paneSize();
            needFixSize = false;
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
        action(false);
    }

    @FXML
    @Override
    public void previewAction() {
        action(true);
    }

    protected void action(boolean isPreview) {
        if (isSettingValues || !checkOptions()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                handleImage(this);
                return !isCancelled() && handledImage != null;
            }

            @Override
            protected void whenSucceeded() {
                if (isCancelled()) {
                    return;
                }
                if (isPreview) {
                    ImagePopController c = ImagePopController.openImage(myController, handledImage);
                    c.setTitle(myController.getTitle());
                    c.imageLabel.setText(operation + " " + opInfo);
                } else {
                    passHandled(handledImage);
                }
                afterHandle();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                finalHandle();
            }

        };
        start(task);
    }

    protected void handleImage(FxTask currentTask) {
    }

    protected void passHandled(Image passImage) {
        imageController.updateImage(operation, opInfo, scope, passImage);
        if (closeAfterCheck.isSelected()) {
            close();
            imageController.popSuccessful();
        } else {
            toFront();
        }
    }

    protected void afterHandle() {
    }

    protected void finalHandle() {
    }

    protected boolean checkDemoOptions() {
        if (imageController == null || !imageController.isShowing()) {
            close();
            return false;
        }
        return srcImage() != null;
    }

    @FXML
    protected void demo() {
        if (!checkDemoOptions()) {
            return;
        }
        if (demoTask != null) {
            demoTask.cancel();
        }
        demoTask = new FxTask<Void>(this) {
            private List<String> files;

            @Override
            protected boolean handle() {
                try {
                    Image demoImage = ScaleTools.demoImage(srcImage());
                    if (demoImage == null || !isWorking()) {
                        return false;
                    }
                    files = new ArrayList<>();
                    makeDemoFiles(this, files, demoImage);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                if (files != null && !files.isEmpty()) {
                    ImagesBrowserController b
                            = (ImagesBrowserController) WindowTools.popStage(myController, Fxmls.ImagesBrowserFxml);
                    b.loadFiles(files);
                }
            }

        };
        start(demoTask);
    }

    protected void makeDemoFiles(FxTask currentTask, List<String> files, Image demoImage) {
    }

    @FXML
    @Override
    public void undoAction() {
        imageController.undoAction();
    }

    @FXML
    @Override
    public void recoverAction() {
        imageController.recoverAction();
    }

    @FXML
    @Override
    public void saveAction() {
        imageController.saveAction();
    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    @Override
    public boolean keyESC() {
        close();
        return false;
    }

}
