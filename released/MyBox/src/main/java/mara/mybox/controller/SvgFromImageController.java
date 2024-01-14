package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.Charset;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.SvgTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-6-29
 * @License Apache License Version 2.0
 */
public class SvgFromImageController extends BaseChildController {

    protected BufferedImage bufferedImage;

    @FXML
    protected ControlSvgFromImage optionsController;

    public SvgFromImageController() {
        baseTitle = message("ImageToSvg");
    }

    public void setParameters(Image image) {
        if (image == null) {
            close();
            return;
        }
        bufferedImage = SwingFXUtils.fromFXImage(image, null);
    }

    @FXML
    @Override
    public void startAction() {
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
                    String svg = SvgTools.imageToSvg(this, myController,
                            bufferedImage, optionsController);
                    if (svg == null || svg.isBlank() || !isWorking()) {
                        return false;
                    }
                    svgFile = FileTmpTools.generateFile(optionsController.getQuantization().name(), "svg");
                    svgFile = TextFileTools.writeFile(svgFile, svg, Charset.forName("utf-8"));
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
    public static SvgFromImageController open(Image image) {
        SvgFromImageController controller = (SvgFromImageController) WindowTools.openStage(Fxmls.SvgFromImageFxml);
        if (controller != null) {
            controller.setParameters(image);
            controller.requestMouse();
        }
        return controller;
    }

}
