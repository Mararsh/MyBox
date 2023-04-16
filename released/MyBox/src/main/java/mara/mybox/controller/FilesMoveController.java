package mara.mybox.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2019-10-13
 * @License Apache License Version 2.0
 */
public class FilesMoveController extends BaseBatchFileController {

    public FilesMoveController() {
        baseTitle = Languages.message("FilesMove");
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return Languages.message("Skip");
            }
            Path path = Files.move(Paths.get(srcFile.getAbsolutePath()), Paths.get(target.getAbsolutePath()),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            if (path == null) {
                return Languages.message("Failed");
            }
            if (verboseCheck == null || verboseCheck.isSelected()) {
                updateLogs(Languages.message("FileMovedSuccessfully") + ": " + path.toString());
            }
            return Languages.message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return Languages.message("Failed");
        }
    }

    @Override
    protected boolean handleDirectory(File sourcePath, String targetPath) {
        if (super.handleDirectory(sourcePath, targetPath)) {
            if (sourcePath != null && sourcePath.isDirectory()
                    && sourcePath.list().length == 0) {
                FileDeleteTools.deleteDir(sourcePath);
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
