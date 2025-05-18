package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyEvent;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-7-23
 * @License Apache License Version 2.0
 */
public class FindReplaceController extends BaseChildController {

    @FXML
    protected ControlFindReplace findController;

    public FindReplaceController() {
        baseTitle = message("FindReplace");
    }

    public boolean setParent(BaseController parent) {
        if (parent == null) {
            close();
            return false;
        }
        baseName = parent.baseName;
        setFileType(parent.TargetFileType);
        setTitle(baseTitle + " - " + parent.getTitle());
        return true;
    }

    public void setInput(BaseController parent, TextInputControl input) {
        try {
            if (!setParent(parent)) {
                return;
            }
            findController.setEditInput(parent, input);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setEditor(BaseTextController parent) {
        try {
            if (!setParent(parent)) {
                return;
            }
            findController.setEditor(parent);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void findAction() {
        findController.findAction();
    }

    @FXML
    @Override
    public void replaceAction() {
        findController.replaceAction();
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (super.keyEventsFilter(event)) {
            return true;
        }
        return findController.keyEventsFilter(event);
    }


    /*
        static methods
     */
    public static FindReplaceController forInput(BaseController parent, TextInputControl input) {
        try {
            if (parent == null || input == null) {
                return null;
            }
            FindReplaceController controller
                    = (FindReplaceController) WindowTools.referredTopStage(parent, Fxmls.FindReplaceFxml);
            controller.setInput(parent, input);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static FindReplaceController forEditor(BaseTextController parent) {
        try {
            if (parent == null) {
                return null;
            }
            FindReplaceController controller
                    = (FindReplaceController) WindowTools.referredTopStage(parent, Fxmls.FindReplaceFxml);
            controller.setEditor(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
