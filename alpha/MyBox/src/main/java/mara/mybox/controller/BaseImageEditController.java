package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-11-3
 * @License Apache License Version 2.0
 */
public class BaseImageEditController extends BaseShapeController {

    protected ImageEditorController editor;
    protected String operation, opInfo;
    protected Image handledImage;

    @FXML
    protected CheckBox closeAfterCheck;

    @Override
    public void setStageStatus() {
        setAsPop(baseName);
    }

    protected void setParameters(ImageEditorController parent) {
        try {
            if (parent == null) {
                close();
                return;
            }
            editor = parent;

            editor.loadNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    loadImage();
                }
            });

            closeAfterCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(interfaceName + "SaveClose", closeAfterCheck.isSelected());
                }
            });
            closeAfterCheck.setSelected(UserConfig.getBoolean(interfaceName + "SaveClose", false));

            if (undoButton != null) {
                undoButton.disableProperty().bind(editor.undoButton.disableProperty());
            }
            if (recoverButton != null) {
                recoverButton.disableProperty().bind(editor.recoverButton.disableProperty());
            }
            if (saveButton != null) {
                saveButton.disableProperty().bind(editor.saveButton.disableProperty());
            }

            initMore();

            refreshAction();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initMore() {
    }

    public void reset() {
        operation = null;
        opInfo = null;
        handledImage = null;
    }

    protected Image srcImage() {
        return editor.imageView.getImage();
    }

    protected Image currentImage() {
        return imageView.getImage();
    }

    protected void loadImage() {
        if (editor == null || !editor.isShowing()) {
            close();
            return;
        }
        loadImage(srcImage());
    }

    protected boolean checkOptions() {
        if (editor == null || !editor.isShowing()) {
            close();
            return false;
        }
        return true;
    }

    @FXML
    @Override
    public void okAction() {
        if (goButton != null && !goButton.isDisabled()) {
            editor.updateImage(operation, currentImage(), -1);
            if (closeAfterCheck.isSelected()) {
                close();
            }
            return;
        }

        action(false);
    }

    @FXML
    @Override
    public void previewAction() {
        action(true);
    }

    protected synchronized void action(boolean isPreview) {
        if (isSettingValues || !checkOptions()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        reset();
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                handledImage = null;
                opInfo = null;
                handleImage();
                if (task == null || isCancelled()) {
                    return false;
                }
                return handledImage != null;
            }

            @Override
            protected void whenSucceeded() {
                if (isCancelled()) {
                    return;
                }
                if (isPreview) {
                    ImagePopController.openImage(myController, handledImage);
                } else {
                    popSuccessful();
                    editor.updateImage(operation, opInfo, null, handledImage, cost);
                    if (closeAfterCheck.isSelected()) {
                        close();
                    }
                }
            }

        };
        start(task);
    }

    protected void handleImage() {
    }

    @FXML
    @Override
    public void goAction() {
        if (goButton == null || goButton.isDisabled()) {
            return;
        }
        if (isSettingValues || !checkOptions()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        reset();
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                handledImage = null;
                opInfo = null;
                handleImage();
                if (task == null || isCancelled()) {
                    return false;
                }
                return handledImage != null;
            }

            @Override
            protected void whenSucceeded() {
                if (isCancelled()) {
                    return;
                }
                loadImage(handledImage);
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void undoAction() {
        editor.undoAction();
    }

    @FXML
    @Override
    public void recoverAction() {
        editor.recoverAction();
    }

    @FXML
    @Override
    public void saveAction() {
        editor.saveAction();
    }

    @FXML
    @Override
    public void refreshAction() {
        reset();
        loadImage();
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