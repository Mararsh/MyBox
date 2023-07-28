package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.SvgTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-6-29
 * @License Apache License Version 2.0
 */
public class SvgFromImageController extends BaseChildController {

    @FXML
    protected ControlSvgFromImage optionsController;
    @FXML
    protected CheckBox closeAfterCheck;

    public SvgFromImageController() {
        baseTitle = message("ImageToSvg");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            closeAfterCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(interfaceName + "SaveClose", closeAfterCheck.isSelected());
                }
            });
            closeAfterCheck.setSelected(UserConfig.getBoolean(interfaceName + "SaveClose", false));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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
