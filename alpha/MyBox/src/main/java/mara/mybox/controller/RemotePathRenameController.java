package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-3-23
 * @License Apache License Version 2.0
 */
public class RemotePathRenameController extends BaseController {

    protected RemotePathManageController manageController;
    protected String currentName, newName;

    @FXML
    protected TextField currentInput, newInput;

    public RemotePathRenameController() {
        baseTitle = message("FileRename");
    }

    public void setParameters(RemotePathManageController manageController, String filename) {
        try {
            this.manageController = manageController;
            this.currentName = filename;

            currentInput.setText(filename);
            newInput.setText(filename);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            currentName = currentInput.getText();
            if (currentName == null || currentName.isBlank()) {
                popError(message("InvalidParameter") + ": " + message("OriginalFileName"));
                return;
            }
            newName = newInput.getText();
            if (newName == null || newName.isBlank()) {
                popError(message("InvalidParameter") + ": " + message("NewFileName"));
                return;
            }
            if (currentName.equals(newName)) {
                popError(message("Unchanged"));
                return;
            }
            manageController.renameFile(currentName, newName);
            close();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static methods
     */
    public static RemotePathRenameController open(RemotePathManageController manageController, String filename) {
        try {
            if (manageController == null) {
                return null;
            }
            RemotePathRenameController controller = (RemotePathRenameController) WindowTools.openChildStage(
                    manageController.getMyWindow(), Fxmls.RemotePathRenameFxml, false);
            controller.setParameters(manageController, filename);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
