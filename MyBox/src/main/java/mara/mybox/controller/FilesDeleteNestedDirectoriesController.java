package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-2-8
 * @License Apache License Version 2.0
 */
public class FilesDeleteNestedDirectoriesController extends FilesBatchController {

    protected int totalDeleted;

    @FXML
    protected RadioButton trashRadio;

    public FilesDeleteNestedDirectoriesController() {
        baseTitle = AppVariables.message("DeleteNestedDirectories");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            tableController.countDirCheck.setSelected(false);
            tableController.countDirCheck.setVisible(false);
            operationBarController.deleteOpenControls();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public boolean makeMoreParameters() {
        totalDeleted = 0;
        return super.makeMoreParameters();
    }

    @Override
    public void handleCurrentFile() {
        try {
            tableController.markFileHandling(currentParameters.currentIndex);
            currentParameters.currentSourceFile = getCurrentFile();
            countHandling(currentParameters.currentSourceFile);
            String result;
            if (!currentParameters.currentSourceFile.exists()) {
                result = AppVariables.message("NotFound");
            } else if (currentParameters.currentSourceFile.isDirectory()) {
                FileTools.deleteNestedDir(currentParameters.currentSourceFile);
                if (currentParameters.currentSourceFile.exists()) {
                    result = message("Failed");
                } else {
                    result = message("DeletedSuccessfully");
                }
            } else {
                result = AppVariables.message("Skip");
            }
            if (verboseCheck == null || verboseCheck.isSelected()) {
                updateLogs(result);
            }
            totalItemsHandled++;
            tableController.markFileHandled(currentParameters.currentIndex, result);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void donePost() {
        tableView.refresh();

        if (miaoCheck != null && miaoCheck.isSelected()) {
            FxmlControl.miao3();
        }
        popInformation(AppVariables.message("CompleteFile"));
    }

}
