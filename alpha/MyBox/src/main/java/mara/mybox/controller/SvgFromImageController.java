package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import mara.mybox.fxml.FxSingletonTask;
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
        if (!optionsController.pickValues()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private File svgFile;

            @Override
            protected boolean handle() {
                try {
                    svgFile = SvgTools.imageToSvgFile(this, myController, sourceFile,
                            optionsController.myboxRadio.isSelected()
                            ? optionsController.quantizationController : null,
                            optionsController.options);
                    return svgFile != null && svgFile.exists();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                SvgEditorController.open(svgFile);
                if (closeAfterCheck.isSelected()) {
                    close();
                }
            }

        };
        start(task);
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
