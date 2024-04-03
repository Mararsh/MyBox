package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.paint.Color;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-10-28
 * @License Apache License Version 2.0
 */
public class ImageSelectPixelsController extends BasePixelsController {

    @FXML
    protected ControlColorSet bgColorController;
    @FXML
    protected CheckBox marginsCheck;

    public ImageSelectPixelsController() {
        baseTitle = message("SelectPixels");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("SelectPixels");

            bgColorController.init(this, baseName + "BackgroundColor", Color.DARKGREEN);

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
        if (!checkOptions()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    handledImage = scopeController.selectedScope(this,
                            bgColorController.awtColor(), marginsCheck.isSelected());
                    return handledImage != null;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                ImagePopController.openImage(myController, handledImage);
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
    public static ImageSelectPixelsController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageSelectPixelsController controller = (ImageSelectPixelsController) WindowTools.branchStage(
                    parent, Fxmls.ImageSelectPixelsFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
