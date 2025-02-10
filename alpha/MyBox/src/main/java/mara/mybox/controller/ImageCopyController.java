package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.ImageClipboardTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-10-28
 * @License Apache License Version 2.0
 */
public class ImageCopyController extends ImageSelectPixelsController {

    @FXML
    protected ToggleGroup targetGroup;
    @FXML
    protected RadioButton systemRadio, myboxRadio;
    @FXML
    protected CheckBox openClipboardCheck, marginsCheck;

    public ImageCopyController() {
        baseTitle = message("Copy");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Copy");

            String target = UserConfig.getString(baseName + "TargetType", "System");
            if ("MyBox".equals(target)) {
                myboxRadio.setSelected(true);
            } else {
                systemRadio.setSelected(true);
            }
            targetGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (myboxRadio.isSelected()) {
                        UserConfig.setString(baseName + "TargetType", "MyBox");
                    } else {
                        UserConfig.setString(baseName + "TargetType", "System");
                    }
                }
            });

            openClipboardCheck.setSelected(UserConfig.getBoolean(baseName + "OpenClipboard", true));
            openClipboardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "OpenClipboard", openClipboardCheck.isSelected());
                }
            });

            marginsCheck.setSelected(UserConfig.getBoolean(baseName + "CutMargins", true));
            marginsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "CutMargins", marginsCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private Image scopedImage;

            @Override
            protected boolean handle() {
                try {
                    scopedImage = scopeHandler.selectedScope(this,
                            bgColorController.awtColor(),
                            marginsCheck.isSelected());
                    if (scopedImage == null || task == null || isCancelled()) {
                        return false;
                    }
                    if (myboxRadio.isSelected()) {
                        return ImageClipboard.add(this, scopedImage,
                                ImageClipboard.ImageSource.Copy) != null;
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
                if (systemRadio.isSelected()) {
                    ImageClipboardTools.copyToSystemClipboard(myController, scopedImage);
                    if (openClipboardCheck.isSelected()) {
                        ImageInSystemClipboardController.oneOpen();
                    }
                } else {
                    if (openClipboardCheck.isSelected()) {
                        ImageInMyBoxClipboardController.oneOpen();
                    }
                }
                if (closeAfterCheck.isSelected()) {
                    imageController.popSuccessful();
                    close();
                } else {
                    getMyWindow().requestFocus();
                    myStage.toFront();
                    popSuccessful();
                }
            }
        };
        start(task);
    }

    /*
        static methods
     */
    public static ImageCopyController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageCopyController controller = (ImageCopyController) WindowTools.branchStage(
                    parent, Fxmls.ImageCopyFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
