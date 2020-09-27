package mara.mybox.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-10-13
 * @License Apache License Version 2.0
 */
public class FilesMoveController extends FilesBatchController {

    public FilesMoveController() {
        baseTitle = AppVariables.message("FilesMove");
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            countHandling(srcFile);
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return AppVariables.message("Skip");
            }
            Path path = Files.move(Paths.get(srcFile.getAbsolutePath()), Paths.get(target.getAbsolutePath()),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            if (path == null) {
                return AppVariables.message("Failed");
            }
            if (verboseCheck == null || verboseCheck.isSelected()) {
                updateLogs(message("FileMovedSuccessfully") + ": " + path.toString());
            }
            return AppVariables.message("Successful");
        } catch (Exception e) {
            logger.error(e.toString());
            return AppVariables.message("Failed");
        }
    }

    @Override
    protected boolean handleDirectory(File sourcePath, File targetPath) {
        if (super.handleDirectory(sourcePath, targetPath)) {
            if (sourcePath != null && sourcePath.isDirectory()
                    && sourcePath.list().length == 0) {
                sourcePath.delete();
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void afterHandleFiles() {
        targetFileGenerated(targetPath);
    }

}
