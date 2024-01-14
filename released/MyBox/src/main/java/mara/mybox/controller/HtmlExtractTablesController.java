package mara.mybox.controller;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-10-17
 * @License Apache License Version 2.0
 */
public class HtmlExtractTablesController extends BaseBatchFileController {

    public HtmlExtractTablesController() {
        baseTitle = message("HtmlExtractTables");
        targetFileSuffix = "csv";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html, VisitHistory.FileType.CSV);
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        try {
            List<StringTable> tables = HtmlReadTools.Tables(
                    TextFileTools.readTexts(currentTask, srcFile), srcFile.getName());
            if (currentTask == null || !currentTask.isWorking()) {
                return message("Cancelled");
            }
            if (tables == null || tables.isEmpty()) {
                return message("NoData");
            }
            LinkedHashMap<File, Boolean> files = DataFileCSV.save(currentTask, targetPath, "", tables);
            if (currentTask == null || !currentTask.isWorking()) {
                return message("Cancelled");
            }
            if (files == null) {
                return message("NoData");
            }
            for (File file : files.keySet()) {
                targetFileGenerated(file);
            }
            return message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e);
            return message("Failed");
        }
    }

}
