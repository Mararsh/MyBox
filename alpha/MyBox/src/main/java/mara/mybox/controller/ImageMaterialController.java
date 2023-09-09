package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-8-27
 * @License Apache License Version 2.0
 */
public class ImageMaterialController extends TreeManageController {

    @FXML
    protected ControlImageMaterialEditor editorController;

    public ImageMaterialController() {
        baseTitle = message("ImageMaterial");
        TipsLabelKey = "ImageMaterialTips";
        category = InfoNode.ImageMaterial;
        nameMsg = message("Title");
        valueMsg = message("ImageMaterial");
        moreMsg = message("More");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
//            nodeController = editorController.attributesController;

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            editorController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void itemClicked() {
    }

    /*
        static
     */
    public static ImageMaterialController open() {
        try {
            ImageMaterialController controller = (ImageMaterialController) WindowTools.openStage(Fxmls.ImageMaterialFxml);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
