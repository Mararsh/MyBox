package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-10-21
 * @License Apache License Version 2.0
 */
public class ImageScopeController extends InfoTreeManageController {

    @FXML
    protected ImageScopeEditor editorController;

    public ImageScopeController() {
        baseTitle = message("ImageScope");
        category = InfoNode.ImageScope;
        nameMsg = message("Title");
        valueMsg = message("Values");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            editor = editorController;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean keyF11() {
        if (editorController.scopeController.thisPane.isFocusWithin()
                || editorController.scopeController.thisPane.isFocusWithin()) {
            editorController.scopeController.keyF11();
            return true;

        }
        return super.keyF11();
    }


    /*
        static methods
     */
    public static ImageScopeController open() {
        ImageScopeController controller = (ImageScopeController) WindowTools.openStage(Fxmls.ImageScopeFxml);
        controller.requestMouse();
        return controller;
    }

    public static ImageScopeController load(ImageScope scope) {
        ImageScopeController controller = open();
        controller.editorController.loadScope(scope);
        return controller;
    }

}
