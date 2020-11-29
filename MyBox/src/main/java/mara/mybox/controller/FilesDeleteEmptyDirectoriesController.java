package mara.mybox.controller;

import java.text.MessageFormat;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-1-6
 * @License Apache License Version 2.0
 */
public class FilesDeleteEmptyDirectoriesController extends FilesBatchController {

    protected int totalDeleted;

    @FXML
    protected RadioButton trashRadio;

    public FilesDeleteEmptyDirectoriesController() {
        baseTitle = AppVariables.message("DeleteEmptyDirectories");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
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
                int count = FileTools.deleteEmptyDir(currentParameters.currentSourceFile, trashRadio.isSelected());
                result = MessageFormat.format(message("DeleteEmptyDirectoriesCount"), count);
                totalDeleted += count;
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
        updateLogs(MessageFormat.format(message("DeleteEmptyDirectoriesTotalCount"), totalDeleted));
        super.donePost();
    }

}
