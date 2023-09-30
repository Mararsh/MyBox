package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2023-8-27
 * @License Apache License Version 2.0
 */
public class ControlImageMaterialEditor extends ControlSvgShape {

    protected ImageMaterialController materialsController;

    @FXML
    protected ControlInfoNodeAttributes attributesController;

    public ControlImageMaterialEditor() {
    }

    protected void setParameters(ImageMaterialController controller) {
        try {
            materialsController = controller;

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

}
