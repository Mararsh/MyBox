package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
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
public class ImageSelectScopeController extends BaseChildController {

    protected BaseImageController imageController;
    protected ImageEditorController editor;

    @FXML
    protected ControlImageScopeInput scopeController;
    @FXML
    protected ControlColorSet bgColorController;
    @FXML
    protected ToggleGroup selectGroup;
    @FXML
    protected RadioButton includeRadio, excludeRadio, wholeRadio;

    public ImageSelectScopeController() {
        baseTitle = message("SelectScope");
        TipsLabelKey = "ImageScopeTips";
    }

    protected void setParameters(BaseImageController parent) {
        try {
            if (parent == null) {
                close();
            }
            this.imageController = parent;
            if (imageController instanceof ImageEditorController) {
                editor = (ImageEditorController) imageController;
            }
            setControls();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void setControls() {
        try {
            scopeController.setParameters(imageController);
            bgColorController.init(this, baseName + "BackgroundColor", Color.DARKGREEN);

            setSelectControls();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void setSelectControls() {
        try {
            if (includeRadio == null) {
                return;
            }
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
                    } else if (excludeRadio.isSelected()) {
                        UserConfig.setString(baseName + "SelectType", "Exclude");
                    } else {
                        UserConfig.setString(baseName + "SelectType", "Whole");
                    }
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            return scopeController.keyEventsFilter(event);
        } else {
            return true;
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private Image scopedImage;

            @Override
            protected boolean handle() {
                try {
                    scopedImage = scopeController.scopedImage(
                            bgColorController.color(),
                            true,
                            excludeRadio.isSelected());
                    return scopedImage != null;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                ImageViewerController.openImage(scopedImage);
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
    public static ImageSelectScopeController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageSelectScopeController controller = (ImageSelectScopeController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageSelectScopeFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
