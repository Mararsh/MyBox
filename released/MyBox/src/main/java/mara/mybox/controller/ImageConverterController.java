package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.ImageConvertTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-6-29
 * @License Apache License Version 2.0
 */
public class ImageConverterController extends BaseChildController {

    @FXML
    protected ControlImageFormat formatController;

    public ImageConverterController() {
        baseTitle = message("ImageConverter");
    }

    public void setParameters(File file) {
        try {
            sourceFile = file;

            formatController.setParameters(this, false);

            okButton.disableProperty().bind(
                    formatController.qualitySelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle())
                            .or(formatController.profileInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(formatController.binaryController.thresholdInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(formatController.icoWidthSelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            File target = FileTmpTools.generateFile(formatController.attributes.getImageFormat());

            if (!ImageConvertTools.convertColorSpace(sourceFile, formatController.attributes, target)
                    || target == null || !target.exists()) {
                popError(message("Failed"));
                return;
            }
            ImageViewerController.openFile(target);
            if (closeAfterCheck.isSelected()) {
                close();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static methods
     */
    public static ImageConverterController open(File file) {
        ImageConverterController controller = (ImageConverterController) WindowTools.openStage(Fxmls.ImageConverterFxml);
        if (controller != null) {
            controller.setParameters(file);
            controller.requestMouse();
        }
        return controller;
    }

}
