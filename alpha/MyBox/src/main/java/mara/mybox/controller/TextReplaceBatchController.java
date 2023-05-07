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
import mara.mybox.db.table.TableStringValues;
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
            startButton.disableProperty().bind(targetPathController.valid.not()
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
            if (findString == null || findString.isEmpty()) {
                popError(message("EmptyValue"));
                return false;
            }
            if (!findString.isBlank()) {
                TableStringValues.add(baseName + "FindString", findString);
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
                findReplace.setOperation(FindReplaceString.Operation.ReplaceAll);
                String replaceString = optionsController.replaceArea.getText();
                if (replaceString == null) {
                    replaceString = "";
                }
                findReplace.setReplaceString(replaceString);
                if (!replaceString.isBlank()) {
                    TableStringValues.add(baseName + "ReplaceString", findString);
                }
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

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return message("Skip");
            }
            File tmpFile = FileTmpTools.getTempFile();
            Files.copy(srcFile, tmpFile);
            TextEditInformation fileInfo = new TextEditInformation(tmpFile);
            if (optionsController.autoDetermine && !TextTools.checkCharset(fileInfo)) {
                return message("Failed");
            }
            fileInfo.setLineBreak(TextTools.checkLineBreak(srcFile));
            fileInfo.setLineBreakValue(TextTools.lineBreakValue(fileInfo.getLineBreak()));
            fileInfo.setPagesNumber(2);
            fileInfo.setFindReplace(findReplace);
            findReplace.setFileInfo(fileInfo);
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
