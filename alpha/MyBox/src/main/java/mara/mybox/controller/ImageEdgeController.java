package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageEdgeController extends ImageSelectScopeController {

    protected int threshold, small, big;

    @FXML
    protected RadioButton eightLaplaceRadio, eightLaplaceExcludedRadio,
            fourLaplaceRadio, fourLaplaceExcludedRadio;
    @FXML
    protected CheckBox greyCheck;

    public ImageEdgeController() {
        baseTitle = message("EdgeDetection");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            if (editor == null) {
                close();
                return;
            }
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
                    if (eightLaplaceRadio.isSelected()) {
                        kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplace();
                    } else if (eightLaplaceExcludedRadio.isSelected()) {
                        kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplaceInvert();
                    } else if (fourLaplaceRadio.isSelected()) {
                        kernel = ConvolutionKernel.makeEdgeDetectionFourNeighborLaplace();
                    } else if (fourLaplaceExcludedRadio.isSelected()) {
                        kernel = ConvolutionKernel.makeEdgeDetectionFourNeighborLaplaceInvert();
                    } else {
                        return false;
                    }
                    kernel.setGray(greyCheck.isSelected());
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
            ConvolutionKernel kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplace();
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
    public static ImageEdgeController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageEdgeController controller = (ImageEdgeController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageEdgeFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
