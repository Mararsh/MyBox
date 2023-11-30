package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import mara.mybox.data.FileNode;
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

    @FXML
    protected TextField currentInput, newInput;

    public RemotePathRenameController() {
        baseTitle = message("FileRename");
    }

    public void setParameters(RemotePathManageController manageController) {
        try {
            this.manageController = manageController;

            TreeItem<FileNode> item = manageController.filesTreeView.getSelectionModel().getSelectedItem();
            if (item != null && item.getValue() != null) {
                String filename = item.getValue().nodeFullName();
                currentInput.setText(filename);
                currentInput.selectEnd();
                newInput.setText(filename);
                newInput.selectEnd();
            }
            newInput.requestFocus();
        } catch (Exception e) {
            MyBoxLog.error(e);
            popError(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            String currentName = currentInput.getText();
            if (currentName == null || currentName.isBlank()) {
                popError(message("InvalidParameter") + ": " + message("OriginalFileName"));
                return;
            }
            String newName = newInput.getText();
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
    public static RemotePathRenameController open(RemotePathManageController manageController) {
        try {
            if (manageController == null) {
                return null;
            }
            RemotePathRenameController controller = (RemotePathRenameController) WindowTools.openChildStage(
                    manageController.getMyWindow(), Fxmls.RemotePathRenameFxml, false);
            controller.setParameters(manageController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
