package mara.mybox.controller;

import java.awt.Desktop;
import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-7-5
 * @License Apache License Version 2.0
 */
public class FilesDeleteController extends FilesBatchController {

    @FXML
    protected RadioButton deleteRadio;
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
    public String handleFile(File srcFile, File targetPath) {
        try {
            if (srcFile == null || !srcFile.isFile()) {
                return AppVariables.message("Skip");
            }
            countHandling(srcFile);
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
                if (verboseCheck == null || verboseCheck.isSelected()) {
                    updateLogs(msg);
                }
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
    protected boolean handleDirectory(File sourcePath, File targetPath) {
        if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()
                || (isPreview && dirFilesHandled > 0)) {
            return false;
        }
        try {
            File[] files = sourcePath.listFiles();
            if (files == null || files.length == 0) {
                deleteEmptyDirectory(sourcePath);
            } else {
                super.handleDirectory(sourcePath, targetPath);
                deleteEmptyDirectory(sourcePath);
            }
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    protected void deleteEmptyDirectory(File sourcePath) {
        if (!deleteEmptyCheck.isSelected()
                || sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()) {
            return;
        }
        try {
            File[] files = sourcePath.listFiles();
            if (files == null || files.length == 0) {
                if (deleteRadio.isSelected()) {
                    if (sourcePath.delete()) {
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(message("DirectoryDeletedSuccessfully") + ": " + sourcePath.getAbsolutePath());
                        }
                    }
                } else {
                    if (Desktop.getDesktop().moveToTrash(sourcePath)) {
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(message("DirectoryMoveToTrashSuccessfully") + ": " + sourcePath.getAbsolutePath());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
