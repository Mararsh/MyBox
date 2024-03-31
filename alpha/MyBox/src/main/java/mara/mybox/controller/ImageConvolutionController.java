package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageConvolutionController extends BasePixelsController {

    protected ConvolutionKernel kernel;

    @FXML
    protected ControlImageConvolution convolutionController;

    public ImageConvolutionController() {
        baseTitle = message("Convolution");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Convolution");

            convolutionController.doubleClickedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    okAction();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        kernel = convolutionController.pickValues();
        return kernel != null;
    }

    @Override
    protected Image handleImage(FxTask currentTask, Image inImage, ImageScope inScope) {
        try {
            ImageConvolution convolution = ImageConvolution.create();
            convolution.setImage(inImage)
                    .setScope(inScope)
                    .setKernel(kernel)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent())
                    .setTask(currentTask);
            opInfo = kernel.getName();
            return convolution.startFx();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    /*
        static methods
     */
    public static ImageConvolutionController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageConvolutionController controller = (ImageConvolutionController) WindowTools.branchStage(
                    parent, Fxmls.ImageConvolutionFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static void updateList() {
        Platform.runLater(() -> {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                if (!(window instanceof Stage)) {
                    continue;
                }
                Stage stage = (Stage) window;
                Object controller = stage.getUserData();
                if (controller == null) {
                    continue;
                }
                if (controller instanceof ImageConvolutionController) {
                    try {
                        ((ImageConvolutionController) controller).refreshAction();
                    } catch (Exception e) {
                    }
                }
            }
        });
    }

}
