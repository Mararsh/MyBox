package mara.mybox.controller;

import com.google.common.io.Files;
import java.io.File;
import java.text.MessageFormat;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import mara.mybox.data.FindReplaceFile;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data.TextEditInformation;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-11-4
 * @License Apache License Version 2.0
 */
public class TextReplaceBatchController extends FilesBatchController {

    protected FindReplaceFile replace;

    @FXML
    protected TextReplaceBatchOptions optionsController;

    public TextReplaceBatchController() {
        baseTitle = AppVariables.message("TextReplaceBatch");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(optionsController.findArea.textProperty().isEmpty())
                    .or(Bindings.isEmpty(tableView.getItems()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            String findString = optionsController.findArea.getText();
            if (findString.isEmpty()) {
                popError(message("EmptyValue"));
                return false;
            }
            String replaceString = optionsController.replaceArea.getText();

            replace = new FindReplaceFile()
                    .setMultiplePages(true)
                    .setPosition(0);
            replace.setOperation(FindReplaceString.Operation.ReplaceAll)
                    .setFindString(findString)
                    .setAnchor(0)
                    .setReplaceString(replaceString)
                    .setUnit(1)
                    .setIsRegex(optionsController.regexCheck.isSelected())
                    .setCaseInsensitive(optionsController.caseInsensitiveCheck.isSelected())
                    .setMultiline(optionsController.multilineCheck.isSelected())
                    .setDotAll(optionsController.dotallCheck.isSelected())
                    .setWrap(false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }

        return super.makeMoreParameters();
    }

    @Override
    public void disableControls(boolean disable) {
        super.disableControls(disable);
        optionsController.thisPane.setDisable(disable);
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            countHandling(srcFile);
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return AppVariables.message("Skip");
            }
            File tmpFile = FileTools.getTempFile();
            Files.copy(srcFile, tmpFile);
            TextEditInformation fileInfo = new TextEditInformation(tmpFile);
            if (optionsController.autoDetermine && !TextTools.checkCharset(fileInfo)) {
                return AppVariables.message("Failed");
            }
            fileInfo.setLineBreak(TextTools.checkLineBreak(srcFile));
            fileInfo.setLineBreakValue(TextTools.lineBreakValue(fileInfo.getLineBreak()));
            fileInfo.setFindReplace(replace);
            replace.setFileInfo(fileInfo);

            if (!replace.file() || !tmpFile.exists()) {
                return message("Failed");
            }
            if (FileTools.rename(tmpFile, target)) {
                targetFileGenerated(target);
                String info = MessageFormat.format(message("ReplaceAllOk"), replace.getCount());
                updateLogs(info);
                return info;
            } else {
                return message("Failed");
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return message("Failed");
        }
    }

}
