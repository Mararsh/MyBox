package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-9-18
 * @License Apache License Version 2.0
 */
public class ImagePopController extends ImageViewerController {

    @FXML
    protected CheckBox openCheck;

    public ImagePopController() {
        baseTitle = Languages.message("ImageViewer");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            openCheck.setSelected(UserConfig.getBoolean(baseName + "Open", true));
            openCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Open", newValue);
                    checkSaveAsType();
                }
            });
            checkSaveAsType();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkSaveAsType() {
        if (openCheck.isSelected()) {
            saveAsType = SaveAsType.Open;
        } else {
            saveAsType = SaveAsType.None;
        }
    }

    @Override
    public void setStageStatus(String prefix, int minSize) {
        setAsPopup(baseName);
    }

    /*
        static methods
     */
    public static ImagePopController open(BaseController parent, Image image) {
        try {
            if (image == null) {
                return null;
            }
            ImagePopController controller = (ImagePopController) WindowTools.openChildStage(parent.getMyWindow(), Fxmls.ImagePopFxml, false);
            controller.loadImage(image);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
