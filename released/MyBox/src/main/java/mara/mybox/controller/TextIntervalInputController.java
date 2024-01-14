package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-23
 * @License Apache License Version 2.0
 */
public class TextIntervalInputController extends BaseChildController {

    protected BaseTextController fileController;

    @FXML
    protected ControlTimeLength setController;

    public boolean setParameter(BaseTextController parent) {
        if (parent == null) {
            close();
            return false;
        }
        fileController = parent;
        baseName = fileController.baseName;
        setTitle(message("AutoSave") + " - " + fileController.getTitle());
        setController.init(interfaceName + "AutoSave", fileController.autoCheckInterval);
        setController.select(fileController.autoCheckInterval);
        return true;
    }

    @FXML
    @Override
    public void okAction() {
        long v = setController.pickValue();
        if (v <= 0) {
            return;
        }
        UserConfig.setLong(baseName + "AutoCheckInterval", v);
        fileController.autoCheckInterval = v;

        close();
        fileController.popInformation(message("Saved") + ": " + fileController.autoCheckInterval);
        fileController.checkAutoSave();
    }

    /*
        static methods
     */
    public static TextIntervalInputController open(BaseTextController parent) {
        try {
            if (parent == null) {
                return null;
            }
            TextIntervalInputController controller
                    = (TextIntervalInputController) WindowTools.branchStage(parent, Fxmls.TextIntervalInputFxml);
            controller.setParameter(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
