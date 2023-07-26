package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import thridparty.jankovicsandras.ImageTracer;

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
            BufferedImage image = ImageFileReaders.readImage(sourceFile);
            if (image == null) {
                popError(message("InvalidData") + ": " + sourceFile);
                return;
            }
            String svg = ImageTracer.imageToSVG(image, optionsController.options, null);
            File svgFile = FileTmpTools.generateFile("svg");
            ImageTracer.saveString(svgFile.getAbsolutePath(), svg);
            if (svgFile.exists()) {
                SvgEditorController.open(svgFile);
                close();
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
