package mara.mybox.controller;

import java.io.File;
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
public class ImagePopController extends BaseShapeController {

    protected BaseImageController sourceController;
    protected ImageView sourceImageView;
    protected ChangeListener sourceListener;

    @FXML
    protected CheckBox refreshChangeCheck;
    @FXML
    protected Button refreshButton;

    @Override
    public void setStageStatus() {
    }

    public void setControls() {
        try {
            if (parentController != null) {
                baseName = parentController.baseName + "_" + baseName;
            }

            saveAsType = SaveAsType.Open;

            sourceListener = new ChangeListener<Image>() {
                @Override
                public void changed(ObservableValue ov, Image oldv, Image newv) {
                    if (refreshChangeCheck.isVisible() && refreshChangeCheck.isSelected()) {
                        refreshAction();
                    }
                }
            };

            refreshChangeCheck.setSelected(UserConfig.getBoolean(baseName + "Sychronized", true));
            checkSychronize();
            refreshChangeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldState, Boolean newState) {
                    checkSychronize();
                }
            });

            fitSize();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void checkSychronize() {
        if (sourceImageView == null) {
            refreshChangeCheck.setVisible(false);
            refreshButton.setVisible(false);
            return;
        }
        if (refreshChangeCheck.isVisible() && refreshChangeCheck.isSelected()) {
            sourceImageView.imageProperty().addListener(sourceListener);
        } else {
            sourceImageView.imageProperty().removeListener(sourceListener);
        }
    }

    public void setSourceImageView(BaseController parent, ImageView sourceImageView) {
        try {
            this.parentController = parent;
            this.sourceImageView = sourceImageView;

            setControls();

            refreshAction();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setImage(BaseController parent, Image image) {
        try {
            this.parentController = parent;
            refreshChangeCheck.setVisible(false);
            refreshButton.setVisible(false);

            setControls();

            loadImage(image);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setFile(BaseController parent, String filename) {
        try {
            this.parentController = parent;
            refreshChangeCheck.setVisible(false);
            refreshButton.setVisible(false);

            setControls();

            sourceFileChanged(new File(filename));

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        if (sourceController != null) {
            loadImage(sourceController.sourceFile,
                    sourceController.imageInformation,
                    sourceController.image,
                    sourceController.imageChanged);

        } else if (sourceImageView != null) {
            loadImage(sourceImageView.getImage());

        } else {
            refreshChangeCheck.setVisible(false);
            refreshButton.setVisible(false);

        }
    }

    @Override
    public void cleanPane() {
        try {
            if (sourceImageView != null) {
                sourceImageView.imageProperty().removeListener(sourceListener);
            }
            sourceListener = null;
            sourceImageView = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }


    /*
        static methods
     */
    public static ImagePopController openImage(BaseController parent, Image image) {
        try {
            if (image == null) {
                return null;
            }
            ImagePopController controller = (ImagePopController) WindowTools.popStage(parent, Fxmls.ImagePopFxml);
            controller.setImage(parent, image);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static ImagePopController openView(BaseController parent, ImageView imageView) {
        try {
            if (parent == null || imageView == null) {
                return null;
            }
            ImagePopController controller = (ImagePopController) WindowTools.popStage(parent, Fxmls.ImagePopFxml);
            controller.setSourceImageView(parent, imageView);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static ImagePopController openFile(BaseController parent, String filename) {
        try {
            ImagePopController controller = (ImagePopController) WindowTools.popStage(parent, Fxmls.ImagePopFxml);
            controller.setFile(parent, filename);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
