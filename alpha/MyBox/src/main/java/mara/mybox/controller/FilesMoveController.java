package mara.mybox.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileDeleteTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-10-13
 * @License Apache License Version 2.0
 */
public class FilesMoveController extends BaseBatchFileController {

    public FilesMoveController() {
        baseTitle = message("FilesMove");
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return message("Skip");
            }
            Path path = Files.move(Paths.get(srcFile.getAbsolutePath()), Paths.get(target.getAbsolutePath()),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            if (path == null) {
                return message("Failed");
            }
            if (verboseCheck == null || verboseCheck.isSelected()) {
                updateLogs(message("FileMovedSuccessfully") + ": " + path.toString());
            }
            return message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e);
            return message("Failed");
        }
    }

    @Override
    protected boolean handleDirectory(FxTask currentTask, File sourcePath, String targetPath) {
        if (super.handleDirectory(currentTask, sourcePath, targetPath)) {
            if (sourcePath != null && sourcePath.isDirectory()
                    && sourcePath.list().length == 0) {
                FileDeleteTools.deleteDir(currentTask, sourcePath);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void afterHandleFiles(FxTask currentTask) {
        targetFileGenerated(targetPath);
    }

}
