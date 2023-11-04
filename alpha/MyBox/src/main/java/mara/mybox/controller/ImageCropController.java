package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-10-28
 * @License Apache License Version 2.0
 */
public class ImageCropController extends ImageSelectScopeController {

    @FXML
    protected ToggleGroup targetGroup;
    @FXML
    protected CheckBox clipMarginsCheck, imageMarginsCheck,
            copyClipboardCheck, openClipboardCheck;

    public ImageCropController() {
        baseTitle = message("Crop");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            if (editor == null) {
                close();
                return;
            }

            clipMarginsCheck.setSelected(UserConfig.getBoolean(baseName + "ClipCutMargins", true));
            clipMarginsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "ClipCutMargins", clipMarginsCheck.isSelected());
                }
            });

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

            openClipboardCheck.setSelected(UserConfig.getBoolean(baseName + "OpenClipboard", true));
            openClipboardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "OpenClipboard", openClipboardCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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
        task = new SingletonCurrentTask<Void>(this) {

            private Image cuttedClip;

            @Override
            protected boolean handle() {
                try {
                    handledImage = scopeController.scopedImage(
                            bgColorController.color(),
                            imageMarginsCheck.isSelected(),
                            includeRadio.isSelected());
                    if (handledImage == null || task == null || isCancelled()) {
                        return false;
                    }
                    if (copyClipboardCheck.isSelected()) {
                        cuttedClip = scopeController.scopedImage(
                                bgColorController.color(),
                                clipMarginsCheck.isSelected(),
                                excludeRadio.isSelected());
                        return ImageClipboard.add(cuttedClip,
                                ImageClipboard.ImageSource.Crop) != null;
                    } else {
                        return true;
                    }
                } catch (Exception e) {
                    MyBoxLog.debug(e);
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                editor.updateImage("Crop", null, scopeController.scope, handledImage, cost);
                if (openClipboardCheck.isSelected() && cuttedClip != null) {
                    ImageInMyBoxClipboardController.oneOpen();
                }
                if (closeAfterCheck.isSelected()) {
                    close();
                }
            }
        };
        start(task);
    }

    /*
        static methods
     */
    public static ImageCropController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageCropController controller = (ImageCropController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageCropFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
