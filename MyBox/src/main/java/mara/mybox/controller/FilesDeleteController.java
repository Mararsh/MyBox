package mara.mybox.controller;

import java.awt.Desktop;
import java.io.File;
import java.util.Date;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-7-5
 * @License Apache License Version 2.0
 */
public class FilesDeleteController extends FilesBatchController {

    protected int totalDeleted = 0;

    @FXML
    protected ToggleGroup deleteType;
    @FXML
    protected RadioButton deleteRadio, trashRadio;
    @FXML
    protected CheckBox deleteEmptyCheck;

    public FilesDeleteController() {
        baseTitle = AppVariables.message("FilesDelete");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            operationBarController.deleteOpenControls();

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public void initTargetSection() {
        try {
            super.initTargetSection();

            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableView.getItems())
            );
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public boolean makeBatchParameters() {
        totalDeleted = 0;
        return super.makeBatchParameters();
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            if (srcFile == null || !srcFile.isFile()) {
                return AppVariables.message("Skip");
            }
            boolean ok;
            String msg;
            if (deleteRadio.isSelected()) {
                ok = srcFile.delete();
                msg = message("FileDeletedSuccessfully") + ": " + srcFile.getAbsolutePath();
            } else {
                ok = Desktop.getDesktop().moveToTrash(srcFile);
                msg = message("FileMoveToTrashSuccessfully") + ": " + srcFile.getAbsolutePath();
            }
            if (ok) {
                totalDeleted++;
                updateLogs(msg);
                return AppVariables.message("Successful");
            } else {
                return AppVariables.message("Failed");
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return AppVariables.message("Failed");
        }
    }

    @Override
    protected void handleDirectory(File sourcePath, File targetPath) {
        if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()
                || (isPreview && dirFilesHandled > 0)) {
            return;
        }
        try {
            File[] files = sourcePath.listFiles();
            if (files.length == 0) {
                deleteEmptyDirectory(sourcePath);
            } else {
                super.handleDirectory(sourcePath, targetPath);
                deleteEmptyDirectory(sourcePath);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void deleteEmptyDirectory(File sourcePath) {
        if (!deleteEmptyCheck.isSelected()
                || sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()) {
            return;
        }
        try {
            File[] files = sourcePath.listFiles();
            if (files.length == 0) {
                if (deleteRadio.isSelected()) {
                    if (sourcePath.delete()) {
                        updateLogs(message("DirectoryDeletedSuccessfully") + ": " + sourcePath.getAbsolutePath());
                    }
                } else {
                    if (Desktop.getDesktop().moveToTrash(sourcePath)) {
                        updateLogs(message("DirectoryMoveToTrashSuccessfully") + ": " + sourcePath.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void donePost() {
        super.donePost();

        updateLogs(message("StartTime") + ": " + DateTools.datetimeToString(startTime) + "   "
                + AppVariables.message("Cost") + ": " + DateTools.showTime(new Date().getTime() - startTime.getTime()), false, true);
        updateLogs(message("TotalDeletedFiles") + ": " + totalDeleted);

    }

}
