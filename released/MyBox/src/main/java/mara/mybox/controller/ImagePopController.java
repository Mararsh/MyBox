package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    @FXML
    protected CheckBox sychronizedCheck;
    @FXML
    protected Button refreshButton;

    @Override
    public void setStageStatus() {
    }

    public void setControls() {
        try {
            saveAsType = SaveAsType.Open;

            listener = new ChangeListener<Image>() {
                @Override
                public void changed(ObservableValue ov, Image oldv, Image newv) {
                    if (sychronizedCheck.isVisible() && sychronizedCheck.isSelected()) {
                        refreshAction();
                    }
                }
            };

            sychronizedCheck.setSelected(UserConfig.getBoolean(baseName + "Sychronized", true));
            checkSychronize();
            sychronizedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldState, Boolean newState) {
                    checkSychronize();
                }
            });

            setAsPopup(baseName);
            paneSize();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkSychronize() {
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

    public void setSourceImageView(String baseName, ImageView sourceImageView) {
        try {
            this.baseName = baseName;

            this.sourceImageView = sourceImageView;
            refreshAction();

            setControls();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setImage(String baseName, Image image) {
        try {
            this.baseName = baseName;

            sychronizedCheck.setVisible(false);
            refreshButton.setVisible(false);

            loadImage(image);

            setControls();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void refreshAction() {
        if (sourceImageView != null) {
            loadImage(sourceImageView.getImage());

        } else {
            sychronizedCheck.setVisible(false);
            refreshButton.setVisible(false);

        }
    }

    @Override
    public void cleanPane() {
        try {
            if (sourceImageView != null) {
                sourceImageView.imageProperty().removeListener(listener);
                sourceImageView = null;
            }
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
            controller.setImage(parent.baseName, image);
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
            controller.setSourceImageView(parent.baseName, imageView);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
