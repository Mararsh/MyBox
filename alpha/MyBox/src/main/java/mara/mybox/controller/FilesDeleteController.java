package mara.mybox.controller;

import java.awt.Desktop;
import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2019-7-5
 * @License Apache License Version 2.0
 */
public class FilesDeleteController extends BaseBatchFileController {

    @FXML
    protected RadioButton deleteRadio;
    @FXML
    protected CheckBox deleteEmptyCheck;

    public FilesDeleteController() {
        baseTitle = Languages.message("FilesDelete");
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
    public void initTargetSection() {
        try {
            super.initTargetSection();

            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableView.getItems())
            );
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            if (srcFile == null || !srcFile.isFile()) {
                return Languages.message("Skip");
            }
            boolean ok;
            String msg;
            if (deleteRadio.isSelected()) {
                ok = FileDeleteTools.delete(srcFile);
                msg = Languages.message("FileDeletedSuccessfully") + ": " + srcFile.getAbsolutePath();
            } else {
                ok = Desktop.getDesktop().moveToTrash(srcFile);
                msg = Languages.message("FileMoveToTrashSuccessfully") + ": " + srcFile.getAbsolutePath();
            }
            if (ok) {
                if (verboseCheck == null || verboseCheck.isSelected()) {
                    updateLogs(msg);
                }
                return Languages.message("Successful");
            } else {
                return Languages.message("Failed");
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return Languages.message("Failed");
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
            MyBoxLog.error(e.toString());
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
                    if (FileDeleteTools.deleteDir(sourcePath)) {
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(Languages.message("DirectoryDeletedSuccessfully") + ": " + sourcePath.getAbsolutePath());
                        }
                    }
                } else {
                    if (Desktop.getDesktop().moveToTrash(sourcePath)) {
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(Languages.message("DirectoryMoveToTrashSuccessfully") + ": " + sourcePath.getAbsolutePath());
                        }
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
