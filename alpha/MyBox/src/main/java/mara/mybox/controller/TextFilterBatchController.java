package mara.mybox.controller;

import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import mara.mybox.data.TextEditInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-7-15
 * @License Apache License Version 2.0
 */
public class TextFilterBatchController extends BaseBatchFileController {

    @FXML
    protected ControlTextFilter filterController;

    public TextFilterBatchController() {
        baseTitle = message("TextFilterBatch");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(filterController.valid.not()
                    .or(Bindings.isEmpty(tableView.getItems()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public boolean makeMoreParameters() {
        filterController.checkFilterStrings();
        if (!filterController.pickValue()) {
            return false;
        }
        return super.makeMoreParameters();
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        try {
            String namePrefix = FileNameTools.prefix(srcFile.getName());
            File target = makeTargetFile(message("Filter") + "-" + namePrefix, ".txt", targetPath);
            if (target == null) {
                return message("Skip");
            }
            TextEditInformation fileInfo = new TextEditInformation(srcFile);
            fileInfo.setLineBreak(TextTools.checkLineBreak(currentTask, srcFile));
            fileInfo.setLineBreakValue(TextTools.lineBreakValue(fileInfo.getLineBreak()));
            fileInfo.checkCharset();
            if (currentTask == null || !currentTask.isWorking()) {
                return message("Canceled");
            }

            fileInfo.setFilterStrings(filterController.filterStrings);
            fileInfo.setFilterType(filterController.filterType);

            File filteredFile = fileInfo.filter(currentTask, filterController.filterLineNumberCheck.isSelected());
            if (currentTask == null || !currentTask.isWorking()) {
                return message("Canceled");
            }
            if (filteredFile == null || !filteredFile.exists() || filteredFile.length() == 0) {
                return message("NoData");
            }
            if (FileTools.override(filteredFile, target)) {
                targetFileGenerated(target);
                return message("Successful");
            } else {
                return message("Failed");
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
            return message("Failed");
        }
    }

}
