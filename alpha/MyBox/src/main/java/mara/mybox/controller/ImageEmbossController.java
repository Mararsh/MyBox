package mara.mybox.controller;

import java.awt.image.BufferedImage;
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
import mara.mybox.fxml.SingletonCurrentTask;
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
public class ImageEmbossController extends ImageSelectScopeController {

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
            private ImageScope scope;
            protected ConvolutionKernel kernel;

            @Override
            protected boolean handle() {
                try {
                    scope = scope();
                    kernel = ConvolutionKernel.makeEmbossKernel(
                            direction, raduis, greyCheck.isSelected());
                    ImageConvolution convolution = ImageConvolution.create();
                    convolution.setImage(editor.imageView.getImage())
                            .setScope(scope)
                            .setKernel(kernel)
                            .setExcludeScope(excludeRadio.isSelected())
                            .setSkipTransparent(ignoreTransparentCheck.isSelected());
                    handledImage = convolution.operateFxImage();
                    return handledImage != null;
                } catch (Exception e) {
                    MyBoxLog.debug(e);
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                editor.updateImage(kernel.getName(), message("Grey") + ": " + kernel.isGray(),
                        scope, handledImage, cost);
                if (closeAfterCheck.isSelected()) {
                    close();
                }
            }
        };
        start(task);
    }

    @Override
    protected Image makeDemo(BufferedImage dbf, ImageScope scope) {
        try {
            ConvolutionKernel kernel = ConvolutionKernel.makeEmbossKernel(
                    BufferedImageTools.Direction.Top, 3, true);
            ImageConvolution convolution = ImageConvolution.create()
                    .setImage(dbf)
                    .setScope(scope)
                    .setKernel(kernel);
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
