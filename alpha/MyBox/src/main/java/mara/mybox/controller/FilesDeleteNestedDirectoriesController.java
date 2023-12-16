package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.SoundTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-2-8
 * @License Apache License Version 2.0
 */
public class FilesDeleteNestedDirectoriesController extends BaseBatchFileController {

    protected int totalDeleted;

    @FXML
    protected RadioButton trashRadio;

    public FilesDeleteNestedDirectoriesController() {
        baseTitle = Languages.message("DeleteNestedDirectories");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            operationBarController.deleteOpenControls();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public boolean makeMoreParameters() {
        totalDeleted = 0;
        return super.makeMoreParameters();
    }

    public boolean countDirectories() {
        return false;
    }

    @Override
    public void handleCurrentFile(FxTask currentTask) {
        try {
            tableController.markFileHandling(currentParameters.currentIndex);
            currentParameters.currentSourceFile = getCurrentFile();
            countHandling(currentParameters.currentSourceFile);
            String result;
            if (!currentParameters.currentSourceFile.exists()) {
                result = Languages.message("NotFound");
            } else if (currentParameters.currentSourceFile.isDirectory()) {
                FileDeleteTools.deleteNestedDir(currentTask, currentParameters.currentSourceFile);
                if (currentParameters.currentSourceFile.exists()) {
                    result = Languages.message("Failed");
                } else {
                    result = Languages.message("DeletedSuccessfully");
                }
            } else {
                result = Languages.message("Skip");
            }
            if (verboseCheck == null || verboseCheck.isSelected()) {
                updateLogs(result);
            }
            totalItemsHandled++;
            tableController.markFileHandled(currentParameters.currentIndex, result);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void afterTask() {
        tableView.refresh();

        if (miaoCheck != null && miaoCheck.isSelected()) {
            SoundTools.miao3();
        }
        popInformation(Languages.message("CompleteFile"));
    }

}
