package mara.mybox.controller;

import java.text.MessageFormat;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileDeleteTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-1-6
 * @License Apache License Version 2.0
 */
public class FilesDeleteEmptyDirectoriesController extends BaseBatchFileController {

    protected int totalDeleted;

    @FXML
    protected RadioButton trashRadio;

    public FilesDeleteEmptyDirectoriesController() {
        baseTitle = Languages.message("DeleteEmptyDirectories");
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
                result = Languages.message("NotFound");
            } else if (currentParameters.currentSourceFile.isDirectory()) {
                int count = FileDeleteTools.deleteEmptyDir(currentParameters.currentSourceFile, trashRadio.isSelected());
                result = MessageFormat.format(Languages.message("DeleteEmptyDirectoriesCount"), count);
                totalDeleted += count;
            } else {
                result = Languages.message("Skip");
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
        updateLogs(MessageFormat.format(Languages.message("DeleteEmptyDirectoriesTotalCount"), totalDeleted));
        super.donePost();
    }

}
