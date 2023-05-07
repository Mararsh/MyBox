package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import javafx.fxml.FXML;
import mara.mybox.data.TextEditInformation;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-11-4
 * @License Apache License Version 2.0
 */
public class TextFindBatchController extends TextReplaceBatchController {

    @FXML
    protected TextFindBatchOptions optionsController;

    public TextFindBatchController() {
        baseTitle = message("TextFindBatch");
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return message("Skip");
            }
            TextEditInformation fileInfo = new TextEditInformation(srcFile);
            if (optionsController.autoDetermine && !TextTools.checkCharset(fileInfo)) {
                return message("Failed");
            }
            fileInfo.setLineBreak(TextTools.checkLineBreak(srcFile));
            fileInfo.setLineBreakValue(TextTools.lineBreakValue(fileInfo.getLineBreak()));
            fileInfo.setPagesNumber(2);
            fileInfo.setFindReplace(findReplace);
            findReplace.setFileInfo(fileInfo);
            if (!findReplace.handleFile()) {
                return message("Failed");
            }
            DataFileCSV matchesData = findReplace.getMatchesData();
            int count = findReplace.getCount();
            if (count == 0 || matchesData == null) {
                return message("NotFound");
            }
            updateLogs(message("Count") + ": " + findReplace.getCount());
//            if (FileTools.rename(tmpFile, target)) {  // TBC
//                targetFileGenerated(target);
//                
//            } else {
//                return message("Failed");
//            }
            return MessageFormat.format(message("ReplaceAllOk"), count);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return message("Failed");
        }
    }

}
