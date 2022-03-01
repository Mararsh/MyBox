package mara.mybox.controller;

import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import mara.mybox.data.TextEditInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-7-15
 * @License Apache License Version 2.0
 */
public class TextFilterBatchController extends BaseBatchFileController {

    @FXML
    protected ControlTextFilter filterController;

    public TextFilterBatchController() {
        baseTitle = Languages.message("TextFilterBatch");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            filterController.sourceLen = Long.MAX_VALUE;

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(targetPathController.valid.not()
                    .or(filterController.valid.not())
                    .or(Bindings.isEmpty(tableView.getItems()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public boolean makeMoreParameters() {
        filterController.checkFilterStrings();
        if (!filterController.valid.get()) {
            return false;
        }
        return super.makeMoreParameters();
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            String namePrefix = FileNameTools.prefix(srcFile.getName());
            File target = makeTargetFile(Languages.message("Filter") + "-" + namePrefix, ".txt", targetPath);
            if (target == null) {
                return Languages.message("Skip");
            }
            TextEditInformation fileInfo = new TextEditInformation(srcFile);
            fileInfo.setLineBreak(TextTools.checkLineBreak(srcFile));
            fileInfo.setLineBreakValue(TextTools.lineBreakValue(fileInfo.getLineBreak()));
            fileInfo.checkCharset();

            fileInfo.setFilterStrings(filterController.filterStrings);
            fileInfo.setFilterType(filterController.filterType);

            File filteredFile = fileInfo.filter(filterController.filterLineNumberCheck.isSelected());

            if (filteredFile == null || !filteredFile.exists() || filteredFile.length() == 0) {
                return Languages.message("NoData");
            }
            if (FileTools.rename(filteredFile, target)) {
                targetFileGenerated(target);
                return Languages.message("Successful");
            } else {
                return Languages.message("Failed");
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return Languages.message("Failed");
        }
    }

}
