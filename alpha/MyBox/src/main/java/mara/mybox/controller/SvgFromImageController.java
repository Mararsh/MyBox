package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.SvgTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-6-29
 * @License Apache License Version 2.0
 */
public class SvgFromImageController extends BaseChildController {

    @FXML
    protected ControlSvgFromImage optionsController;

    public SvgFromImageController() {
        baseTitle = message("ImageToSvg");
    }

    public void setParameters(File file) {
        sourceFile = file;
    }

    @FXML
    @Override
    public void okAction() {
        try {
            if (!optionsController.pickValues()) {
                return;
            }
            File svgFile = SvgTools.imageToSvgFile(this, sourceFile,
                    optionsController.myboxRadio.isSelected() ? optionsController.quantizationController : null,
                    optionsController.options);
            if (svgFile != null && svgFile.exists()) {
                SvgEditorController.open(svgFile);
                if (closeAfterCheck.isSelected()) {
                    close();
                }
            } else {
                popError(message("Failed"));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static methods
     */
    public static SvgFromImageController open(File file) {
        SvgFromImageController controller = (SvgFromImageController) WindowTools.openStage(Fxmls.SvgFromImageFxml);
        if (controller != null) {
            controller.setParameters(file);
            controller.requestMouse();
        }
        return controller;
    }

}
