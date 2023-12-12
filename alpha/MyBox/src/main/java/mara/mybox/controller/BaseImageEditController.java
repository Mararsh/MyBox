package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
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

    protected ImageEditorController editor;
    protected String operation, opInfo;
    protected Image handledImage;
    protected boolean needFixSize;
    protected FxTask demoTask;

    @FXML
    protected CheckBox closeAfterCheck;

    protected void setParameters(ImageEditorController parent) {
        try {
            if (parent == null) {
                close();
                return;
            }
            editor = parent;
            needFixSize = true;

            editor.loadNotify.addListener(new ChangeListener<Boolean>() {
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

    @Override
    public void fitView() {
        if (needFixSize) {
            paneSize();
            needFixSize = false;
        }
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
                handleImage();
                return !isCancelled() && handledImage != null;
            }

            @Override
            protected void whenSucceeded() {
                if (isCancelled()) {
                    return;
                }
                if (isPreview) {
                    ImagePopController.openImage(myController, handledImage);
                } else {
                    editor.updateImage(operation, opInfo, null, handledImage);
                    if (closeAfterCheck.isSelected()) {
                        close();
                        editor.popSuccessful();
                    } else {
                        getMyWindow().requestFocus();
                        myStage.toFront();
                        popSuccessful();
                    }
                }
            }

        };
        start(task);
    }

    protected void handleImage() {
    }

    @FXML
    protected void demo() {
        if (!checkOptions()) {
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
                    makeDemoFiles(files, demoImage);
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
                            = (ImagesBrowserController) WindowTools.openStage(Fxmls.ImagesBrowserFxml);
                    b.loadFiles(files);
                    b.setAlwaysOnTop();
                }
            }

        };
        start(demoTask);
    }

    protected void makeDemoFiles(List<String> files, Image demoImage) {
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
