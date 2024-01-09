package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-7-19
 * @License Apache License Version 2.0
 */
public class MarkdownOptionsController extends BaseChildController {

    protected MarkdownEditorController fileController;

    @FXML
    protected ControlMarkdownOptions optionsController;

    public MarkdownOptionsController() {
        baseTitle = message("MarkdownOptions");
    }

    public void setParameters(MarkdownEditorController parent) {
        fileController = parent;
    }

    @FXML
    @Override
    public void okAction() {
        optionsController.pickValues();
        if (fileController != null) {
            fileController.htmlOptions = optionsController.options();
            fileController.updateHtmlConverter();
        } else {
            popSuccessful();
        }
        if (closeAfterCheck.isSelected()) {
            close();
        }
    }

    /*
        static methods
     */
    public static MarkdownOptionsController open() {
        try {
            MarkdownOptionsController controller
                    = (MarkdownOptionsController) WindowTools.openStage(Fxmls.MarkdownOptionsFxml);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static MarkdownOptionsController open(MarkdownEditorController parent) {
        try {
            if (parent == null) {
                return open();
            }
            MarkdownOptionsController controller
                    = (MarkdownOptionsController) WindowTools.branchStage(parent, Fxmls.MarkdownOptionsFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
