package mara.mybox.controller;

import com.google.common.io.Files;
import java.io.File;
import java.text.MessageFormat;
import javafx.fxml.FXML;
import mara.mybox.data.FileEditInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-11-4
 * @License Apache License Version 2.0
 */
public class TextReplaceBatchController extends FindReplaceBatchController {

    @FXML
    protected FindReplaceBatchOptions textReplaceOptionsController;

    public TextReplaceBatchController() {
        baseTitle = message("TextReplaceBatch");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            optionsController = textReplaceOptionsController;

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return message("Skip");
            }
            File tmpFile = FileTmpTools.getTempFile();
            Files.copy(srcFile, tmpFile);

            FileEditInformation info = info(tmpFile);
            if (info == null) {
                return message("Failed");
            }
            if (!findReplace.handleFile() || !tmpFile.exists()) {
                return message("Failed");
            }
            int count = findReplace.getCount();
            if (count > 0) {
                updateLogs(message("Count") + ": " + findReplace.getCount());
                if (FileTools.override(tmpFile, target)) {
                    targetFileGenerated(target);
                    return MessageFormat.format(message("ReplaceAllOk"), count);
                } else {
                    return message("Failed");
                }
            } else {
                return message("NotFound");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return message("Failed");
        }
    }

}
