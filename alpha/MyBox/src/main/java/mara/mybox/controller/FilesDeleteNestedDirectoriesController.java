package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
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
            String result;
            File file = currentSourceFile();
            if (!file.exists()) {
                result = Languages.message("NotFound");
            } else if (file.isDirectory()) {
                FileDeleteTools.deleteNestedDir(currentTask, file);
                if (file.exists()) {
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
            tableController.markFileHandled(currentParameters.currentSourceFile, result);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void afterTask(boolean ok) {
        super.afterTask(ok);
        popInformation(Languages.message("CompleteFile"));
    }

}
