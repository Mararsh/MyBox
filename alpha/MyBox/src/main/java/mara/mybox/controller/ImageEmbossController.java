package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.BufferedImageTools;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageEmbossController extends BasePixelsController {

    protected int direction, raduis;

    @FXML
    protected RadioButton topRadio, bottomRadio, leftRadio, rightRadio,
            leftTopRadio, rightBottomRadio, leftBottomRadio, rightTopRadio;
    @FXML
    protected ComboBox<String> raduisSelector;
    @FXML
    protected CheckBox greyCheck;

    public ImageEmbossController() {
        baseTitle = message("Emboss");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            if (editor == null) {
                close();
                return;
            }
            topRadio.setSelected(true);

            raduis = UserConfig.getInt(baseName + "Raduis", 3);
            if (raduis <= 0) {
                raduis = 3;
            }
            raduisSelector.getItems().addAll(Arrays.asList("3", "5"));
            raduisSelector.getSelectionModel().select(raduis + "");
            raduisSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            raduis = v;
                            UserConfig.setInt(baseName + "Raduis", raduis);
                            ValidationTools.setEditorNormal(raduisSelector);
                        } else {
                            ValidationTools.setEditorBadStyle(raduisSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(raduisSelector);
                    }
                }
            });

            greyCheck.setSelected(UserConfig.getBoolean(baseName + "Grey", true));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        UserConfig.setBoolean(baseName + "Grey", greyCheck.isSelected());

        if (raduis <= 0) {
            popError(message("InvalidParameter") + ": " + message("Radius"));
            return false;
        }

        if (topRadio.isSelected()) {
            direction = BufferedImageTools.Direction.Top;
        } else if (bottomRadio.isSelected()) {
            direction = BufferedImageTools.Direction.Bottom;
        } else if (leftRadio.isSelected()) {
            direction = BufferedImageTools.Direction.Left;
        } else if (rightRadio.isSelected()) {
            direction = BufferedImageTools.Direction.Right;
        } else if (leftTopRadio.isSelected()) {
            direction = BufferedImageTools.Direction.LeftTop;
        } else if (rightBottomRadio.isSelected()) {
            direction = BufferedImageTools.Direction.RightBottom;
        } else if (leftBottomRadio.isSelected()) {
            direction = BufferedImageTools.Direction.LeftBottom;
        } else if (rightTopRadio.isSelected()) {
            direction = BufferedImageTools.Direction.RightTop;
        } else {
            direction = BufferedImageTools.Direction.Top;
        }

        return true;
    }

    @Override
    protected Image handleImage(Image inImage, ImageScope inScope) {
        try {
            ConvolutionKernel kernel = ConvolutionKernel.makeEmbossKernel(
                    direction, raduis, greyCheck.isSelected());
            ImageConvolution convolution = ImageConvolution.create();
            convolution.setImage(inImage)
                    .setScope(inScope)
                    .setKernel(kernel)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent());
            operation = kernel.getName();
            opInfo = message("Grey") + ": " + kernel.isGray();
            return convolution.operateFxImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    /*
        static methods
     */
    public static ImageEmbossController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageEmbossController controller = (ImageEmbossController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageEmbossFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
