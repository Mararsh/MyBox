package mara.mybox.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-10-13
 * @License Apache License Version 2.0
 */
public class FilesCopyController extends FilesBatchController {

    protected int totalCopied = 0;

    @FXML
    protected CheckBox copyAttrCheck;

    public FilesCopyController() {
        baseTitle = AppVariables.message("FilesCopy");
    }

    @Override
    public boolean makeBatchParameters() {
        totalCopied = 0;
        return super.makeBatchParameters();
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return AppVariables.message("Skip");
            }
            Path path;
            if (copyAttrCheck.isSelected()) {
                path = Files.copy(Paths.get(srcFile.getAbsolutePath()), Paths.get(target.getAbsolutePath()),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            } else {
                path = Files.copy(Paths.get(srcFile.getAbsolutePath()), Paths.get(target.getAbsolutePath()),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            if (path == null) {
                return AppVariables.message("Failed");
            }
            totalCopied++;
            updateLogs(message("FileCopiedSuccessfully") + ": " + path.toString());
            currentParameters.finalTargetName = path.toString();
            targetFiles.add(target);
            return AppVariables.message("Successful");

        } catch (Exception e) {
            logger.error(e.toString());
            return AppVariables.message("Failed");
        }
    }

    @Override
    public void donePost() {
        super.donePost();

        updateLogs(message("StartTime") + ": " + DateTools.datetimeToString(startTime) + "   "
                + AppVariables.message("Cost") + ": " + DateTools.showTime(new Date().getTime() - startTime.getTime()), false, true);
        updateLogs(message("TotalCopiedFiles") + ": " + totalCopied);

    }

}
