package mara.mybox.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2019-10-13
 * @License Apache License Version 2.0
 */
public class FilesCopyController extends FilesBatchController {

    @FXML
    protected CheckBox copyAttrCheck;

    public FilesCopyController() {
        baseTitle = AppVariables.message("FilesCopy");
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            countHandling(srcFile);
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
            targetFileGenerated(target);
            return AppVariables.message("Successful");

        } catch (Exception e) {
            logger.error(e.toString());
            return AppVariables.message("Failed");
        }
    }

}
