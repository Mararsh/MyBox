package mara.mybox.controller;

import com.google.common.io.Files;
import java.io.File;
import java.text.MessageFormat;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import mara.mybox.data.FindReplaceFile;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data.TextEditInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-11-4
 * @License Apache License Version 2.0
 */
public class TextReplaceBatchController extends BaseBatchFileController {

    protected FindReplaceFile findReplace;

    @FXML
    protected TextReplaceBatchOptions optionsController;

    public TextReplaceBatchController() {
        baseTitle = message("TextReplaceBatch");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            optionsController.setParent(this);

            startButton.disableProperty().unbind();
            if (targetPathController != null) {
                startButton.disableProperty().bind(targetPathController.valid.not()
                        .or(optionsController.findArea.textProperty().isEmpty())
                        .or(Bindings.isEmpty(tableView.getItems()))
                );
            } else {
                startButton.disableProperty().bind(optionsController.findArea.textProperty().isEmpty()
                        .or(Bindings.isEmpty(tableView.getItems()))
                );
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            String findString = optionsController.findString();
            if (findString == null || findString.isEmpty()) {
                return false;
            }
            findReplace = new FindReplaceFile();
            findReplace.reset();
            findReplace.setPosition(0)
                    .setFindString(findString)
                    .setAnchor(0)
                    .setUnit(1)
                    .setIsRegex(optionsController.regexCheck.isSelected())
                    .setCaseInsensitive(optionsController.caseInsensitiveCheck.isSelected())
                    .setMultiline(optionsController.multilineCheck.isSelected())
                    .setDotAll(optionsController.dotallCheck.isSelected())
                    .setWrap(false);
            if (optionsController.replaceArea != null) {
                String replaceString = optionsController.replaceString();
                if (replaceString.equals(findString)) {
                    popError(message("Unchanged"));
                    return false;
                }
                findReplace.setOperation(FindReplaceString.Operation.ReplaceAll);
                findReplace.setReplaceString(replaceString);
            } else {
                findReplace.setOperation(FindReplaceString.Operation.FindAll);
            }

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

    public TextEditInformation info(File file) {
        TextEditInformation fileInfo = new TextEditInformation(file);
        if (optionsController.autoDetermine && !TextTools.checkCharset(fileInfo)) {
            return null;
        }
        fileInfo.setLineBreak(TextTools.checkLineBreak(file));
        fileInfo.setLineBreakValue(TextTools.lineBreakValue(fileInfo.getLineBreak()));
        fileInfo.setPagesNumber(2);
        fileInfo.setFindReplace(findReplace);
        findReplace.setFileInfo(fileInfo);
        return fileInfo;
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

            TextEditInformation info = info(tmpFile);
            if (info == null) {
                return message("Failed");
            }
            if (!findReplace.handleFile() || !tmpFile.exists()) {
                return message("Failed");
            }
            int count = findReplace.getCount();
            if (count > 0) {
                updateLogs(message("Count") + ": " + findReplace.getCount());
                if (FileTools.rename(tmpFile, target)) {
                    targetFileGenerated(target);
                    return MessageFormat.format(message("ReplaceAllOk"), count);
                } else {
                    return message("Failed");
                }
            } else {
                return message("NotFound");
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return message("Failed");
        }
    }

}
