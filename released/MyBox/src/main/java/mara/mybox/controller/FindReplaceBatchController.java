package mara.mybox.controller;

import java.io.File;
import javafx.beans.binding.Bindings;
import mara.mybox.data.BytesEditInformation;
import mara.mybox.data.FileEditInformation;
import mara.mybox.data.FileEditInformation.Edit_Type;
import mara.mybox.data.FindReplaceFile;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data.TextEditInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-11-4
 * @License Apache License Version 2.0
 */
public abstract class FindReplaceBatchController extends BaseBatchFileController {

    protected FindReplaceFile findReplace;
    protected FindReplaceBatchOptions optionsController;
    protected Edit_Type editType = FileEditInformation.Edit_Type.Text;

    public FindReplaceBatchController() {
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

            optionsController.setParent(this, editType);

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
            MyBoxLog.error(e);
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

    public FileEditInformation info(File file) {
        FileEditInformation fileInfo;
        if (editType == Edit_Type.Bytes) {
            fileInfo = new BytesEditInformation(file);
        } else {
            fileInfo = new TextEditInformation(file);
            if (optionsController.autoDetermine && !TextTools.checkCharset(fileInfo)) {
                return null;
            }
            fileInfo.setLineBreak(TextTools.checkLineBreak(file));
            fileInfo.setLineBreakValue(TextTools.lineBreakValue(fileInfo.getLineBreak()));
        }
        fileInfo.setPagesNumber(2);
        fileInfo.setFindReplace(findReplace);
        findReplace.setFileInfo(fileInfo);
        return fileInfo;
    }

}
